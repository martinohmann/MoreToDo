package de.mohmann.moretodo.data;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mohmann on 2/3/16.
 */
public class TodoStore {

    final public static String TAG = "TodoStore";
    final private static String PREFS_NAME = "todos";

    final private static Type LIST_TYPE = new TypeToken<List<Todo>>() {}.getType();

    private static TodoStore instance = new TodoStore();
    private List<Todo> mTodoList = new ArrayList<>();

    private SharedPreferences mPrefs = null;

    private Comparator<Todo> mObjComparator = new Comparator<Todo>() {
        public int compare(Todo t1, Todo t2) {
            return t2.compareTo(t1);
        }
    };

    private TodoStore() {}

    public static TodoStore getInstance() {
        return instance;
    }

    public void add(Todo todo) {
        mTodoList.add(todo);
    }

    public void remove(int position) {
        mTodoList.remove(position);
    }

    public void remove(Todo todo) {
        mTodoList.remove(todo);
    }

    public void removeById(final String id) {
        for (Iterator<Todo> iterator = mTodoList.iterator(); iterator.hasNext();) {
            Todo todo = iterator.next();
            if (todo.getId().equals(id)) {
                iterator.remove();
                return;
            }
        }
    }

    public void removeDone() {
        for (Iterator<Todo> iterator = mTodoList.iterator(); iterator.hasNext();) {
            Todo todo = iterator.next();
            if (todo.isDone()) {
                iterator.remove();
            }
        }
    }

    public Todo get(int position) {
        return mTodoList.get(position);
    }

    public boolean contains(Todo todo) {
        if (todo == null)
            return false;

        for (Todo item : mTodoList) {
            if (item.getId().equals(todo.getId()))
                return true;
        }
        return false;
    }

    public Todo getById(String id) {
        for (Todo item : mTodoList) {
            if (item.getId().equals(id))
                return item;
        }
        return null;
    }

    public List<Todo> getList() {
        return mTodoList;
    }

    public void setPreferences(SharedPreferences prefs) {
        mPrefs = prefs;
    }

    public void persist() {
        if (mPrefs == null) {
            Log.w(TAG, "preferences not available, not saving todos");
            return;
        }

        Log.i(TAG, String.format("saving %d todos", mTodoList.size()));
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREFS_NAME, new Gson().toJson(mTodoList));
        editor.apply();
    }

    public void load() {
        if (mPrefs == null) {
            Log.w(TAG, "preferences not available, not loading todos");
            return;
        }


        mTodoList = new Gson().fromJson(mPrefs.getString(PREFS_NAME, null), LIST_TYPE);

        /* if there are no todos persisted yet */
        if (mTodoList == null)
            mTodoList = new ArrayList<>();

        Log.i(TAG, String.format("loaded %d todos", mTodoList.size()));
    }

    public void sort() {
        Collections.sort(mTodoList, mObjComparator);
    }
}
