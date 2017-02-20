package de.aaronoe.popularmovies.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.aaronoe.popularmovies.Data.MovieAdapter;
import de.aaronoe.popularmovies.Database.MoviesContract.MovieEntry;
import de.aaronoe.popularmovies.MainActivity;
import de.aaronoe.popularmovies.Movies.MovieItem;

import static android.content.ContentValues.TAG;

/**
 *
 * Created by aaronoe on 20.02.17.
 */

public class Utilities {

    /**
     * This function takes the required fields of a {@link MovieItem} and puts them into
     * a {@link ContentValues} object to use the {@link MoviesContentProvider} to insert a
     * movie into the underlying Database ({@link MoviesDbHelper})
     *
     * @param movieItem a single MovieItem
     * @return a {@link ContentValues} object containing the required information to put the movie into the db
     */
    public static ContentValues getContentValuesForMovie(MovieItem movieItem) {

        ContentValues cv = new ContentValues();

        cv.put(MovieEntry.COLUMN_POSTER_PATH, movieItem.getmPosterPath());
        cv.put(MovieEntry.COLUMN_DESCRIPTION, movieItem.getmMovieDescription());
        cv.put(MovieEntry.COLUMN_TITLE, movieItem.getmTitle());
        cv.put(MovieEntry.COLUMN_MOVIE_ID, movieItem.getmMovieId());
        cv.put(MovieEntry.COLUMN_RELEASE_DATE, movieItem.getmReleaseDate());
        cv.put(MovieEntry.COLUMN_VOTE_AVERAGE, movieItem.getmVoteAverage());
        cv.put(MovieEntry.COLUMN_BACKDROP_PATH, movieItem.getmBackdropPath());

        return cv;

    }

    /**
     * Takes a Cursor as it's input and returns a {@link List<MovieItem>} object to pass into the
     * {@link MovieAdapter} to populate the corresponding RecyclerView in {@link MainActivity}
     *
     * @param movieCursor containing a user's favorite movie
     * @return a {@link List<MovieItem>} containing movie information
     */
    public static List<MovieItem> extractMovieItemFromCursor(Cursor movieCursor) {

        Log.d(TAG, "extract movies called");

        if (movieCursor == null || movieCursor.getCount() == 0) return null;

        Log.d(TAG, "starts extracting");

        List<MovieItem> resultList = new ArrayList<>();

        int movieIdIndex = movieCursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID);
        int movieTitleIndex = movieCursor.getColumnIndex(MovieEntry.COLUMN_TITLE);
        int movieDescriptionIndex = movieCursor.getColumnIndex(MovieEntry.COLUMN_DESCRIPTION);
        int moviePosterPathIndex = movieCursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH);
        int movieReleaseDateIndex = movieCursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE);
        int movieVoteAverageIndex = movieCursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE);
        int movieBackdropIndex = movieCursor.getColumnIndex(MovieEntry.COLUMN_BACKDROP_PATH);

        try {

            while (movieCursor.moveToNext()) {

                Log.d(TAG, "cursor moved");

                String posterPath = movieCursor.getString(moviePosterPathIndex);
                String movieDescription = movieCursor.getString(movieDescriptionIndex);
                String movieTitle = movieCursor.getString(movieTitleIndex);
                int movieId = movieCursor.getInt(movieIdIndex);
                String releaseDate = movieCursor.getString(movieReleaseDateIndex);
                Double voteAverage = movieCursor.getDouble(movieVoteAverageIndex);
                String backdropPath = movieCursor.getColumnName(movieBackdropIndex);

                MovieItem currentMovie =
                        new MovieItem(posterPath, movieDescription, movieTitle,
                                movieId, releaseDate, voteAverage, backdropPath);

                Log.d(Utilities.class.getSimpleName(), "Movie queried: " + movieTitle);

                resultList.add(currentMovie);

            }

        } finally {
            movieCursor.close();
        }

        return resultList;
    }

}
