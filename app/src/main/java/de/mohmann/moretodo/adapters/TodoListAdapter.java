package de.mohmann.moretodo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.util.DateFormatter;

/**
 * Created by mohmann on 2/3/16.
 */
public class TodoListAdapter extends ArrayAdapter<Todo> implements View.OnClickListener {

    final public static String TAG = "TodoListAdapter";

    public TodoListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public TodoListAdapter(Context context, int resource, List<Todo> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.todo_list_item, null);
        }

        final Todo todo = getItem(position);

        if (todo == null) {
            return v;
        }

        final TextView titleView = (TextView) v.findViewById(R.id.title);
        final TextView dateView = (TextView) v.findViewById(R.id.date);
        final ImageView iconView = (ImageView) v.findViewById(R.id.icon);
        final CheckBox checkbox = (CheckBox) v.findViewById(R.id.done);

        if (todo.isDone()) {
            v.setAlpha(0.3f);
            dateView.setText(DateFormatter.getDateTime(todo.getFinished()));
            iconView.setImageResource(R.drawable.ic_done_black_18dp);
        } else {
            v.setAlpha(1f);
            dateView.setText(DateFormatter.getDateTime(todo.getCreated()));
            iconView.setImageResource(R.drawable.ic_add_black_18dp);
        }

        titleView.setText(todo.getTitle());

        checkbox.setTag(todo);
        checkbox.setChecked(todo.isDone());
        checkbox.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        Todo todo = (Todo) v.getTag();

        if (todo == null)
            return;

        todo.setDone(!todo.isDone());
        TodoStore.getInstance().persist();
        notifyDataSetChanged();
    }
}
