package com.markesilva.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.markesilva.footballscores.data.DatabaseContract;
import com.markesilva.footballscores.R;
import com.markesilva.footballscores.utils.LOG;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class myFetchService extends IntentService {
    public static final String LOG_TAG = LOG.makeLogTag(myFetchService.class);
    private ConcurrentLinkedQueue<FetchData> mWorkQueue = new ConcurrentLinkedQueue<>();
    private Handler mHandler;
    private SpaceRequests mWorkTask;

    public myFetchService() {
        super("myFetchService");
    }

    @Override
    public void onCreate() {
        mHandler = new Handler();
        mWorkTask = new SpaceRequests();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Since we are already on a background thread, we need to jump through some hoops to
        // start the worker thread from the main thread
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mWorkTask.execute();
                } catch (IllegalStateException e) {
                    // If it is still running we will continue to add to the queue
                }
            }
        });
        getData("n2");
        getData("p2");

        return;
    }

    private void getData(String timeFrame) {
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        LOG.D(LOG_TAG, "The url we are looking at is: " + fetch_build.toString()); //log spam
        AddSeasonAndScores season = new AddSeasonAndScores(fetch_build.toString());
        mWorkQueue.add(season);

    }

    private class AddSeasonAndScores implements FetchData {
        private String mFixtureUrl;

        AddSeasonAndScores(String url) {
            mFixtureUrl = url;
        }

        public void fetchData() {

            HttpURLConnection m_connection = null;
            BufferedReader reader = null;
            String JSON_data = null;
            //Opening Connection
            try {
                URL fetch = new URL(mFixtureUrl.toString());
                m_connection = (HttpURLConnection) fetch.openConnection();
                m_connection.setRequestMethod("GET");
                m_connection.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
                m_connection.connect();

                // Read the input stream into a String
                InputStream inputStream = m_connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                JSON_data = buffer.toString();

                if (JSON_data != null) {
                    //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                    JSONArray matches = new JSONObject(JSON_data).getJSONArray("fixtures");
                    if (matches.length() == 0) {
                        //if there is no data, call the function on dummy data
                        //this is expected behavior during the off season.
                        processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                        return;
                    }


                    processJSONdata(JSON_data, getApplicationContext(), true);
                } else {
                    //Could not Connect
                    LOG.D(LOG_TAG, "Could not connect to server.");
                }
            } catch (Exception e) {
                LOG.E(LOG_TAG, "Error when fetching " + mFixtureUrl + ". Trying again later...");
                // This means we need to try again later
                AddSeasonAndScores scores = new AddSeasonAndScores(mFixtureUrl);
                mWorkQueue.add(scores);
            } finally {
                if (m_connection != null) {
                    m_connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOG.E(LOG_TAG, "Error Closing Stream");
                    }
                }
            }
        }
    }

    private void processJSONdata(String JSONdata, Context mContext, boolean isReal) {
        //JSON data

        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeam";
        final String AWAY_TEAM = "awayTeam";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;


        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);


            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<>(matches.length());
            for (int i = 0; i < matches.length(); i++) {

                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                AddLeagueIfNeeded addLeague = new AddLeagueIfNeeded(League, mContext);
                League = addLeague.getLeagueId();
                if (!addLeague.getLeagueExists()) {
                    mWorkQueue.add(addLeague);
                }
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if (true) {
                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    match_id = match_id.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id = match_id + Integer.toString(i);
                    }

                    String away_team = match_data.getJSONObject(LINKS).getJSONObject(AWAY_TEAM).getString("href");
                    AddTeamIfNeeded awayTeamTask = new AddTeamIfNeeded(away_team, mContext);
                    Away = awayTeamTask.getTeamId();
                    if (!awayTeamTask.getTeamExists()) {
                        mWorkQueue.add(awayTeamTask);
                    }

                    String home_team = match_data.getJSONObject(LINKS).getJSONObject(HOME_TEAM).getString("href");
                    AddTeamIfNeeded homeTeamTask = new AddTeamIfNeeded(home_team, mContext);
                    Home = homeTeamTask.getTeamId();
                    if (!homeTeamTask.getTeamExists()) {
                        mWorkQueue.add(homeTeamTask);
                    }

                    mDate = match_data.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0, mDate.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parseddate = match_date.parse(mDate + mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0, mDate.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                            mDate = mformat.format(fragmentdate);
                        }
                    } catch (Exception e) {
                        LOG.E(LOG_TAG, "error here!", e);
                    }
                    Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = match_data.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID, match_id);
                    match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
                    match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);
                    match_values.put(DatabaseContract.scores_table.HOME_COL, Home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL, Away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, Home_goals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, Away_goals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);
                    //log spam

                    LOG.D(LOG_TAG, "Inserting: " + match_id + ", " + mDate + ", " + mTime + ", " + Home + "(" + Home_goals + "), " + Away + "(" + Away_goals + ")");
                    //LOG.D(LOG_TAG, mDate);
                    //LOG.D(LOG_TAG, mTime);
                    //LOG.D(LOG_TAG, Home);
                    //LOG.D(LOG_TAG, Away);
                    //LOG.D(LOG_TAG, Home_goals);
                    //LOG.D(LOG_TAG, Away_goals);

                    values.add(match_values);
                }
            }
            int inserted_data;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.scores_table.CONTENT_URI, insert_data);

            LOG.D(LOG_TAG, "Successfully Inserted: " + String.valueOf(inserted_data));
        } catch (JSONException e) {
            LOG.E(LOG_TAG, e.getMessage());
        }
    }

    private class AddLeagueIfNeeded implements FetchData {
        private String mLeagueUrl;
        private Context mContext;
        private String mLeagueId;
        private boolean mLeagueExists;
        private final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        private final String LEAGUE_CAPTION = "caption";
        private final String LEAGUE_CODE = "league";

        AddLeagueIfNeeded(String leagueUrl, Context context) {
            mLeagueUrl = leagueUrl;
            mContext = context;
            mLeagueId = mLeagueUrl.replace(SEASON_LINK, "");
            Uri u = DatabaseContract.leagues_table.buildLeagueWithId(mLeagueId);
            Cursor c = mContext.getContentResolver().query(u, null, null, null, null);
            LOG.D(LOG_TAG, "Searching for league " + mLeagueId + ", found " + c.getCount());
            mLeagueExists = c.getCount() > 0;
            c.close();
            if (!mLeagueExists) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseContract.leagues_table.LEAGUE_ID_COL, mLeagueId);
                cv.put(DatabaseContract.leagues_table.NAME_COL, getResources().getString(R.string.loading_data));
                cv.put(DatabaseContract.leagues_table.ENABLED_COL, "1"); // By default, all leagues are enabled when first seen
                cv.put(DatabaseContract.leagues_table.LEAGUE_CODE_COL, getResources().getString(R.string.loading_data));
                Uri returnedUri = mContext.getContentResolver().insert(DatabaseContract.leagues_table.CONTENT_URI, cv);
                LOG.D(LOG_TAG, "inserted league placeholder for: " + returnedUri.toString());
            }
        }

        public String getLeagueId() {
            return mLeagueId;
        }

        public boolean getLeagueExists() {
            return mLeagueExists;
        }

        public void fetchData() {
            String JSON_data = null;
            BufferedReader reader = null;
            HttpURLConnection league_conn = null;
            try {
                LOG.D(LOG_TAG, "Getting League info from " + mLeagueUrl);
                URL fetch = new URL(mLeagueUrl);
                league_conn = (HttpURLConnection) fetch.openConnection();
                league_conn.setRequestMethod("GET");
                league_conn.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
                league_conn.connect();

                // Read the input stream into a String
                InputStream inputStream = league_conn.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                JSON_data = buffer.toString();

                JSONObject league = new JSONObject(JSON_data);
                String league_name = league.getString(LEAGUE_CAPTION);
                String league_code = league.getString(LEAGUE_CODE);

                ContentValues cv = new ContentValues();
                cv.put(DatabaseContract.leagues_table.LEAGUE_ID_COL, mLeagueId);
                cv.put(DatabaseContract.leagues_table.NAME_COL, league_name);
                cv.put(DatabaseContract.leagues_table.ENABLED_COL, "1"); // By default, all leagues are enabled when first seen
                cv.put(DatabaseContract.leagues_table.LEAGUE_CODE_COL, league_code);
                Uri returnedUri = mContext.getContentResolver().insert(DatabaseContract.leagues_table.CONTENT_URI, cv);
                //LOG.D(LOG_TAG, "inserted league: " + returnedUri.toString());
            } catch (Exception e) {
                LOG.E(LOG_TAG, "Error when fetching " + mLeagueUrl + ". Trying again later...");
                // This means we need to try again later
                AddLeagueIfNeeded league = new AddLeagueIfNeeded(mLeagueUrl, mContext);
                mWorkQueue.add(league);
            } finally {
                if (league_conn != null) {
                    league_conn.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOG.E(LOG_TAG, "Error Closing Stream");
                    }
                }
            }

            return;
        }
    }

    private class AddTeamIfNeeded implements FetchData {
        private String mTeamUrl;
        private Context mContext;
        private String mTeamId;
        private boolean mTeamExists;
        private final String TEAM_LINK = "http://api.football-data.org/alpha/teams/";
        private final String NAME = "name";
        private final String CREST_URL = "crestUrl";

        AddTeamIfNeeded(String leagueUrl, Context context) {
            mTeamUrl = leagueUrl;
            mContext = context;
            mTeamId = mTeamUrl.replace(TEAM_LINK, "");

            Uri u = DatabaseContract.teams_table.buildTeamWithId(mTeamId);
            Cursor c = mContext.getContentResolver().query(u, null, null, null, null);
            LOG.D(LOG_TAG, "Searching for team " + mTeamId + ", found " + c.getCount());
            mTeamExists = c.getCount() > 0;
            c.close();
            if (!mTeamExists) {
                // The team doesn't exist in the DB so we add some placeholder values so
                // cards can be displayed
                ContentValues cv = new ContentValues();
                cv.put(DatabaseContract.teams_table.TEAM_ID_COL, mTeamId);
                cv.put(DatabaseContract.teams_table.NAME_COL, getResources().getString(R.string.loading_data));
                cv.put(DatabaseContract.teams_table.CREST_URL_COL, "");
                Uri returnedUri = mContext.getContentResolver().insert(DatabaseContract.teams_table.CONTENT_URI, cv);
                LOG.D(LOG_TAG, "inserted team placeholder for: " + returnedUri.toString());
            }
        }

        public String getTeamId() {
            return mTeamId;
        }

        public boolean getTeamExists() {
            return mTeamExists;
        }

        public void fetchData() {
            String JSON_data = null;
            BufferedReader reader = null;
            HttpURLConnection team_conn = null;
            try {
                LOG.D(LOG_TAG, "Getting team info from " + mTeamUrl);
                URL fetch = new URL(mTeamUrl);
                team_conn = (HttpURLConnection) fetch.openConnection();
                team_conn.setRequestMethod("GET");
                team_conn.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
                team_conn.connect();

                // Read the input stream into a String
                InputStream inputStream = team_conn.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                JSON_data = buffer.toString();

                JSONObject team = new JSONObject(JSON_data);
                String team_name = team.getString(NAME);
                String crestUrl = team.getString(CREST_URL);

                ContentValues cv = new ContentValues();
                cv.put(DatabaseContract.teams_table.TEAM_ID_COL, mTeamId);
                cv.put(DatabaseContract.teams_table.NAME_COL, team_name);
                cv.put(DatabaseContract.teams_table.CREST_URL_COL, crestUrl);
                Uri returnedUri = mContext.getContentResolver().insert(DatabaseContract.teams_table.CONTENT_URI, cv);
                //LOG.D(LOG_TAG, "inserted team: " + returnedUri.toString());
            } catch (Exception e) {
                LOG.E(LOG_TAG, "Error when fetching " + mTeamUrl + ". Trying again later...");
                // This means we need to try again later
                AddTeamIfNeeded team = new AddTeamIfNeeded(mTeamUrl, mContext);
                mWorkQueue.add(team);
            } finally {
                if (team_conn != null) {
                    team_conn.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOG.E(LOG_TAG, "Error Closing Stream");
                    }
                }
            }

            return;
        }
    }

    private interface FetchData {
        void fetchData();
    }

    private class SpaceRequests extends AsyncTask<Void, Void, Void> {
        // wait time is based on 50 queries per minute allowed
        private static final long WAIT_TIME_IN_MILLIS = 1200;
        private static final int MAX_EMPTY_LIMIT = 6;
        private long mNextSleepTime = WAIT_TIME_IN_MILLIS;
        private int mEmptyCount = 0;

        @Override
        protected Void doInBackground(Void... params) {
            LOG.D(LOG_TAG, "Entering work queue thread");
            while (mEmptyCount < MAX_EMPTY_LIMIT) {
                try {
                    //LOG.D(LOG_TAG, "Sleeping for " + mNextSleepTime + " ms, " + mWorkQueue.size() + " items in the queue, empty count is " + mEmptyCount);
                    Thread.sleep(mNextSleepTime);
                    FetchData nextWorker = mWorkQueue.poll();
                    if (nextWorker != null) {
                        long startTime = SystemClock.elapsedRealtime();
                        nextWorker.fetchData();
                        long endTime = SystemClock.elapsedRealtime();
                        mNextSleepTime = WAIT_TIME_IN_MILLIS - (endTime - startTime);
                        mNextSleepTime = (mNextSleepTime < 0) ? 0 : mNextSleepTime;
                    } else {
                        mEmptyCount++;
                    }
                } catch (Exception e) {
                    LOG.E(LOG_TAG, "Error", e);
                    mNextSleepTime = WAIT_TIME_IN_MILLIS;
                }
            }
            LOG.D(LOG_TAG, "Exiting work queue thread");

            return null;
        }
    }
}

