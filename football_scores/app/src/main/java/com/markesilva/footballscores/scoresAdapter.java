package com.markesilva.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;
import com.markesilva.footballscores.data.DatabaseContract;
import com.markesilva.footballscores.utils.LOG;

import java.io.InputStream;

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
    public static final int COL_HOME_CREST_URL = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_AWAY_CREST_URL = 5;
    public static final int COL_LEAGUE_NAME = 6;
    public static final int COL_LEAGUE_CODE = 7;
    public static final int COL_HOME_GOALS = 8;
    public static final int COL_AWAY_GOALS =9;
    public static final int COL_ID = 10;
    public static final int COL_MATCHDAY = 11;
    public static final String[] SCORES_COLUMS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.teams_table.HOME_QUERY_TABLE_NAME + "." + DatabaseContract.teams_table.NAME_COL,
            DatabaseContract.teams_table.HOME_QUERY_TABLE_NAME + "." + DatabaseContract.teams_table.CREST_URL_COL,
            DatabaseContract.teams_table.AWAY_QUERY_TABLE_NAME + "." + DatabaseContract.teams_table.NAME_COL,
            DatabaseContract.teams_table.AWAY_QUERY_TABLE_NAME + "." + DatabaseContract.teams_table.CREST_URL_COL,
            DatabaseContract.leagues_table.TABLE_NAME + "." + DatabaseContract.leagues_table.NAME_COL,
            DatabaseContract.leagues_table.TABLE_NAME + "." + DatabaseContract.leagues_table.LEAGUE_CODE_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TABLE_NAME + "." + DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.MATCH_DAY,
    };
    public double detail_match_id = 0;

    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> mRequestBuilder;
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
        if (mRequestBuilder == null) {
            mRequestBuilder = Glide.with(context)
                    .using(Glide.buildStreamModelLoader(Uri.class, context), InputStream.class)
                    .from(Uri.class)
                    .as(SVG.class)
                    .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                    .sourceEncoder(new StreamEncoder())
                    .cacheDecoder(new FileToStreamDecoder<>(new SvgDecoder()))
                    .decoder(new SvgDecoder())
                    .animate(android.R.anim.fade_in)
                    .placeholder(R.drawable.no_icon)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new SvgSoftwareLayerSetter<Uri>());
        }
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        mHolder.date.setText(cursor.getString(COL_MATCHTIME));
        mHolder.score.setText(Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        mHolder.score.setContentDescription(Utilies.getScoresContentDescription(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        mHolder.match_id = cursor.getDouble(COL_ID);

        // Home team name & crest
        String name = cursor.getString(COL_HOME);
        String url = cursor.getString(COL_HOME_CREST_URL);
        setTeamInfo(mHolder.home_name, name, mHolder.home_crest, url);

        // Away team name & crest
        name = cursor.getString(COL_AWAY);
        url = cursor.getString(COL_AWAY_CREST_URL);
        setTeamInfo(mHolder.away_name, name, mHolder.away_crest, url);

        //LOG.D(LOG_TAG, mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() + " id " + String.valueOf(mHolder.match_id));
        //LOG.D(LOG_TAG,String.valueOf(detail_match_id));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if(mHolder.match_id == detail_match_id)
        {
            LOG.D(LOG_TAG, "will insert extraView");

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilies.getMatchDay(cursor.getInt(COL_MATCHDAY), cursor.getString(COL_LEAGUE_CODE)));
            match_day.setContentDescription(Utilies.getMatchDay(cursor.getInt(COL_MATCHDAY), cursor.getString(COL_LEAGUE_CODE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(cursor.getString(COL_LEAGUE_NAME));
            league.setContentDescription(cursor.getString(COL_LEAGUE_NAME));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.home_name.getText() + " "
                            + mHolder.score.getText() + " " + mHolder.away_name.getText() + " "));
                }
            });
        }
        else
        {
            container.removeAllViews();
        }

    }

    private void setTeamInfo(TextView nameView, String name, ImageView crestView, String url) {
        float textSizeInPx;

        // Adjust the text size based on string length
        if (name.length() > 20) {
            textSizeInPx = mContext.getResources().getDimension(R.dimen.TeamNamesSmall);
        } else if (name.length() > 12) {
            textSizeInPx = mContext.getResources().getDimension(R.dimen.TeamNamesMedium);
        } else {
            textSizeInPx = mContext.getResources().getDimension(R.dimen.TeamNamesLarge);
        }
        // When we get the text size via a query it is returned in PX
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx);
        nameView.setText(name);
        nameView.setContentDescription(name);
        // If the crest is an svg file, we use our preconfigured Glide object,
        // otherwise we use the normal Glide methods
        crestView.setContentDescription(name + mContext.getResources().getString(R.string.crest_description));
        if (url.toUpperCase().endsWith(".SVG")) {
            Uri uri = Uri.parse(url);
            mRequestBuilder
                    .load(uri)
                    .into(crestView);
        } else {
            Glide.with(mContext)
                    .load(url)
                    .error(R.drawable.no_icon)
                    .placeholder(R.drawable.no_icon)
                    .into(crestView);
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
