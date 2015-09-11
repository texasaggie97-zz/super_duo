package com.markesilva.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.markesilva.footballscores.data.DatabaseContract;
import com.markesilva.footballscores.utils.LOG;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class scoresAdapter extends CursorAdapter
{
    private final static String LOG_TAG = LOG.makeLogTag(scoresAdapter.class);
    // These COL indices are tied to the columns in the projection
    public static final int COL_DATE = 0;
    public static final int COL_MATCHTIME = 1;
    public static final int COL_HOME = 2;
    public static final int COL_AWAY = 3;
    public static final int COL_LEAGUE_NAME = 4;
    public static final int COL_LEAGUE_CODE = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS =7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHDAY = 9;
    public static final String[] SCORES_COLUMS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.teams_table.HOME_QUERY_TABLE_NAME + "." + DatabaseContract.teams_table.NAME_COL,
            DatabaseContract.teams_table.AWAY_QUERY_TABLE_NAME + "." + DatabaseContract.teams_table.NAME_COL,
            DatabaseContract.leagues_table.TABLE_NAME + "." + DatabaseContract.leagues_table.NAME_COL,
            DatabaseContract.leagues_table.TABLE_NAME + "." + DatabaseContract.leagues_table.LEAGUE_CODE_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TABLE_NAME + "." + DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.MATCH_DAY,
    };
    public double detail_match_id = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
    public scoresAdapter(Context context,Cursor cursor,int flags)
    {
        super(context,cursor,flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        mHolder.home_name.setText(cursor.getString(COL_HOME));
        mHolder.away_name.setText(cursor.getString(COL_AWAY));
        mHolder.date.setText(cursor.getString(COL_MATCHTIME));
        mHolder.score.setText(Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        mHolder.match_id = cursor.getDouble(COL_ID);
        mHolder.home_crest.setImageResource(Utilies.getTeamCrestByTeamName(
                cursor.getString(COL_HOME)));
        mHolder.away_crest.setImageResource(Utilies.getTeamCrestByTeamName(
                cursor.getString(COL_AWAY)
        ));
        LOG.D(LOG_TAG, mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() + " id " + String.valueOf(mHolder.match_id));
        LOG.D(LOG_TAG,String.valueOf(detail_match_id));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if(mHolder.match_id == detail_match_id)
        {
            LOG.D(LOG_TAG,"will insert extraView");

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilies.getMatchDay(cursor.getInt(COL_MATCHDAY),
                    cursor.getString(COL_LEAGUE_CODE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(cursor.getString(COL_LEAGUE_NAME));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.home_name.getText()+" "
                    +mHolder.score.getText()+" "+mHolder.away_name.getText() + " "));
                }
            });
        }
        else
        {
            container.removeAllViews();
        }

    }
    public Intent createShareForecastIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
