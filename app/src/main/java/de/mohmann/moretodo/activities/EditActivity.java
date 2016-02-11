package de.mohmann.moretodo.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.util.DateUtils;
import de.mohmann.moretodo.util.Utils;

public class EditActivity extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    final public static String TAG = "EditActivity";

    private Calendar mCalendar;
    private EditText mInputTitle;
    private EditText mInputContent;
    private EditText mInputDueDate;
    private Todo mTodo;
    private TodoStore mTodoStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setupActionBar();

        mInputTitle = (EditText) findViewById(R.id.input_title);
        mInputContent = (EditText) findViewById(R.id.input_content);
        mInputDueDate = (EditText) findViewById(R.id.input_due_date);

        mInputDueDate.setOnClickListener(this);

        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        mTodoStore = TodoStore.getInstance(this);

        /* get intent data */
        final long todoId = getIntent().getLongExtra(Todo.EXTRA_ID, Todo.ID_UNSET);

        mTodo = mTodoStore.getById(todoId);

        populateViews();
    }

    @Override
    public void onClick(View view) {
       if (view.getId() == R.id.input_due_date) {
           showDatePickerDialog();
       } else if (view.getId() == R.id.toolbar) {
           onBackPressed();
       }
    }

    private void populateViews() {
        if (mTodo == null) {
            setTitle(getResources().getString(R.string.title_activity_new));
        } else {

            mInputTitle.setText(mTodo.getTitle());
            mInputContent.setText(mTodo.getContent());

            if (mTodo.getDueDate() != Todo.DATE_UNSET) {
                mCalendar.setTimeInMillis(mTodo.getDueDate());
                mInputDueDate.setText(DateUtils.getFullDate(mTodo.getDueDate()));
            }
        }
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar == null)
            return;

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        /* add back button and bind onBackPressed action to it */
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog dialog = DatePickerDialog.newInstance(this,
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show(getFragmentManager(), "DatePickerDialog");
    }

    private void showTimePickerDialog() {
        TimePickerDialog dialog = TimePickerDialog.newInstance(this,
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.SECOND),
                DateFormat.is24HourFormat(getApplicationContext())
        );
        dialog.show(getFragmentManager(), "TimePickerDialog");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        mInputDueDate.setText(DateUtils.getFullDate(mCalendar.getTime()));
        Log.d(TAG, mCalendar.getTime().toString());
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, monthOfYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mInputDueDate.setText(DateUtils.getFullDate(mCalendar.getTime()));
        showTimePickerDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            String title = mInputTitle.getText().toString().trim();
            String content = mInputContent.getText().toString().trim();
            boolean hasDueDate = !mInputDueDate.getText().toString().isEmpty();
            long dueDate = Todo.DATE_UNSET;

            if (hasDueDate) {
                dueDate = mCalendar.getTimeInMillis();
            }

            if (title.isEmpty()) {
                Utils.toast(this, R.string.message_todo_enter_title);
                return true;
            }

            if (mTodo == null) {
                Todo todo = new Todo(title, content);
                todo.setDueDate(dueDate);
                mTodoStore.add(todo);
                Utils.toast(this, R.string.message_todo_created);
            } else {
                mTodo.setTitle(title);
                mTodo.setContent(content);
                mTodo.setDueDate(dueDate);
                if (dueDate > System.currentTimeMillis())
                    mTodo.setNotified(false);
                mTodoStore.save(mTodo);
                Utils.toast(this, R.string.message_todo_updated);
            }

            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}
