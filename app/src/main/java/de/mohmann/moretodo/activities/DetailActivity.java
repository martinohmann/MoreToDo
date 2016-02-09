package de.mohmann.moretodo.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.services.BackgroundService;
import de.mohmann.moretodo.util.DateFormatter;
import de.mohmann.moretodo.util.Preferences;
import de.mohmann.moretodo.util.Utils;

public class DetailActivity extends AppCompatActivity
        implements DialogInterface.OnClickListener {

    final public static String TAG = "DetailActivity";

    private AlertDialog mDeleteDialog;
    private TextView mTitleView;
    private CardView mContainerContent;
    private TextView mContentView;
    private TextView mCreatedView;
    private ImageView mDoneView;
    private TextView mFinishedView;
    private CardView mContainerDueDate;
    private TextView mDueDateView;
    private Todo mTodo;
    private TodoStore mTodoStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setupActionBar();

        mTitleView = (TextView) findViewById(R.id.text_title);
        mContainerContent = (CardView) findViewById(R.id.container_content);
        mContentView = (TextView) findViewById(R.id.text_content);
        mCreatedView = (TextView) findViewById(R.id.text_created);
        mDoneView = (ImageView) findViewById(R.id.icon_done);
        mFinishedView = (TextView) findViewById(R.id.text_finished);
        mContainerDueDate = (CardView) findViewById(R.id.container_due_date);
        mDueDateView = (TextView) findViewById(R.id.text_due_date);

        mTodoStore = TodoStore.getInstance(this);

        /* get intent data */
        Intent intent = getIntent();

        mTodo = intent.getParcelableExtra(Todo.EXTRA_TODO);

        final int notificationId = intent.getIntExtra(BackgroundService.EXTRA_NOTIFICATION_ID, -1);

        if (notificationId > -1) {
            final NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
        
        populateViews();
        buildDeleteDialog();
    }
    
    private void populateViews() {
        if (mTodo == null)
            return;

        setTitle(mTodo.getTitle());
        mTitleView.setText(mTodo.getTitle());

        if (mTodo.getContent().isEmpty()) {
            mContainerContent.setVisibility(View.GONE);
        } else {
            if (Preferences.isMarkdownEnabled(this)) {
                mContentView.setText(Utils.markdownToHtml(mTodo.getContent()));
            } else {
                mContentView.setText(mTodo.getContent());
            }
            mContainerContent.setVisibility(View.VISIBLE);
        }

        mCreatedView.setText(DateFormatter.getFullDate(mTodo.getCreationDate()));

        if (mTodo.isDone()) {
            mDoneView.setImageResource(R.drawable.ic_done_black_18dp);
            mFinishedView.setText(DateFormatter.getFullDate(mTodo.getFinishDate()));
        }

        if (mTodo.getDueDate() == Todo.DATE_UNSET) {
            mContainerDueDate.setVisibility(View.GONE);
        } else {
            mDueDateView.setText(DateFormatter.getFullDate(mTodo.getDueDate()));
            mContainerDueDate.setVisibility(View.VISIBLE);
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

    private void buildDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.question_delete_item_long)
                .setTitle(R.string.alert_delete_item)
                .setPositiveButton(R.string.yes, this)
                .setNegativeButton(R.string.cancel, this);

        mDeleteDialog = builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        if (dialog != mDeleteDialog)
            return;

        if (id == DialogInterface.BUTTON_POSITIVE) {
            mTodoStore.remove(mTodo);

            Utils.toast(this, R.string.message_todo_deleted);
            finish();
        } else {
            dialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (mTodo == null)
            return true;

        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(Todo.EXTRA_TODO, mTodo);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } else if (id == R.id.action_delete) {
            mDeleteDialog.show();
        } else if (id == R.id.action_mark_done) {
            if (mTodo.isDone())
                return true;

            mTodo.setDone(true);
            mTodoStore.save(mTodo);

            Utils.toast(this, R.string.message_todo_done);

            populateViews();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mTodo != null) {
            mTodo = mTodoStore.getById(mTodo.getId());
            populateViews();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}
