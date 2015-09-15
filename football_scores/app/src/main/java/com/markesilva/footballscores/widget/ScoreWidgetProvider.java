package com.markesilva.footballscores.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.markesilva.footballscores.MainActivity;
import com.markesilva.footballscores.R;
import com.markesilva.footballscores.utils.LOG;

/**
 * Provider for a scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ScoreWidgetProvider extends AppWidgetProvider {
    private final String LOG_TAG = LOG.makeLogTag(ScoreWidgetProvider.class);

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        LOG.D(LOG_TAG, "onUpdate");
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scores_widget);

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.scores_widget, pendingIntent);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views, appWidgetId);
            } else {
                setRemoteAdapterV11(context, views, appWidgetId);
            }
            //Intent clickIntentTemplate = new Intent(context, MainActivity.class);
            //PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
            //        .addNextIntentWithParentStack(clickIntentTemplate)
            //        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            //views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            //views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        LOG.D(LOG_TAG, "onReceive");
        super.onReceive(context, intent);
        //if (SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.scores_widget_list);
        //}
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views, int widgetId) {
        Intent intent = new Intent(context, ScoresWidgetRemoteViewsService.class);
        intent.putExtra(ScoresWidgetRemoteViewsService.WIDGET_ID_EXTRA, widgetId);
        views.setRemoteAdapter(R.id.scores_widget_list, intent);
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views, int widgetId) {
        Intent intent = new Intent(context, ScoresWidgetRemoteViewsService.class);
        intent.putExtra(ScoresWidgetRemoteViewsService.WIDGET_ID_EXTRA, widgetId);
        views.setRemoteAdapter(0, R.id.scores_widget_list, intent);
    }
}
