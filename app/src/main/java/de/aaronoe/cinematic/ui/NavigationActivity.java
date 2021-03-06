package de.aaronoe.cinematic.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.aaronoe.cinematic.R;
import de.aaronoe.cinematic.ui.favorites.FavoritesActivity;
import de.aaronoe.cinematic.ui.login.LoginActivity;
import de.aaronoe.cinematic.ui.search.SearchMoviesActivity;

public class NavigationActivity extends AppCompatActivity {

    @Nullable
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Nullable
    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.frame)
    FrameLayout frame;

    private static final int MOVIES_SELECTION = 940;
    private static final int SHOWS_SELECTION = 499;
    private int mCurrentSelection;
    private static final String TAG = "NavigationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_menu__activity);

        ButterKnife.bind(this);

        // Setting toolbar as the actionbar
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            // Get last state
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            mCurrentSelection = sharedPref.getInt(getString(R.string.key_nav_selection), MOVIES_SELECTION);
            Log.d(TAG, "onCreate: " + mCurrentSelection);
            switch (mCurrentSelection) {
                case MOVIES_SELECTION:
                    navigationView.setCheckedItem(R.id.drawer_menu_movies);
                    selectMovieView();
                    break;
                case SHOWS_SELECTION:
                    navigationView.setCheckedItem(R.id.drawer_menu_shows);
                    selectTvView();
                    break;
                default:
                    navigationView.setCheckedItem(R.id.drawer_menu_movies);
                    selectMovieView();
                    break;
            }
        }

        // set up navigation
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //Checking if the item is in checked state or not, if not make it in checked state
                item.setChecked(!item.isChecked());

                //Closing drawer on item click
                if (drawerLayout != null) {
                    drawerLayout.closeDrawers();
                }

                switch (item.getItemId()) {

                    case R.id.drawer_menu_movies:
                        selectMovieView();
                        return true;

                    case R.id.drawer_menu_shows:
                        selectTvView();
                        return true;

                    case R.id.drawer_menu_faves:
                        startActivity(new Intent(NavigationActivity.this, FavoritesActivity.class));
                        return true;

                    case R.id.drawer_menu_search:
                        startActivity(new Intent(NavigationActivity.this, SearchMoviesActivity.class));
                        return true;

                    /*
                    case R.id.drawer_menu_account:
                        startActivity(new Intent(NavigationActivity.this, LoginActivity.class));
                        return true;
                        */

                    case R.id.drawer_menu_info:
                        new LibsBuilder()
                                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                .withActivityTitle("About & Licenses")
                                .withAboutAppName(getString(R.string.app_name))
                                .withAboutIconShown(true)
                                .withAboutDescription("<br /> Developer: <br /> <h1>Aaron Oertel</h1> Data from the TMDb API <br /><br /> All copyrights belong to their respective owners <br /><br /> External libraries used:")
                                .withAboutVersionShown(true)
                                .start(NavigationActivity.this);
                        return true;
                    default:
                        Toast.makeText(NavigationActivity.this, "Something went terribly wrong", Toast.LENGTH_SHORT).show();
                        return true;

                }

            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle
                (this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
        }
        actionBarDrawerToggle.syncState();

    }


    @Override
    protected void onStart() {
        super.onStart();

        // Get last state
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        mCurrentSelection = sharedPref.getInt(getString(R.string.key_nav_selection), MOVIES_SELECTION);
        Log.d(TAG, "onStart: " + mCurrentSelection);
        switch (mCurrentSelection) {
            case MOVIES_SELECTION:
                navigationView.setCheckedItem(R.id.drawer_menu_movies);
                if (toolbar != null) {
                    toolbar.setTitle("Movies");
                }
                break;
            case SHOWS_SELECTION:
                navigationView.setCheckedItem(R.id.drawer_menu_shows);
                if (toolbar != null) {
                    toolbar.setTitle("TV Shows");
                }
                break;
            default:
                navigationView.setCheckedItem(R.id.drawer_menu_movies);
                break;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getString(R.string.key_nav_selection), mCurrentSelection);
        saveSelection();
        Log.d(TAG, "onSaveInstanceState: "+ mCurrentSelection);
    }


    private void saveSelection() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.key_nav_selection), mCurrentSelection);
        editor.apply();
    }


    private void selectMovieView() {
        mCurrentSelection = MOVIES_SELECTION;
        MoviesFragment moviesFragment = new MoviesFragment();
        FragmentTransaction moviesFragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        moviesFragmentTransaction.replace(R.id.frame, moviesFragment);
        moviesFragmentTransaction.commit();
        if (toolbar != null) {
            toolbar.setTitle("Movies");
        }
    }

    private void selectTvView() {
        mCurrentSelection = SHOWS_SELECTION;
        TvShowsFragment tvShowsFragment = new TvShowsFragment();
        FragmentTransaction showsFragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        showsFragmentTransaction.replace(R.id.frame, tvShowsFragment);
        showsFragmentTransaction.commit();
        if (toolbar != null) {
            toolbar.setTitle("TV Shows");
        }
    }

}
