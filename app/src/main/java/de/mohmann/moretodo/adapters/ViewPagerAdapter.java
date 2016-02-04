package de.mohmann.moretodo.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.fragments.TodoListFragment;

/**
 * Created by mohmann on 2/4/16.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter
        implements TodoStore.OnTodoListUpdateListener {

    final public static String TAG = "ViewPagerAdapter";

    final private List<TodoListFragment> mFragmentList = new ArrayList<>();
    final private List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public TodoListFragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(TodoListFragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public void updateFragments() {
        for (int i = 0; i < mFragmentList.size(); i++) {
            mFragmentList.get(i).updateViews();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    @Override
    public void onTodoListUpdate() {
        Log.d(TAG, "onTodoListUpdate");
        updateFragments();
    }
}