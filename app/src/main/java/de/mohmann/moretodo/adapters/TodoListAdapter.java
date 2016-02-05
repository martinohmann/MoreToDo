package de.mohmann.moretodo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.util.DateFormatter;
import de.mohmann.moretodo.util.Utils;

/**
 * Created by mohmann on 2/3/16.
 */
public class TodoListAdapter extends ArrayAdapter<Todo> implements View.OnClickListener {

    final public static String TAG = "TodoListAdapter";

    final public static String FILTER_ALL = "all";
    final public static String FILTER_DONE = "done";
    final public static String FILTER_PENDING = "pending";

    private Filter mFilter;
    private List<Todo> mOriginalTodoList;
    private List<Todo> mFilteredTodoList = new ArrayList<>();
    public String mDefaultFilter;

    private Comparator<Todo> mComparator = new Comparator<Todo>() {
        @Override
        public int compare(Todo t1, Todo t2) {
            if (t1 == null || t2 == null)
                return -1;
            if (mDefaultFilter.equals(FILTER_ALL))
                return t1.getCreated() > t2.getCreated() ? -1 : 1;
            if (mDefaultFilter.equals(FILTER_PENDING))
                return t1.getCreated() > t2.getCreated() ? 1 : -1;
            if (mDefaultFilter.equals(FILTER_DONE))
                return t1.getFinished() > t2.getFinished() ? -1 : 1;
            return -1;
        }
    };

    public TodoListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public TodoListAdapter(Context context, int resource, List<Todo> items, String defaultFilter) {
        super(context, resource, items);
        mOriginalTodoList = items;
        mFilteredTodoList.addAll(items);
        mDefaultFilter = defaultFilter;
    }

    public Comparator<Todo> getComparator() {
        return mComparator;
    }

    @Override
    public int getCount() {
        return mFilteredTodoList.size();
    }

    @Override
    public Todo getItem(int position) {
        return mFilteredTodoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;

        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.todo_list_item, parent, false);

            holder = new ViewHolder();
            holder.titleView = (TextView) v.findViewById(R.id.title);
            holder.dateView = (TextView) v.findViewById(R.id.date);
            holder.iconView = (ImageView) v.findViewById(R.id.icon);
            holder.checkBox = (CheckBox) v.findViewById(R.id.done);
            holder.dueDateContainer = (RelativeLayout) v.findViewById(R.id.container_due_date);
            holder.dueDate = (TextView) v.findViewById(R.id.due_date);
            v.setTag(R.id.TAG_VIEWHOLDER, holder);
        } else {
            holder = (ViewHolder) v.getTag(R.id.TAG_VIEWHOLDER);
        }

        final Todo todo = getItem(position);

        if (todo == null) {
            return v;
        }

        if (todo.isDone()) {
            if (!mDefaultFilter.equals(FILTER_DONE))
                v.setAlpha(0.3f);
            holder.dateView.setText(DateFormatter.humanReadable(todo.getFinished()));
            holder.iconView.setImageResource(R.drawable.ic_done_black_18dp);
        } else {
            v.setAlpha(1f);
            holder.dateView.setText(DateFormatter.humanReadable(todo.getCreated()));
            holder.iconView.setImageResource(R.drawable.ic_add_black_18dp);
        }

        if (!todo.isDone() && todo.getDueDate() != Todo.NO_DUEDATE) {
            holder.dueDateContainer.setVisibility(View.VISIBLE);
            holder.dueDate.setText(DateFormatter.humanReadable(todo.getDueDate()));
        } else {
            holder.dueDateContainer.setVisibility(View.GONE);
        }

        holder.titleView.setText(todo.getTitle());

        holder.checkBox.setTag(todo);
        holder.checkBox.setChecked(todo.isDone());
        holder.checkBox.setOnClickListener(this);

        v.setTag(R.id.TAG_TODO, todo);
        return v;
    }

    @Override
    public void onClick(View v) {
        Todo todo = (Todo) v.getTag();

        if (todo == null)
            return;

        todo.setDone(!todo.isDone());
        TodoStore.getInstance().persist();

        if (todo.isDone()) {
            Utils.toast(getContext(), R.string.message_todo_done);
        } else {
            Utils.toast(getContext(), R.string.message_todo_pending);
        }

        applyDefaultFilter();
    }

    public void applyDefaultFilter() {
        getFilter().filter(mDefaultFilter);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new TodoFilter();
        return mFilter;
    }


    private class ViewHolder {
        TextView titleView;
        TextView dateView;
        ImageView iconView;
        CheckBox checkBox;
        RelativeLayout dueDateContainer;
        TextView dueDate;
    }

    private class TodoFilter extends Filter {

        private FilterResults filterDoneItems(boolean done) {
            FilterResults result = new FilterResults();
            List<Todo> filteredItems = new ArrayList<>();

            for (Todo todo : mOriginalTodoList) {
                if (todo.isDone() == done)
                    filteredItems.add(todo);
            }

            result.count = filteredItems.size();
            result.values = filteredItems;

            return result;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString();
            FilterResults result = new FilterResults();

            if (constraint.equals(FILTER_DONE)) {
                result = filterDoneItems(true);
            } else if (constraint.equals(FILTER_PENDING)) {
                result = filterDoneItems(false);
            } else {
                synchronized(this) {
                    result.values = mOriginalTodoList;
                    result.count = mOriginalTodoList.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredTodoList = (ArrayList<Todo>) results.values;

            if(mFilteredTodoList != null && mFilteredTodoList.size() > 0) {
                Collections.sort(mFilteredTodoList, mComparator);
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
