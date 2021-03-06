package de.mohmann.moretodo.activities;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.adapters.TodoListAdapter;
import de.mohmann.moretodo.adapters.ViewPagerAdapter;
import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.fragments.TodoListFragment;
import de.mohmann.moretodo.services.BackgroundService;
import de.mohmann.moretodo.util.ServiceUtils;
import de.mohmann.moretodo.util.Utils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        DialogInterface.OnClickListener, ViewPager.OnPageChangeListener,
        TodoStore.OnTodoListFilterListener {

    final public static String TAG = "MainActivity";

    final private int[][] mTabOptions = {
            /* icon, label, list type */
            { R.drawable.ic_list_white_18dp, R.string.label_tab_all, TodoListAdapter.LIST_ALL },
            { R.drawable.ic_schedule_white_18dp, R.string.label_tab_pending, TodoListAdapter.LIST_PENDING },
            { R.drawable.ic_done_white_18dp, R.string.label_tab_done, TodoListAdapter.LIST_DONE }
    };

    private AlertDialog mAboutDialog;
    private AlertDialog mDeleteDialog;
    private FrameLayout mSearchFilter;
    private TabLayout mTabLayout;
    private TextView mSearchFilterText;
    private TodoStore mTodoStore;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTodoStore = TodoStore.getInstance(this);
        mTodoStore.addOnTodoListFilterListener(this);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        final ImageView searchFilterClear = (ImageView) findViewById(R.id.search_filter_clear);
        searchFilterClear.setOnClickListener(this);

        mSearchFilter = (FrameLayout) findViewById(R.id.search_filter);
        mSearchFilterText = (TextView) findViewById(R.id.search_filter_text);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(this);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        setupTabIcons();
        setTabAlpha(0);

        buildAboutDialog();
        buildDeleteDialog();

        /* start background service */
        if (!ServiceUtils.isServiceRunning(this, BackgroundService.class)) {
            Log.d(TAG, "starting BackgroundService");
            startService(new Intent(this, BackgroundService.class));
        }

        /* handle search intent */
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mTodoStore.filterBy(intent.getStringExtra(SearchManager.QUERY));
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onTodoListFilter(String filterString, boolean enabled) {
        if (enabled) {
            mSearchFilterText.setText(filterString);
            mSearchFilter.setVisibility(View.VISIBLE);
            Utils.setMargins(mViewPager, 0, mSearchFilter.getHeight(), 0, 0);
        } else {
            Utils.setMargins(mViewPager, 0, 0, 0, 0);
            mSearchFilter.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }
    }

    private void setTabAlpha(int position) {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab == null)
                continue;
            View view = tab.getCustomView();
            if (view == null)
                continue;
            view.setAlpha(i == position ? 1f : 0.8f);
        }
    }

    private void setupTabIcons() {
        TextView tabView;
        TabLayout.Tab tab;

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            tabView = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            tabView.setText(mTabOptions[i][1]);
            tabView.setCompoundDrawablesWithIntrinsicBounds(mTabOptions[i][0], 0, 0, 0);
            tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(tabView);
            }
        }
    }

    private void setupViewPager(final ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        final Resources res = getResources();
        for (int[] options : mTabOptions) {
            adapter.addFragment(TodoListFragment.newInstance(options[2]),
                    res.getString(options[1]));
        }
        viewPager.setAdapter(adapter);
    }

    private void buildAboutDialog() {
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setPositiveButton(R.string.button_label_close, this);
        builder.setView(messageView);
        mAboutDialog = builder.create();
    }

    private void buildDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_content_delete_items);
        builder.setTitle(R.string.dialog_title_delete_items);
        builder.setPositiveButton(R.string.button_label_yes, this);
        builder.setNegativeButton(R.string.button_label_cancel, this);
        mDeleteDialog = builder.create();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        /* unused */
    }

    @Override
    public void onPageSelected(int position) {
        setTabAlpha(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        /* unused */
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            Intent intent = new Intent(this, EditActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } else if (view.getId() == R.id.search_filter_clear) {
            mTodoStore.clearFilter();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        if (dialog == mDeleteDialog) {
            if (id == DialogInterface.BUTTON_POSITIVE) {
                mTodoStore.removeDone();
                Utils.toast(this, R.string.message_todos_deleted);
            } else {
                dialog.dismiss();
            }
        } else if (dialog == mAboutDialog) {
            dialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else if (id == R.id.action_clear_done) {
            mDeleteDialog.show();
        } else if (id == R.id.action_about) {
            mAboutDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTodoStore.removeOnTodoListFilterListener(this);
    }
}
