package de.aaronoe.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.aaronoe.popularmovies.Data.ApiClient;
import de.aaronoe.popularmovies.Data.ApiInterface;
import de.aaronoe.popularmovies.Data.MovieAdapter;
import de.aaronoe.popularmovies.Database.MoviesContract.MovieEntry;
import de.aaronoe.popularmovies.Database.Utilities;
import de.aaronoe.popularmovies.DetailPage.DetailActivity;
import de.aaronoe.popularmovies.Movies.MovieItem;
import de.aaronoe.popularmovies.Movies.MovieResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, MovieAdapter.MovieAdapterOnClickHandler {

    // for debugging purposes
    private static final String TAG = MainActivity.class.getSimpleName();

    public MovieAdapter mMovieAdapter;
    final String SELECTION_POPULAR = "popular";
    final String SELECTION_TOP_RATED = "top_rated";
    final String SELECTION_FAVORITES = "favorites";
    final String SELECTION_UPCOMING = "upcoming";
    final String SELECTION_SEARCH = "search";
    String mCurrentSelection;
    private final static String API_KEY = BuildConfig.MOVIE_DB_API_KEY;
    ApiInterface apiService;
    private static final int FAVORITE_LOADER_ID = 26;
    List<MovieItem> movieItemList;
    GridLayoutManager gridLayout;
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";
    private Parcelable mLayoutManagerSavedState;


    @BindView(R.id.fab_menu) FloatingActionMenu fabMenu;
    @BindView(R.id.fab_action_favorite) FloatingActionButton fabButtonFavorite;
    @BindView(R.id.fab_action_top_rated) FloatingActionButton fabButtonTopRated;
    @BindView(R.id.fab_action_popular) FloatingActionButton fabButtonPopular;
    @BindView(R.id.fab_action_upcoming) FloatingActionButton fabButtonUpcoming;
    @BindView(R.id.fab_action_search) FloatingActionButton fabButtonSearch;
    @BindView(R.id.rv_main_movie_list) RecyclerView mRecyclerView;
    @BindView(R.id.tv_error_message_display) TextView mErrorMessageDisplay;
    @BindView(R.id.pb_loading_indicator) ProgressBar mLoadingIndicator;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get last state
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mCurrentSelection = sharedPref.getString(getString(R.string.SAVE_SELECTION_KEY), SELECTION_POPULAR);

        ButterKnife.bind(this);

        gridLayout = new GridLayoutManager(MainActivity.this, calculateNoOfColumns(this));

        mRecyclerView.setLayoutManager(gridLayout);
        //mRecyclerView.hasFixedSize(true);
        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        initializeFabMenu();

        apiService = ApiClient.getClient().create(ApiInterface.class);

        if (mCurrentSelection.equals(SELECTION_FAVORITES)) {
            getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, this);
        } else if (mCurrentSelection.equals(SELECTION_SEARCH)) {
            selectSearch();
        } else {
            downloadMovieData();
        }
    }


    private void restorePosition() {
        if (mLayoutManagerSavedState != null) {
            gridLayout.onRestoreInstanceState(mLayoutManagerSavedState);
            mLayoutManagerSavedState = null;
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mLayoutManagerSavedState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }


    public void initializeFabMenu() {

        fabButtonFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFavorite();
                fabMenu.close(true);
            }
        });

        fabButtonPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPopular();
                fabMenu.close(true);
            }
        });

        fabButtonTopRated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTopRated();
                fabMenu.close(true);
            }
        });

        fabButtonUpcoming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectUpcoming();
                fabMenu.close(true);
            }
        });

        fabButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSearch();
                fabMenu.close(true);
            }
        });

    }


    private void downloadMovieData() {

        mLoadingIndicator.setVisibility(View.VISIBLE);

        Call<MovieResponse> call = apiService.getMovies(mCurrentSelection, API_KEY);

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                movieItemList = response.body().getResults();

                mLoadingIndicator.setVisibility(View.INVISIBLE);
                if (movieItemList != null) {
                    showMovieView();
                    mMovieAdapter.setMovieData(movieItemList);
                } else {
                    showErrorMessage();
                }
                restorePosition();
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                showErrorMessage();
            }
        });

    }


    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 180);
    }


    @Override
    public void onClick(MovieItem movieItem) {
        Intent intentToStartDetailActivity = new Intent(MainActivity.this, DetailActivity.class);
        intentToStartDetailActivity.putExtra("MovieItem", movieItem);
        startActivity(intentToStartDetailActivity);
    }

    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     *
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMovieView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }


    private void saveSelection(String selection) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.SAVE_SELECTION_KEY), selection);
        editor.apply();
    }


    /**
     * This method will make the error message visible and hide the weather
     * View.
     *
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


    private boolean selectPopular() {
        if (mCurrentSelection.equals(SELECTION_POPULAR)) {
            Toast.makeText(this, "This option is already selected", Toast.LENGTH_SHORT).show();
            return true;
        }

        mMovieAdapter.setMovieData(null);
        mCurrentSelection = SELECTION_POPULAR;
        downloadMovieData();
        saveSelection(SELECTION_POPULAR);
        return true;
    }

    private void selectSearch() {
        saveSelection(SELECTION_SEARCH);
        Intent intentToStartSearchActivity = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(intentToStartSearchActivity);
    }


    private boolean selectTopRated() {
        // return if the option is already selected
        if (mCurrentSelection.equals(SELECTION_TOP_RATED)) {
            Toast.makeText(this, "This option is already selected", Toast.LENGTH_SHORT).show();
            return true;
        }

        mMovieAdapter.setMovieData(null);
        mCurrentSelection = SELECTION_TOP_RATED;
        downloadMovieData();
        saveSelection(SELECTION_TOP_RATED);
        return true;
    }


    private boolean selectFavorite() {
        getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, this);

        mCurrentSelection = SELECTION_FAVORITES;
        mMovieAdapter.setMovieData(null);
        saveSelection(SELECTION_FAVORITES);
        return true;
    }


    private boolean selectUpcoming() {
        if (mCurrentSelection.equals(SELECTION_UPCOMING)) {
            Toast.makeText(this, "This option is already selected", Toast.LENGTH_SHORT).show();
            return true;
        }
        mMovieAdapter.setMovieData(null);
        mCurrentSelection = SELECTION_UPCOMING;
        downloadMovieData();
        saveSelection(SELECTION_UPCOMING);
        return true;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                this,
                MovieEntry.CONTENT_URI,
                null, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        movieItemList = Utilities.extractMovieItemFromCursor(data);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (movieItemList != null) {
            showMovieView();
            mMovieAdapter.setMovieData(movieItemList);
            mMovieAdapter.notifyDataSetChanged();
            restorePosition();
        } else {
            showErrorMessage();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.setMovieData(null);
        getSupportLoaderManager().restartLoader(0, null, this);
    }

}
