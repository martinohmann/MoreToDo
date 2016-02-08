package de.mohmann.moretodo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.util.Constants;

/**
 * Created by mohmann on 2/8/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    final private static String TAG = "DatabaseHelper";

    final private static String TEXT_TYPE = " TEXT";
    final private static String INTEGER_TYPE = " INTEGER";
    final private static String COMMA_SEP = ",";

    private static final String SQL_CREATE_TODOS =
            "CREATE TABLE " + Todo.TABLE_NAME + " (" +
                    Todo.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    Todo.COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                    Todo.COLUMN_CONTENT + TEXT_TYPE + COMMA_SEP +
                    Todo.COLUMN_CREATION_DATE + INTEGER_TYPE + COMMA_SEP +
                    Todo.COLUMN_FINISH_DATE + INTEGER_TYPE + COMMA_SEP +
                    Todo.COLUMN_DUE_DATE + INTEGER_TYPE + COMMA_SEP +
                    Todo.COLUMN_DONE + INTEGER_TYPE + COMMA_SEP +
                    Todo.COLUMN_NOTIFIED + INTEGER_TYPE + " )";

    final private static String SQL_DELETE_TODOS =
            "DROP TABLE IF EXISTS " + Todo.TABLE_NAME;

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TODOS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* db.execSQL(SQL_DELETE_TODOS);
        onCreate(db); */
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private ContentValues getTodoContentValues(Todo todo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Todo.COLUMN_TITLE, todo.getTitle());
        contentValues.put(Todo.COLUMN_CONTENT, todo.getContent());
        contentValues.put(Todo.COLUMN_CREATION_DATE, todo.getCreationDate());
        contentValues.put(Todo.COLUMN_FINISH_DATE, todo.getFinishDate());
        contentValues.put(Todo.COLUMN_DUE_DATE, todo.getDueDate());
        contentValues.put(Todo.COLUMN_DONE, todo.isDone());
        contentValues.put(Todo.COLUMN_NOTIFIED, todo.isNotified());
        return contentValues;
    }

    private long insertTodo(Todo todo) {
        Log.d(TAG, "inserting " + todo.toString());
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(Todo.TABLE_NAME, null, getTodoContentValues(todo));
        todo.setId(id);
        return id;
    }

    private long updateTodo(final Todo todo) {
        Log.d(TAG, "updating: " + todo.toString());
        SQLiteDatabase db = getWritableDatabase();
        db.update(Todo.TABLE_NAME, getTodoContentValues(todo), "id = ? ",
                new String[]{Long.toString(todo.getId())});
        return todo.getId();
    }

    public long saveTodo(final Todo todo) {
        if (todo == null)
            return -1;

        if (todo.getId() == Todo.ID_UNSET) {
            return insertTodo(todo);
        }
        return updateTodo(todo);
    }

    public long deleteTodo(final Todo todo) {
        Log.d(TAG, "deleting: " + todo.toString());
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(Todo.TABLE_NAME, "id = ? ", new String[]{Long.toString(todo.getId())});
    }

    public Todo getTodo(final int id) {
        Log.d(TAG, "loading todo with id: " + id);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Todo.TABLE_NAME + "WHERE id = ?",
                new String[]{Integer.toString(id)});
        cursor.moveToFirst();

        return createTodoFromCursor(cursor);
    }

    public List<Todo> getTodos() {
        Log.d(TAG, "loading todos");
        SQLiteDatabase db = getReadableDatabase();
        List<Todo> todos = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Todo.TABLE_NAME, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            todos.add(createTodoFromCursor(cursor));
            cursor.moveToNext();
        }

        return todos;
    }

    public long getTodoCount() {
        SQLiteDatabase db = getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, Todo.TABLE_NAME);
    }

    private Todo createTodoFromCursor(Cursor cursor) {
        Todo todo = new Todo(getString(cursor, Todo.COLUMN_TITLE),
                getString(cursor, Todo.COLUMN_CONTENT));
        todo.setId(getLong(cursor, Todo.COLUMN_ID));
        todo.setCreationDate(getLong(cursor, Todo.COLUMN_CREATION_DATE));
        todo.setFinishDate(getLong(cursor, Todo.COLUMN_FINISH_DATE));
        todo.setDueDate(getLong(cursor, Todo.COLUMN_DUE_DATE));
        todo.setDone(getBoolean(cursor, Todo.COLUMN_DONE));
        todo.setNotified(getBoolean(cursor, Todo.COLUMN_NOTIFIED));
        return todo;
    }

    private String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private long getLong(Cursor cursor, String columnName) {
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }

    private boolean getBoolean(Cursor cursor, String columnName) {
        long value = cursor.getLong(cursor.getColumnIndex(columnName));
        return (value != 0);
    }
}
