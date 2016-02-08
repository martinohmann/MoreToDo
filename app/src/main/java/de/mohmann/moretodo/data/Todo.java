package de.mohmann.moretodo.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by mohmann on 2/3/16.
 */
public class Todo implements Comparable<Todo>, Parcelable {

    final public static String TAG = "Todo";

    final public static int DATE_UNSET = -1;
    final public static int ID_UNSET = -1;

    private long id = ID_UNSET;
    private String title = "";
    private String content = "";
    private long creationDate = System.currentTimeMillis();
    private long finishDate = DATE_UNSET;
    private long dueDate = DATE_UNSET;
    private boolean done = false;
    private boolean notified = false;

    final public static String TABLE_NAME = "todos";
    final public static String COLUMN_ID = "id";
    final public static String COLUMN_TITLE = "title";
    final public static String COLUMN_CONTENT = "content";
    final public static String COLUMN_CREATION_DATE = "creation_date";
    final public static String COLUMN_FINISH_DATE = "finish_date";
    final public static String COLUMN_DUE_DATE = "due_date";
    final public static String COLUMN_DONE = "done";
    final public static String COLUMN_NOTIFIED = "notified";

    final public static String EXTRA_TODO = "de.mohmann.moretodo.data.Todo";

    final private static String PARCEL_EXTRA_ID = "de.mohmann.moretodo.data.Todo.ID";
    final private static String PARCEL_EXTRA_TITLE = "de.mohmann.moretodo.data.Todo.TITLE";
    final private static String PARCEL_EXTRA_CONTENT = "de.mohmann.moretodo.data.Todo.CONTENT";
    final private static String PARCEL_EXTRA_CREATED = "de.mohmann.moretodo.data.Todo.CREATED";
    final private static String PARCEL_EXTRA_FINISHED = "de.mohmann.moretodo.data.Todo.FINISHED";
    final private static String PARCEL_EXTRA_DUEDATE = "de.mohmann.moretodo.data.Todo.DUEDATE";
    final private static String PARCEL_EXTRA_DONE = "de.mohmann.moretodo.data.Todo.DONE";
    final private static String PARCEL_EXTRA_NOTIFIED = "de.mohmann.moretodo.data.Todo.NOTIFIED";

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
        id = b.getLong(PARCEL_EXTRA_ID, ID_UNSET);
        title = b.getString(PARCEL_EXTRA_TITLE, "");
        content = b.getString(PARCEL_EXTRA_CONTENT, "");
        creationDate = b.getLong(PARCEL_EXTRA_CREATED, System.currentTimeMillis());
        finishDate = b.getLong(PARCEL_EXTRA_FINISHED, DATE_UNSET);
        dueDate = b.getLong(PARCEL_EXTRA_DUEDATE, DATE_UNSET);
        done = b.getBoolean(PARCEL_EXTRA_DONE, false);
        notified = b.getBoolean(PARCEL_EXTRA_NOTIFIED, false);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int arg1) {
        final Bundle b = new Bundle(getClass().getClassLoader());
        b.putLong(PARCEL_EXTRA_ID, id);
        b.putString(PARCEL_EXTRA_TITLE, title);
        b.putString(PARCEL_EXTRA_CONTENT, content);
        b.putLong(PARCEL_EXTRA_CREATED, creationDate);
        b.putLong(PARCEL_EXTRA_FINISHED, finishDate);
        b.putLong(PARCEL_EXTRA_DUEDATE, dueDate);
        b.putBoolean(PARCEL_EXTRA_DONE, done);
        b.putBoolean(PARCEL_EXTRA_NOTIFIED, notified);
        parcel.writeBundle(b);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(long finishDate) {
        this.finishDate = finishDate;
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

    public void setDone(boolean done, boolean setFinishTime) {
        if (done && setFinishTime) {
            Log.d(TAG, "finishDate " + toString());
            finishDate = System.currentTimeMillis();
        }
        this.done = done;
    }

    public void setDone(boolean done) {
        setDone(done, true);
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    @Override
    public int compareTo(@NonNull Todo another) {
        if (done && !another.isDone())
            return -1;
        if (!done && another.isDone())
            return 1;
        /* both done */
        if (done)
            return finishDate > another.getFinishDate() ? 1 : -1;
        /* both not done */
        return creationDate > another.getCreationDate() ? 1 : -1;
    }

    public String toString() {
        return String.format("<Todo id: %s, title: \"%s\", content: \"%s\",\n" +
                        "creationDate: %d, finishDate: %d, dueDate: %d,\n" +
                        "done: %s, notified: %s>",
                id, title, content, creationDate, finishDate, dueDate, done, notified);
    }
}
