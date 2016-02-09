package de.mohmann.moretodo.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
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

        mTodoStore = TodoStore.getInstance(getContext());
        mTodoStore.addOnTodoListUpdateListener(this);
        mTodoListAdapter = new TodoListAdapter(getActivity(), R.layout.todo_list_item,
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
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(Todo.EXTRA_TODO, (Todo) view.getTag(R.id.TAG_TODO));
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
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
            String[] menuItems = res.getStringArray(R.array.todo_list_context_menu);
            menu.setHeaderTitle(res.getString(R.string.title_todo_list_context_menu));

            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        View v = info.targetView;

        if (v.getId() == R.id.todo_list_item) {
            final Todo todo = (Todo) v.getTag(R.id.TAG_TODO);

            switch (menuItemIndex) {
                case 0: /* edit */
                    Intent intent = new Intent(getActivity(), EditActivity.class);
                    intent.putExtra(Todo.EXTRA_TODO, todo);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                    break;
                case 1: /* delete */
                    Log.d(TAG, "deleting todo: " + todo.toString());
                    mTodoStore.removeById(todo.getId());
                    Utils.toast(getActivity(), R.string.message_todo_deleted);
                    break;
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
