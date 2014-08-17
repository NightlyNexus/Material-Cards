package com.brianco.materialcards;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.brianco.materialcards.adapter.DrawerAdapter;
import com.brianco.materialcards.model.PaletteColor;
import com.brianco.materialcards.model.PaletteColorSection;

import java.util.ArrayList;

public class PaletteActivity extends Activity {

    public static final String ACTION_START_COLOR
            = "com.brianco.materialcards.PaletteActivity.ACTION_START_COLOR";
    public static final String COLOR_SECTION_VALUE_EXTRA = "COLOR_SECTION_VALUE_EXTRA";

    private static final String FIRST_RUN = "FIRST_RUN";
    private static final String DARK_THEME = "DARK_THEME";
    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private static final String FRAGMENT_KEY = "FRAGMENT_KEY";
    private static final String COLOR_LIST_KEY = "COLOR_LIST_KEY";
    private static final String POSITION_KEY = "POSITION_KEY";
    private static final String DRAWER_TITLE_KEY = "DRAWER_TITLE_KEY";
    private static final String TITLE_KEY = "TITLE_KEY";

    private SharedPreferences mPrefs;
    private boolean mDark;
    private MenuItem mChangeThemeMenuItem = null;
    private PaletteFragment mFragment = null;
    private ArrayList<PaletteColorSection> mColorList = null;
    private CharSequence mDrawerTitle = null;
    private CharSequence mTitle = null;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mPosition = 0;
    private int[] mColorSecionsValues;

    private final ListView.OnItemClickListener drawerClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(final AdapterView parent, final View view,
                                final int position, final long id) {
            selectItem(position, null);
        }
    };

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private boolean handleIntent(final Intent intent) {
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            // Activity launched from history
            return false;
        }
        if (ACTION_START_COLOR.equals(intent.getAction())) {
            final int sectionValue = intent.getIntExtra(COLOR_SECTION_VALUE_EXTRA, 0);
            final int sectionIndex = findIndex(mColorSecionsValues, sectionValue);
            selectItem(sectionIndex, null);
            intent.setAction(Intent.ACTION_DEFAULT);
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_palette);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        final String[] colorSectionsNames
                = getResources().getStringArray(R.array.color_sections_names);
        mColorSecionsValues
                = getResources().getIntArray(R.array.color_sections_colors);

        if (savedInstanceState != null) {
            mFragment = (PaletteFragment) getFragmentManager()
                    .getFragment(savedInstanceState, FRAGMENT_KEY);
            mColorList = savedInstanceState.getParcelableArrayList(COLOR_LIST_KEY);
            mPosition = savedInstanceState.getInt(POSITION_KEY);
            mDrawerTitle = savedInstanceState.getCharSequence(DRAWER_TITLE_KEY);
            mTitle = savedInstanceState.getCharSequence(TITLE_KEY);
        }
        if (mColorList == null) {
            mColorList = PaletteColorSection.getPaletteColorSectionsList(colorSectionsNames,
                mColorSecionsValues, getBaseColorNames(colorSectionsNames),
                    getColorValues(colorSectionsNames));
        }
        if (mDrawerTitle == null) {
            mDrawerTitle = getTitle();
        }
        if (mTitle == null) {
            mTitle = getTitle();
        }

        setupNavigationDrawer();

        if (!handleIntent(getIntent())) {
            selectItem(mPosition, mFragment);
        }

        mPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        final boolean firstRun
                = mPrefs.getBoolean(FIRST_RUN, true);
        if (firstRun) {
            mPrefs.edit().putBoolean(FIRST_RUN, false).apply();
            mDrawerLayout.openDrawer(mDrawerList);
        }
        final boolean darkTheme = mPrefs.getBoolean(DARK_THEME, false);
        setThemeDark(darkTheme);
    }

    private void setupNavigationDrawer() {
        mDrawerList.setAdapter(new DrawerAdapter(this, mColorList));
        mDrawerList.setOnItemClickListener(drawerClickListener);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                doDrawerClosed();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                doDrawerOpened();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void doDrawerClosed() {
        getActionBar().setTitle(mTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
    }

    private void doDrawerOpened() {
        getActionBar().setTitle(mDrawerTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
    }

    private void selectItem(final int position, final PaletteFragment fragment) {
        final PaletteColorSection paletteColorSection = mColorList.get(position);
        final ArrayList<PaletteColor> colors = paletteColorSection.getPaletteColorList();
        final String sectionName = paletteColorSection.getColorSectionName();
        final int sectionValue = paletteColorSection.getColorSectionValue();
        if (mPosition == position && mFragment != null && mFragment.isColorsAdded()) {
            mFragment.scrollToTop();
        } else if (mFragment != null && mFragment.isColorsAdded()) {
            mPosition = position;
            mFragment.replaceColorCardList(colors);
        } else {
            mPosition = position;
            if (fragment == null) {
                final Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(PaletteFragment.ARG_COLORS, colors);
                mFragment = new PaletteFragment();
                mFragment.setArguments(bundle);
            } else {
                mFragment = fragment;
            }
            getFragmentManager().beginTransaction().replace(R.id.container, mFragment,
                    FRAGMENT_TAG).commit();
        }
        mDrawerList.setItemChecked(mPosition, true);
        setTitle(sectionName);
        getActionBar().setBackgroundDrawable(new ColorDrawable(sectionValue));
        final Window window = getWindow();
        final int darkenedColor = getDarkenedColor(sectionValue);
        window.setStatusBarColor(darkenedColor);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();

        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
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
        final boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        mChangeThemeMenuItem.setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        getFragmentManager().putFragment(outState, FRAGMENT_KEY, mFragment);
        outState.putParcelableArrayList(COLOR_LIST_KEY, mColorList);
        outState.putInt(POSITION_KEY, mPosition);
        outState.putCharSequence(DRAWER_TITLE_KEY, mDrawerTitle);
        outState.putCharSequence(TITLE_KEY, mTitle);
        super.onSaveInstanceState(outState);
    }

    private void setThemeDark(final boolean dark) {
        mDark = dark;
        final int colorResource = dark ? R.color.pure_black : R.color.pure_white;
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
        final int colorResource = dark ? R.color.pure_white : R.color.pure_black;
        final Drawable drawable = getResources().getDrawable(R.drawable.dot);
        drawable.setColorFilter(getResources().getColor(colorResource), PorterDuff.Mode.SRC);
        mChangeThemeMenuItem.setIcon(drawable);
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
