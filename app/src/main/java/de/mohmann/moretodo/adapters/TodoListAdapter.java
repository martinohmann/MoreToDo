package de.mohmann.moretodo.adapters;

import android.content.Context;
import android.graphics.Paint;
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

    final public static String FILTER_NONE = "";

    final public static int LIST_ALL = 0x1;
    final public static int LIST_DONE = 0x2;
    final public static int LIST_PENDING = 0x3;

    private Filter mFilter;
    private int mListType;
    private List<Todo> mOriginalTodoList;
    private List<Todo> mFilteredTodoList = new ArrayList<>();

    private Comparator<Todo> mComparator = new Comparator<Todo>() {
        @Override
        public int compare(Todo t1, Todo t2) {
            if (t1 == null || t2 == null)
                return -1;
            if (mListType == LIST_ALL)
                return t1.getCreationDate() > t2.getCreationDate() ? -1 : 1;
            if (mListType == LIST_PENDING) {
                if (t1.getDueDate() == Todo.DATE_UNSET && t2.getDueDate() == Todo.DATE_UNSET) {
                    return t1.getCreationDate() > t2.getCreationDate() ? 1 : -1;
                }
                if (t1.getDueDate() == Todo.DATE_UNSET) {
                    return 1;
                }
                if (t2.getDueDate() == Todo.DATE_UNSET) {
                    return -1;
                }
                return t1.getDueDate() > t2.getDueDate() ? 1 : -1;
            }
            if (mListType == LIST_DONE)
                return t1.getFinishDate() > t2.getFinishDate() ? -1 : 1;
            return -1;
        }
    };

    public TodoListAdapter(Context context, int resource, List<Todo> items, int listType) {
        super(context, resource, items);
        mOriginalTodoList = items;
        mFilteredTodoList.addAll(items);
        mListType = listType;
        Collections.sort(mFilteredTodoList, getComparator());
    }

    public Comparator<Todo> getComparator() {
        return mComparator;
    }

    public void setList(List<Todo> list) {
        mOriginalTodoList = list;
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
    public View getView(int position, final View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;

        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.todo_list_item, parent, false);

            holder = new ViewHolder();
            holder.titleView = (TextView) v.findViewById(R.id.title);
            holder.dateView = (TextView) v.findViewById(R.id.date);
            holder.iconView = (ImageView) v.findViewById(R.id.icon);
            holder.contentIconView = (ImageView) v.findViewById(R.id.content_icon);
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

       final int paintFlags = holder.titleView.getPaintFlags();

        if (todo.isDone()) {
            if (mListType != LIST_DONE) {
                v.setAlpha(0.3f);
                holder.titleView.setPaintFlags(paintFlags | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            holder.dateView.setText(DateFormatter.humanReadable(todo.getFinishDate()));
            holder.iconView.setImageResource(R.drawable.ic_done_black_18dp);
        } else {
            v.setAlpha(1f);
            holder.titleView.setPaintFlags(paintFlags & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.dateView.setText(DateFormatter.humanReadable(todo.getCreationDate()));
            holder.iconView.setImageResource(R.drawable.ic_add_black_18dp);
        }

        if (!todo.isDone() && todo.getDueDate() != Todo.DATE_UNSET) {
            holder.dueDateContainer.setVisibility(View.VISIBLE);
            holder.dueDate.setText(DateFormatter.humanReadable(todo.getDueDate()));
        } else {
            holder.dueDateContainer.setVisibility(View.GONE);
        }

        if (todo.getContent().isEmpty()) {
            holder.contentIconView.setVisibility(View.GONE);
        } else {
            holder.contentIconView.setVisibility(View.VISIBLE);
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
        TodoStore.getInstance(getContext()).save(todo);

        if (todo.isDone()) {
            Utils.toast(getContext(), R.string.message_todo_done);
        } else {
            Utils.toast(getContext(), R.string.message_todo_pending);
        }

        applyFilter();
    }

    public void applyFilter() {
        getFilter().filter(FILTER_NONE);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new TodoFilter();
        return mFilter;
    }

    private class ViewHolder {
        TextView titleView;
        ImageView contentIconView;
        TextView dateView;
        ImageView iconView;
        CheckBox checkBox;
        RelativeLayout dueDateContainer;
        TextView dueDate;
    }

    private class TodoFilter extends Filter {

        private List<Todo> getDoneItems(boolean done) {
            List<Todo> items = new ArrayList<>();

            for (Todo todo : mOriginalTodoList) {
                if (todo.isDone() == done)
                    items.add(todo);
            }

            return items;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            List<Todo> items;

            if (mListType == LIST_DONE) {
                items = getDoneItems(true);
            } else if (mListType == LIST_PENDING) {
                items = getDoneItems(false);
            } else {
                synchronized(this) {
                    items = mOriginalTodoList;
                }
            }
            result.values = items;
            result.count = items.size();

            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredTodoList = (ArrayList<Todo>) results.values;

            if (mFilteredTodoList != null && mFilteredTodoList.size() > 0) {
                Collections.sort(mFilteredTodoList, getComparator());
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
