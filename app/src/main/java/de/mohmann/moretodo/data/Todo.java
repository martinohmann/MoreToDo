package de.mohmann.moretodo.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Date;
import java.util.UUID;

/**
 * Created by mohmann on 2/3/16.
 */
public class Todo implements Comparable<Todo>, Parcelable {

    final public static String TAG = "Todo";

    final public static int NO_DUEDATE = -1;

    private String id = UUID.randomUUID().toString();
    private String title = "";
    private String content = "";
    private long created = System.currentTimeMillis();
    private long finished = 0;
    private long dueDate = NO_DUEDATE;
    private boolean done = false;

    final public static String EXTRA_TODO = "de.mohmann.moretodo.Todo";

    final private static String PARCEL_EXTRA_ID = "de.mohmann.moretodo.data.Todo.ID";
    final private static String PARCEL_EXTRA_TITLE = "de.mohmann.moretodo.data.Todo.TITLE";
    final private static String PARCEL_EXTRA_CONTENT = "de.mohmann.moretodo.data.Todo.CONTENT";
    final private static String PARCEL_EXTRA_DONE = "de.mohmann.moretodo.data.Todo.DONE";
    final private static String PARCEL_EXTRA_CREATED = "de.mohmann.moretodo.data.Todo.CREATED";
    final private static String PARCEL_EXTRA_FINISHED = "de.mohmann.moretodo.data.Todo.FINISHED";
    final private static String PARCEL_EXTRA_DUEDATE = "de.mohmann.moretodo.data.Todo.DUEDATE";

    public static final Parcelable.Creator<Todo> CREATOR = new Parcelable.Creator<Todo>() {
        public Todo createFromParcel(final Parcel in) {
            return new Todo(in);
        }

        public Todo[] newArray(final int size) {
            return new Todo[size];
        }
    };

    public Todo(String title, String content) {
        this.title = title;
        this.content = content;
    }

    protected Todo(final Parcel in) {
        final Bundle b = in.readBundle(getClass().getClassLoader());
        id = b.getString(PARCEL_EXTRA_ID, UUID.randomUUID().toString());
        title = b.getString(PARCEL_EXTRA_TITLE, "");
        content = b.getString(PARCEL_EXTRA_CONTENT, "");
        done = b.getBoolean(PARCEL_EXTRA_DONE, false);
        created = b.getLong(PARCEL_EXTRA_CREATED, 0);
        finished = b.getLong(PARCEL_EXTRA_FINISHED, 0);
        dueDate = b.getLong(PARCEL_EXTRA_DUEDATE, NO_DUEDATE);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int arg1) {
        final Bundle b = new Bundle(getClass().getClassLoader());
        b.putString(PARCEL_EXTRA_ID, id);
        b.putString(PARCEL_EXTRA_TITLE, title);
        b.putString(PARCEL_EXTRA_CONTENT, content);
        b.putBoolean(PARCEL_EXTRA_DONE, done);
        b.putLong(PARCEL_EXTRA_CREATED, created);
        b.putLong(PARCEL_EXTRA_FINISHED, finished);
        b.putLong(PARCEL_EXTRA_DUEDATE, dueDate);
        parcel.writeBundle(b);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreated() {
        return created;
    }

    public long getFinished() {
        return finished;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        if (done) {
            Log.d(TAG, "finished " + toString());
            finished = System.currentTimeMillis();
        }
        this.done = done;
    }

    @Override
    public int compareTo(@NonNull Todo another) {
        if (done && !another.isDone())
            return -1;
        if (!done && another.isDone())
            return 1;
        /* both done */
        if (done)
            return finished > another.getFinished() ? 1 : -1;
        /* both not done */
        return created > another.getCreated() ? 1 : -1;
    }

    public String toString() {
        return String.format("<Todo> id: %s, done: %s, title: %s", id, done, title);
    }
}
