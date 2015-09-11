package com.markesilva.footballscores.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.markesilva.footballscores.data.DatabaseContract.scores_table;
import com.markesilva.footballscores.data.DatabaseContract.teams_table;
import com.markesilva.footballscores.data.DatabaseContract.leagues_table;
import com.markesilva.footballscores.utils.LOG;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper
{
    private static final String LOG_TAG = LOG.makeLogTag(ScoresDBHelper.class);
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 1;
    public ScoresDBHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String CreateLeaguesTable = "CREATE TABLE " + leagues_table.TABLE_NAME + " ("
                + leagues_table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + leagues_table.LEAGUE_ID_COL+ " INTEGER NOT NULL, "
                + leagues_table.NAME_COL + " TEXT NOT NULL, "
                + leagues_table.ENABLED_COL + " INTEGER NOT NULL, "
                + leagues_table.LEAGUE_CODE_COL + " STRING NOT NULL, "
                + " UNIQUE (" + leagues_table.LEAGUE_ID_COL + ") ON CONFLICT REPLACE"
                + " );";

        final String CreateTeamsTable = "CREATE TABLE " + teams_table.TABLE_NAME + " ("
                + teams_table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + teams_table.TEAM_ID_COL+ " INTEGER NOT NULL, "
                + teams_table.NAME_COL + " TEXT NOT NULL, "
                + teams_table.CREST_URL_COL + " TEXT NOT NULL, "
                + " UNIQUE (" + teams_table.TEAM_ID_COL + ") ON CONFLICT REPLACE"
                + " );";

        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.scores_table.TABLE_NAME + " ("
                + scores_table._ID + " INTEGER PRIMARY KEY, "
                + scores_table.DATE_COL + " TEXT NOT NULL, "
                + scores_table.TIME_COL + " INTEGER NOT NULL, "
                + scores_table.HOME_COL + " INTEGER NOT NULL, "
                + scores_table.AWAY_COL + " INTEGER NOT NULL, "
                + scores_table.LEAGUE_COL + " INTEGER NOT NULL, "
                + scores_table.HOME_GOALS_COL + " TEXT NOT NULL, "
                + scores_table.AWAY_GOALS_COL + " TEXT NOT NULL, "
                + scores_table.MATCH_ID + " INTEGER NOT NULL, "
                + scores_table.MATCH_DAY + " INTEGER NOT NULL, "
                + " UNIQUE ("+scores_table.MATCH_ID+") ON CONFLICT REPLACE, "
                + "FOREIGN KEY (" + scores_table.HOME_COL + ") REFERENCES "
                + teams_table.TABLE_NAME + " (" + teams_table.NAME_COL + "), "
                + "FOREIGN KEY (" + scores_table.AWAY_COL + ") REFERENCES "
                + teams_table.TABLE_NAME + " (" + teams_table.NAME_COL + "), "
                + "FOREIGN KEY (" + scores_table.LEAGUE_COL + ") REFERENCES "
                + leagues_table.TABLE_NAME + " (" + leagues_table.NAME_COL + ") "
                + ");";

        LOG.D(LOG_TAG, CreateLeaguesTable);
        db.execSQL(CreateLeaguesTable);
        LOG.D(LOG_TAG, CreateTeamsTable);
        db.execSQL(CreateTeamsTable);
        LOG.D(LOG_TAG, CreateScoresTable);
        db.execSQL(CreateScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + scores_table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + teams_table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + leagues_table.TABLE_NAME);
    }
}
