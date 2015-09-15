package com.markesilva.footballscores.widget;

/**
 * Created by marke on 9/13/2015.
 */

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.SVG;
import com.markesilva.footballscores.R;
import com.markesilva.footballscores.SvgDecoder;
import com.markesilva.footballscores.SvgDrawableTranscoder;
import com.markesilva.footballscores.Utilies;
import com.markesilva.footballscores.data.DatabaseContract;
import com.markesilva.footballscores.scoresAdapter;
import com.markesilva.footballscores.utils.LOG;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ScoresWidgetRemoteViewsService extends RemoteViewsService {
    public static final String WIDGET_ID_EXTRA = "widget_id";

    private final String LOG_TAG = LOG.makeLogTag(ScoresWidgetRemoteViewsService.class);
    private ConcurrentLinkedQueue<CrestData> mWorkQueue = new ConcurrentLinkedQueue<>();
    private Context mContext = ScoresWidgetRemoteViewsService.this;
    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> mRequestBuilder;
    private Intent mStartingIntent;
    private Map<String, Bitmap> mCrestMap = new ConcurrentHashMap<>();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        mStartingIntent = intent;
        return new RemoteViewsFactory() {
            private Cursor mCursor = null;


            @Override
            public void onCreate() {
                LOG.D(LOG_TAG, "onCreate");
                if (mRequestBuilder == null) {
                    mRequestBuilder = Glide.with(mContext)
                            .using(Glide.buildStreamModelLoader(Uri.class, mContext), InputStream.class)
                            .from(Uri.class)
                            .as(SVG.class)
                            .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                            .sourceEncoder(new StreamEncoder())
                            .cacheDecoder(new FileToStreamDecoder<>(new SvgDecoder()))
                            .decoder(new SvgDecoder())
                            .animate(android.R.anim.fade_in)
                            .placeholder(R.drawable.no_icon)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE);
                }
            }

            @Override
            public void onDataSetChanged() {
                LOG.D(LOG_TAG, "onDataSetChanged");
                if (mCursor != null) {
                    mCursor.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                // We only show today's scores, using the query that only shows enabled leagues
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Uri scoresUri = DatabaseContract.scores_table.buildScoreWithDate(format.format(date));
                mCursor = getContentResolver().query(scoresUri,
                        scoresAdapter.SCORES_COLUMS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                LOG.D(LOG_TAG, "onDestroy");
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
            }

            @Override
            public int getCount() {
                return mCursor == null ? 0 : mCursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                LOG.D(LOG_TAG, "getViewAt");
                if (position == AdapterView.INVALID_POSITION ||
                        mCursor == null || !mCursor.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.scores_list_item);

                views.setTextViewText(R.id.time_textview, mCursor.getString(scoresAdapter.COL_MATCHTIME));
                views.setTextViewText(R.id.score_textview, Utilies.getScores(mCursor.getInt(scoresAdapter.COL_HOME_GOALS), mCursor.getInt(scoresAdapter.COL_AWAY_GOALS)));
                views.setContentDescription(R.id.score_textview, Utilies.getScoresContentDescription(mCursor.getInt(scoresAdapter.COL_HOME_GOALS), mCursor.getInt(scoresAdapter.COL_AWAY_GOALS)));

                // Home team name & crest
                String name = mCursor.getString(scoresAdapter.COL_HOME);
                String url = mCursor.getString(scoresAdapter.COL_HOME_CREST_URL);
                setTeamInfo(views, R.id.home_name, name, R.id.home_crest, url);

                // Away team name & crest
                name = mCursor.getString(scoresAdapter.COL_AWAY);
                url = mCursor.getString(scoresAdapter.COL_AWAY_CREST_URL);
                setTeamInfo(views, R.id.away_name, name, R.id.away_crest, url);
                return views;
            }

            private void setTeamInfo(RemoteViews views, int nameView, String name, int crestView, String url) {
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
                views.setTextViewTextSize(nameView, TypedValue.COMPLEX_UNIT_PX, textSizeInPx);
                views.setTextViewText(nameView, name);
                views.setContentDescription(nameView, name);
                views.setContentDescription(nameView, name + mContext.getResources().getString(R.string.crest_description));

                if (mCrestMap.containsKey(url)) {
                    views.setImageViewBitmap(crestView, mCrestMap.get(url));
                } else {
                    new CrestData(url).execute();
                }
            }

            @Override
            public RemoteViews getLoadingView() {
                LOG.D(LOG_TAG, "getLoadingView");
                return new RemoteViews(getPackageName(), R.layout.scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mCursor.moveToPosition(position))
                    return mCursor.getLong(scoresAdapter.COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

    private class CrestData extends AsyncTask<Void, Void, Bitmap> {
        private String mCrestUrl;
        public CrestData(String u) {
            mCrestUrl = u;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (mCrestMap.containsKey(mCrestUrl)) {
                // This means we have already downloaded it and there is no reason to do it again.
                return null;
            }

            Bitmap crestArt = null;
            LOG.D(LOG_TAG, "Getting crest for " + mCrestUrl);
            try {
                // If the crest is an svg file, we use our preconfigured Glide object,
                // otherwise we use the normal Glide methods
                if (mCrestUrl.toUpperCase().endsWith(".SVG")) {
                    Uri uri = Uri.parse(mCrestUrl);
                    PictureDrawable svg = mRequestBuilder
                            .load(uri)
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();
                    // Convert PictureDrawable to Bitmap from http://stackoverflow.com/questions/7201542/svg-to-bitmap-at-runtime-conversion-in-android
                    crestArt = Bitmap.createBitmap(svg.getIntrinsicWidth(), svg.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(crestArt);
                    canvas.drawPicture(svg.getPicture());
                } else {
                    GlideDrawable art = Glide.with(mContext)
                            .load(mCrestUrl)
                            .error(R.drawable.no_icon)
                            .placeholder(R.drawable.no_icon)
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();
                    crestArt = Bitmap.createBitmap(art.getIntrinsicWidth(), art.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(crestArt);
                    art.draw(canvas);
                }
            } catch (Exception e) {

            }

            if (crestArt != null) {
                float w = getApplication().getResources().getDimension(R.dimen.crest_width);
                float h = getApplication().getResources().getDimension(R.dimen.crest_height);
                crestArt = Bitmap.createScaledBitmap(crestArt, (int) w, (int) h, true);
            }

            return crestArt;
        }

        public void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                mCrestMap.put(mCrestUrl, bitmap);
                int widgetId = mStartingIntent.getIntExtra(WIDGET_ID_EXTRA, -1);
                if (widgetId != -1) {
                    AppWidgetManager.getInstance(getApplicationContext()).notifyAppWidgetViewDataChanged(widgetId, R.id.scores_widget_list);
                }
            } if (!mCrestMap.containsKey(mCrestUrl)) {
                mCrestMap.put(mCrestUrl, BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_icon));
            }
        }
    }
}

