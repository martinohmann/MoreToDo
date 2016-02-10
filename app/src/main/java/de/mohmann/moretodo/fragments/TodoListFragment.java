package de.mohmann.moretodo.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.activities.DetailActivity;
import de.mohmann.moretodo.activities.EditActivity;
import de.mohmann.moretodo.adapters.TodoListAdapter;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.util.Utils;

public class TodoListFragment extends Fragment implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, TodoStore.OnTodoListUpdateListener {

    final public static String TAG = "TodoListFragment";

    final public static String EXTRA_LIST_TYPE =
            "de.mohmann.moretodo.fragments.TodoListFragment.EXTRA_LIST_TYPE";

    private SwipeRefreshLayout mSwipeLayout;
    private TodoListAdapter mTodoListAdapter;
    private TodoStore mTodoStore;
    private Activity mActivity;

    public static TodoListFragment newInstance(int listType) {
        TodoListFragment f = new TodoListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_LIST_TYPE, listType);
        f.setArguments(bundle);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        View view = inflater.inflate(R.layout.fragment_todo_list, container, false);
        int listType = bundle.getInt(EXTRA_LIST_TYPE, TodoListAdapter.LIST_ALL);

        mActivity = getActivity();
        mTodoStore = TodoStore.getInstance(getContext());
        mTodoStore.addOnTodoListUpdateListener(this);
        mTodoListAdapter = new TodoListAdapter(mActivity, R.layout.todo_list_item,
                mTodoStore.getList(), listType);
        mTodoListAdapter.applyFilter();

        final ListView listView = (ListView) view.findViewById(R.id.todo_list);
        listView.setEmptyView(view.findViewById(R.id.todo_list_empty));
        listView.setAdapter(mTodoListAdapter);
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeLayout.setOnRefreshListener(this);

        return view;
    }
    @Override
    public void onRefresh() {
        /* use delay to make it look smoother */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateViews();
                mSwipeLayout.setRefreshing(false);
            }
        }, 1000);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mActivity, DetailActivity.class);
        final Todo todo = (Todo) view.getTag(R.id.TAG_TODO);
        intent.putExtra(Todo.EXTRA_ID, todo.getId());
        startActivity(intent);
        mActivity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }


    private void updateViews() {
        if (mTodoListAdapter != null) {
            mTodoListAdapter.setList(mTodoStore.getList());
            mTodoListAdapter.applyFilter();
        }
    }

    @Override
    public void onTodoListUpdate() {
        updateViews();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Resources res = getResources();
        if (v.getId() == R.id.todo_list) {
            mActivity.getMenuInflater().inflate(R.menu.context_menu_list_item, menu);
            menu.setHeaderTitle(res.getString(R.string.title_context_menu_list_item));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id = item.getItemId();
        View v = info.targetView;

        if (v.getId() == R.id.todo_list_item) {
            final Todo todo = (Todo) v.getTag(R.id.TAG_TODO);

            if (id == R.id.action_edit) {
                Intent intent = new Intent(mActivity, EditActivity.class);
                intent.putExtra(Todo.EXTRA_ID, todo.getId());
                startActivity(intent);
                mActivity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            } else if (id == R.id.action_delete) {
                mTodoStore.remove(todo);
                Utils.toast(mActivity, R.string.message_todo_deleted);
            }
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTodoStore.removeOnTodoListUpdateListener(this);
    }
}
