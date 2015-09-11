package com.markesilva.footballscores.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.markesilva.footballscores.data.DatabaseContract.scores_table;
import com.markesilva.footballscores.data.DatabaseContract.leagues_table;
import com.markesilva.footballscores.data.DatabaseContract.teams_table;
/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider {
    private static ScoresDBHelper mOpenHelper;
    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int LEAGUES = 200;
    private static final int LEAGUE_WITH_ID = 201;
    private static final int TEAMS = 300;
    private static final int TEAM_WITH_ID = 301;

    private UriMatcher muriMatcher = buildUriMatcher();
    private static final String SCORES_BY_LEAGUE = scores_table.TABLE_NAME + "." + scores_table.LEAGUE_COL + " = ?";
    private static final String SCORES_BY_DATE =   scores_table.TABLE_NAME + "." + scores_table.DATE_COL + " LIKE ?";
    private static final String SCORES_BY_ID =     scores_table.TABLE_NAME + "." + scores_table.MATCH_ID + " = ?";
    private static final String TEAM_BY_ID =       teams_table.TABLE_NAME + "." + teams_table.TEAM_ID_COL + " = ?";
    private static final String LEAGUE_BY_ID =     leagues_table.TABLE_NAME + "." + leagues_table.LEAGUE_ID_COL + " = ?";
    private static final SQLiteQueryBuilder sScoreQuery;
    static {
        sScoreQuery = new SQLiteQueryBuilder();
        sScoreQuery.setTables(
                scores_table.TABLE_NAME +
                        " INNER JOIN " + leagues_table.TABLE_NAME +
                        " ON " + scores_table.TABLE_NAME +
                        "." + scores_table.LEAGUE_COL +
                        " = " + leagues_table.TABLE_NAME +
                        "." + leagues_table.LEAGUE_ID_COL +
                        " INNER JOIN " + teams_table.TABLE_NAME +
                        " " + teams_table.HOME_QUERY_TABLE_NAME +
                        " ON " + scores_table.TABLE_NAME +
                        "." + scores_table.HOME_COL +
                        " = " + teams_table.HOME_QUERY_TABLE_NAME +
                        "." + teams_table.TEAM_ID_COL +
                        " INNER JOIN " + teams_table.TABLE_NAME +
                        " " + teams_table.AWAY_QUERY_TABLE_NAME +
                        " ON " + scores_table.TABLE_NAME +
                        "." + scores_table.AWAY_COL +
                        " = " + teams_table.AWAY_QUERY_TABLE_NAME +
                        "." + teams_table.TEAM_ID_COL
        );
    }


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, scores_table.PATH, MATCHES);
        matcher.addURI(authority, scores_table.PATH + "/" + scores_table.PATH_LEAGUE, MATCHES_WITH_LEAGUE);
        matcher.addURI(authority, scores_table.PATH + "/" + scores_table.PATH_ID, MATCHES_WITH_ID);
        matcher.addURI(authority, scores_table.PATH + "/" + scores_table.PATH_DATE + "/*", MATCHES_WITH_DATE);
        matcher.addURI(authority, teams_table.PATH, TEAMS);
        matcher.addURI(authority, teams_table.PATH + "/" + teams_table.PATH_ID + "/#", TEAM_WITH_ID);
        matcher.addURI(authority, leagues_table.PATH, LEAGUES);
        matcher.addURI(authority, leagues_table.PATH + "/" + leagues_table.PATH_ID + "/#", LEAGUE_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ScoresDBHelper(getContext());
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        final int match = muriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return scores_table.CONTENT_TYPE;
            case MATCHES_WITH_LEAGUE:
                return scores_table.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return scores_table.CONTENT_ITEM_TYPE;
            case MATCHES_WITH_DATE:
                return scores_table.CONTENT_TYPE;
            case TEAMS:
                return teams_table.CONTENT_TYPE;
            case TEAM_WITH_ID:
                return teams_table.CONTENT_ITEM_TYPE;
            case LEAGUES:
                return leagues_table.CONTENT_TYPE;
            case LEAGUE_WITH_ID:
                return leagues_table.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        //Log.v(FetchScoreTask.LOG_TAG,uri.getPathSegments().toString());
        //Log.v(FetchScoreTask.LOG_TAG,SCORES_BY_LEAGUE);
        //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[0]);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(match));
        switch (muriMatcher.match(uri)) {
            case MATCHES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        scores_table.TABLE_NAME,
                        projection, null, null, null, null, sortOrder);
                break;
            case MATCHES_WITH_DATE:
                String date = DatabaseContract.scores_table.getDateFromUri(uri);
                retCursor = sScoreQuery.query(mOpenHelper.getReadableDatabase(),
                        projection, SCORES_BY_DATE, new String[]{date}, null, null, sortOrder);
                break;
            case MATCHES_WITH_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        scores_table.TABLE_NAME,
                        projection, SCORES_BY_ID, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_LEAGUE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        scores_table.TABLE_NAME,
                        projection, SCORES_BY_LEAGUE, selectionArgs, null, null, sortOrder);
                break;
            case TEAMS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        teams_table.TABLE_NAME,
                        projection, null, null, null, null, sortOrder);
                break;
            case TEAM_WITH_ID:
                String team = DatabaseContract.teams_table.getTeamFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        teams_table.TABLE_NAME,
                        projection, TEAM_BY_ID, new String[]{team}, null, null, sortOrder);
                break;
            case LEAGUES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        leagues_table.TABLE_NAME,
                        projection, null, null, null, null, sortOrder);
                break;
            case LEAGUE_WITH_ID:
                String league = DatabaseContract.leagues_table.getLeagueFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        leagues_table.TABLE_NAME,
                        projection, LEAGUE_BY_ID, new String[]{league}, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (muriMatcher.match(uri))
        {
            case TEAMS: {
                long _id = db.insert(teams_table.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = teams_table.buildTeamWithId(values.getAsString(teams_table.TEAM_ID_COL));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LEAGUES: {
                long _id = db.insert(leagues_table.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = teams_table.buildTeamWithId(values.getAsString(leagues_table.LEAGUE_ID_COL));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //db.delete(DatabaseContract.TABLE_NAME,null,null);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(muriMatcher.match(uri)));
        switch (muriMatcher.match(uri)) {
            case MATCHES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(scores_table.TABLE_NAME, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
