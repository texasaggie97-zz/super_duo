package com.markesilva.footballscores.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract {
    public static final class scores_table implements BaseColumns {
        public static final String TABLE_NAME = "scores_table";
        public static final String PATH = "scores";
        public static final String PATH_LEAGUE = "league";
        public static final String PATH_ID = "id";
        public static final String PATH_DATE = "date";
        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID = "match_id";
        public static final String MATCH_DAY = "match_day";

        //public static Uri SCORES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH)
        //.build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        // Content Uri's and build methods
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final Uri CONTENT_URI_WITH_LEAGUE = BASE_CONTENT_URI.buildUpon().appendPath(PATH).appendPath(PATH_LEAGUE).build();

        public static final Uri CONTENT_URI_WITH_ID = BASE_CONTENT_URI.buildUpon().appendPath(PATH).appendPath(PATH_ID).build();

        public static final Uri CONTENT_URI_WITH_DATE = BASE_CONTENT_URI.buildUpon().appendPath(PATH).appendPath(PATH_DATE).build();

        public static Uri buildScoreWithDate(String date) {
            return CONTENT_URI_WITH_DATE.buildUpon().appendPath(date).build();
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class teams_table implements BaseColumns {
        public static final String TABLE_NAME = "teams_table";
        public static final String HOME_QUERY_TABLE_NAME = "home";
        public static final String AWAY_QUERY_TABLE_NAME = "away";
        public static final String PATH = "team";
        public static final String PATH_ID = "id";
        // Table data
        public static final String TEAM_ID_COL = "team_id";
        public static final String NAME_COL = "team_name";
        public static final String CREST_URL_COL = "crestUrl";

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        // Content Uri's and build methods
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final Uri CONTENT_URI_WITH_ID = BASE_CONTENT_URI.buildUpon().appendPath(PATH).appendPath(PATH_ID).build();

        public static Uri buildTeamWithId(String id) {
            return CONTENT_URI_WITH_ID.buildUpon().appendPath(id).build();
        }

        public static String getTeamFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class leagues_table implements BaseColumns {
        public static final String TABLE_NAME = "league_table";
        public static final String PATH = "league";
        public static final String PATH_ID = "id";
        // Table data
        public static final String LEAGUE_ID_COL = "league_id";
        public static final String NAME_COL = "league_name";
        public static final String ENABLED_COL = "enabled";
        public static final String LEAGUE_CODE_COL = "code";

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        // Content Uri's and build methods
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final Uri CONTENT_URI_WITH_ID = BASE_CONTENT_URI.buildUpon().appendPath(PATH).appendPath(PATH_ID).build();

        public static Uri buildLeagueWithId(String id) {
            return CONTENT_URI_WITH_ID.buildUpon().appendPath(id).build();
        }

        public static String getLeagueFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    //URI data
    public static final String CONTENT_AUTHORITY = "com.markesilva.footballscores";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
}
