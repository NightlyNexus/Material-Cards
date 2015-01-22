package com.brianco.materialcards;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.brianco.materialcards.adapter.DrawerAdapter;
import com.brianco.materialcards.model.PaletteColor;
import com.brianco.materialcards.model.PaletteColorSection;

import java.util.ArrayList;

public class PaletteActivity extends ActionBarActivity {

    public static final String ACTION_START_COLOR
            = "com.brianco.materialcards.PaletteActivity.ACTION_START_COLOR";
    public static final String COLOR_SECTION_VALUE_EXTRA = "COLOR_SECTION_VALUE_EXTRA";

    private static final String FIRST_RUN = "FIRST_RUN";
    private static final String DARK_THEME = "DARK_THEME";
    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private static final String COLOR_LIST_KEY = "COLOR_LIST_KEY";
    private static final String POSITION_KEY = "POSITION_KEY";

    private SharedPreferences mPrefs;
    private boolean mDark;
    private MenuItem mChangeThemeMenuItem;
    private PaletteFragment mFragment;
    private ArrayList<PaletteColorSection> mColorList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private Toolbar mToolBar;
    private View mDrawerView;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mPosition;
    private int[] mColorSectionsValues;

    private final ListView.OnItemClickListener drawerClickListener
            = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(final AdapterView parent, final View view,
                                final int position, final long id) {
            selectItem(position);
        }
    };

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        mToolBar.setTitle(mTitle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    // returns the sectionIndex
    private int handleIntent(final Intent intent) {
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            // Activity launched from history
            return -1;
        }
        if (ACTION_START_COLOR.equals(intent.getAction())) {
            final int sectionValue = intent.getIntExtra(COLOR_SECTION_VALUE_EXTRA, -1);
            return findIndex(mColorSectionsValues, sectionValue);
        }
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_palette);

        mDrawerTitle = getTitle();

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerView = findViewById(R.id.navigation_drawer);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer_list);

        mDrawerList.setPadding(0, getStatusBarHeight(), 0, 0);
        mDrawerList.setClipToPadding(false);

        setSupportActionBar(mToolBar);

        final String[] colorSectionsNames
                = getResources().getStringArray(R.array.color_sections_names);
        mColorSectionsValues
                = getResources().getIntArray(R.array.color_sections_colors);

        if (savedInstanceState != null) {
            mColorList = savedInstanceState.getParcelableArrayList(COLOR_LIST_KEY);
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        } else {
            mColorList = PaletteColorSection.getPaletteColorSectionsList(colorSectionsNames,
                    mColorSectionsValues, getBaseColorNames(colorSectionsNames),
                    getColorValues(colorSectionsNames));
            mPosition = handleIntent(getIntent());
            if (mPosition < 0) {
                mPosition = 0;
            }
        }
        mFragment = (PaletteFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (mFragment == null) {
            mFragment = new PaletteFragment();
            final Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(PaletteFragment.ARG_COLORS,
                    mColorList.get(mPosition).getPaletteColorList());
            mFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.container, mFragment,
                    FRAGMENT_TAG).commit();
        }

        setupNavigationDrawer();

        selectItemActivityUi(mColorList.get(mPosition));

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean firstRun
                = mPrefs.getBoolean(FIRST_RUN, true);
        if (firstRun) {
            mPrefs.edit().putBoolean(FIRST_RUN, false).apply();
            mDrawerLayout.openDrawer(mDrawerView);
        }
        final boolean darkTheme = mPrefs.getBoolean(DARK_THEME, false);
        setThemeDark(darkTheme);
    }

    private void setupNavigationDrawer() {
        mDrawerList.setAdapter(new DrawerAdapter(this, mColorList));
        mDrawerList.setOnItemClickListener(drawerClickListener);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                doDrawerClosed();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                doDrawerOpened();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // disable hamburger-arrow animation
                super.onDrawerSlide(drawerView, 0);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void doDrawerClosed() {
        mToolBar.setTitle(mTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
    }

    private void doDrawerOpened() {
        mToolBar.setTitle(mDrawerTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
    }

    private void selectItem(final int position) {
        final PaletteColorSection paletteColorSection = mColorList.get(position);
        final ArrayList<PaletteColor> colors = paletteColorSection.getPaletteColorList();
        if (mPosition == position) {
            mFragment.scrollToTop();
        } else {
            mPosition = position;
            mFragment.replaceColorCardList(colors);
        }
        selectItemActivityUi(paletteColorSection);
        mDrawerLayout.closeDrawer(mDrawerView);
    }

    private void selectItemActivityUi(final PaletteColorSection paletteColorSection) {
        final String sectionName = paletteColorSection.getColorSectionName();
        final int sectionValue = paletteColorSection.getColorSectionValue();
        mDrawerList.setItemChecked(mPosition, true);
        setTitle(sectionName);
        mToolBar.setBackgroundColor(sectionValue);
        final int darkenedColor = getDarkenedColor(sectionValue);
        mDrawerLayout.setStatusBarBackgroundColor(darkenedColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final ActivityManager.TaskDescription taskDescription
                    = new ActivityManager.TaskDescription(getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_launcher), sectionValue);
            setTaskDescription(taskDescription);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();

        if (mDrawerLayout.isDrawerOpen(mDrawerView)) {
            doDrawerOpened();
        } else {
            doDrawerClosed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.change_theme:
                setThemeDark(!mDark);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.palette, menu);
        mChangeThemeMenuItem = menu.findItem(R.id.change_theme);
        setMenuItemDark(mDark);
        return true;
    }

    /* Called on invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        final boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerView);
        mChangeThemeMenuItem.setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerView)) {
            mDrawerLayout.closeDrawer(mDrawerView);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(COLOR_LIST_KEY, mColorList);
        outState.putInt(POSITION_KEY, mPosition);
        super.onSaveInstanceState(outState);
    }

    private void setThemeDark(final boolean dark) {
        mDark = dark;
        final int colorResource = dark ? R.color.pure_grey : R.color.pure_white;
        getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(colorResource)));
        setMenuItemDark(mDark);
        mPrefs.edit().putBoolean(DARK_THEME, mDark).apply();
    }

    private void setMenuItemDark(final boolean dark) {
        if (mChangeThemeMenuItem == null) {
            return;
        }
        final int stringResource = dark ? R.string.go_light : R.string.go_dark;
        mChangeThemeMenuItem.setTitle(stringResource);
        final int colorResource = dark ? R.color.pure_white : R.color.pure_grey;
        final Drawable drawable = getResources().getDrawable(R.drawable.dot);
        drawable.setColorFilter(getResources().getColor(colorResource), PorterDuff.Mode.SRC);
        mChangeThemeMenuItem.setIcon(drawable);
    }

    private int getStatusBarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int resourceId = getResources()
                    .getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return getResources().getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    private static int getDarkenedColor(final int color) {
        final float darkenRatio = 0.7f;
        return Color.argb(Color.alpha(color), (int) (Color.red(color) * darkenRatio),
                (int) (Color.green(color) * darkenRatio), (int) (Color.blue(color) * darkenRatio));
    }

    private String[][] getBaseColorNames(String[] colorSectionsNames) {
        String[][] strArr = new String[colorSectionsNames.length][];
        for (int i = 0; i < colorSectionsNames.length; i++) {
            strArr[i] = getBaseColorNames(colorSectionsNames[i]);
        }
        return strArr;
    }

    private int[][] getColorValues(String[] colorSectionsNames) {
        int[][] intArr = new int[colorSectionsNames.length][];
        for (int i = 0; i < colorSectionsNames.length; i++) {
            intArr[i] = getColorValues(colorSectionsNames[i]);
        }
        return intArr;
    }

    private String[] getBaseColorNames(String colorSectionName) {
        Resources res = getResources();
        if (getString(R.string.red).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.pink).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.purple).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.deep_purple).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.indigo).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.blue).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.light_blue).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.cyan).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.teal).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.green).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.light_green).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.lime).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.yellow).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.amber).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.orange).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.deep_orange).equals(colorSectionName)) {
            return res.getStringArray(R.array.full_base_color_list);
        } else if (getString(R.string.brown).equals(colorSectionName)) {
            return res.getStringArray(R.array.base_color_list_to_900);
        } else if (getString(R.string.grey).equals(colorSectionName)) {
            return res.getStringArray(R.array.base_color_list_greys);
        } else if (getString(R.string.blue_grey).equals(colorSectionName)) {
            return res.getStringArray(R.array.base_color_list_to_900);
        } else {
            throw new RuntimeException("Invalid color section: " + colorSectionName);
        }
    }

    private int[] getColorValues(String colorSectionName) {Resources res = getResources();
        if (getString(R.string.red).equals(colorSectionName)) {
            return res.getIntArray(R.array.reds);
        } else if (getString(R.string.pink).equals(colorSectionName)) {
            return res.getIntArray(R.array.pinks);
        } else if (getString(R.string.purple).equals(colorSectionName)) {
            return res.getIntArray(R.array.purples);
        } else if (getString(R.string.deep_purple).equals(colorSectionName)) {
            return res.getIntArray(R.array.deep_purples);
        } else if (getString(R.string.indigo).equals(colorSectionName)) {
            return res.getIntArray(R.array.indigos);
        } else if (getString(R.string.blue).equals(colorSectionName)) {
            return res.getIntArray(R.array.blues);
        } else if (getString(R.string.light_blue).equals(colorSectionName)) {
            return res.getIntArray(R.array.light_blues);
        } else if (getString(R.string.cyan).equals(colorSectionName)) {
            return res.getIntArray(R.array.cyans);
        } else if (getString(R.string.teal).equals(colorSectionName)) {
            return res.getIntArray(R.array.teals);
        } else if (getString(R.string.green).equals(colorSectionName)) {
            return res.getIntArray(R.array.greens);
        } else if (getString(R.string.light_green).equals(colorSectionName)) {
            return res.getIntArray(R.array.light_greens);
        } else if (getString(R.string.lime).equals(colorSectionName)) {
            return res.getIntArray(R.array.limes);
        } else if (getString(R.string.yellow).equals(colorSectionName)) {
            return res.getIntArray(R.array.yellows);
        } else if (getString(R.string.amber).equals(colorSectionName)) {
            return res.getIntArray(R.array.ambers);
        } else if (getString(R.string.orange).equals(colorSectionName)) {
            return res.getIntArray(R.array.oranges);
        } else if (getString(R.string.deep_orange).equals(colorSectionName)) {
            return res.getIntArray(R.array.deep_oranges);
        } else if (getString(R.string.brown).equals(colorSectionName)) {
            return res.getIntArray(R.array.browns);
        } else if (getString(R.string.grey).equals(colorSectionName)) {
            return res.getIntArray(R.array.greys);
        } else if (getString(R.string.blue_grey).equals(colorSectionName)) {
            return res.getIntArray(R.array.blue_greys);
        } else {
            throw new RuntimeException("Invalid color section: " + colorSectionName);
        }
    }

    private static int findIndex(int[] hackstack, int needle) {
        for (int i = 0; i < hackstack.length; i++) {
            if (hackstack[i] == needle) {
                return i;
            }
        }
        return -1;
    }
}
