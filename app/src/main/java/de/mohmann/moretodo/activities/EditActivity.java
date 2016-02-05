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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.util.DateFormatter;
import de.mohmann.moretodo.util.Utils;

public class EditActivity extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    final public static String TAG = "EditActivity";

    private EditText mInputTitle;
    private EditText mInputContent;
    private CheckBox mCheckBoxDueDate;
    private RelativeLayout mContainerDueDate;
    private EditText mInputDueDate;

    private Todo mTodo;

    private Calendar mCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setupActionBar();

        mInputTitle = (EditText) findViewById(R.id.input_title);
        mInputContent = (EditText) findViewById(R.id.input_content);
        mCheckBoxDueDate = (CheckBox) findViewById(R.id.checkbox_due_date);
        mContainerDueDate = (RelativeLayout) findViewById(R.id.container_due_date);
        mInputDueDate = (EditText) findViewById(R.id.input_due_date);

        mInputDueDate.setOnClickListener(this);
        mCheckBoxDueDate.setOnClickListener(this);

        mCalendar = Calendar.getInstance();

        /* get intent data */
        mTodo = getIntent().getParcelableExtra(Todo.EXTRA_TODO);

        populateViews();
    }

    @Override
    public void onClick(View view) {
       if (view.getId() == R.id.input_due_date) {
            Log.d(TAG, "due date clicked");
           showDatePickerDialog();
       } else if (view.getId() == R.id.checkbox_due_date) {
           if (mCheckBoxDueDate.isChecked()) {
               mContainerDueDate.setVisibility(View.VISIBLE);
               showDatePickerDialog();
           } else {
               mContainerDueDate.setVisibility(View.GONE);
           }
       } else if (view.getId() == R.id.toolbar) {
           onBackPressed();
       }
    }

    private void populateViews() {
        if (mTodo == null) {
            setTitle(getResources().getString(R.string.title_activity_new));
            mContainerDueDate.setVisibility(View.GONE);
        } else {
            mInputTitle.setText(mTodo.getTitle());
            mInputContent.setText(mTodo.getContent());

            if (mTodo.getDueDate() != Todo.NO_DUEDATE) {
                mCheckBoxDueDate.setChecked(true);
                mContainerDueDate.setVisibility(View.VISIBLE);

                mCalendar.setTimeInMillis(mTodo.getDueDate());
                mInputDueDate.setText(DateFormatter.getFullDate(mTodo.getDueDate()));
            } else {
                mContainerDueDate.setVisibility(View.GONE);
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

        toolbar.setNavigationOnClickListener(this);
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
        mCalendar.set(Calendar.SECOND, 0);
        mInputDueDate.setText(DateFormatter.getFullDate(mCalendar.getTime()));
        Log.d(TAG, mCalendar.getTime().toString());
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, monthOfYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mInputDueDate.setText(DateFormatter.getFullDate(mCalendar.getTime()));
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
            Todo todo;
            TodoStore todoStore = TodoStore.getInstance();
            String title = mInputTitle.getText().toString().trim();
            String content = mInputContent.getText().toString().trim();
            long dueDate = Todo.NO_DUEDATE;

            if (mCheckBoxDueDate.isChecked()) {
                if (mInputDueDate.getText().toString().isEmpty()) {
                    Utils.toast(this, R.string.message_todo_enter_duedate);
                    return true;
                }
                dueDate = mCalendar.getTimeInMillis();
            }

            if (title.isEmpty()) {
                Utils.toast(this, R.string.message_todo_enter_title);
                return true;
            }

            if (mTodo == null) {
                todo = new Todo(title, content);
                todo.setDueDate(dueDate);
                todoStore.add(todo);
                Utils.toast(this, R.string.message_todo_created);
            } else {
                todo = todoStore.getById(mTodo.getId());
                todo.setTitle(title);
                todo.setContent(content);
                todo.setDueDate(dueDate);
                if (dueDate > System.currentTimeMillis())
                    todo.setNotified(false);
                Utils.toast(this, R.string.message_todo_updated);
            }
            todoStore.persist();

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
