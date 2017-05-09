package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.DetailsActivity;

public class StocksWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stocks_widget);

            views.setRemoteAdapter(
                    R.id.stocks_widget_list,
                    new Intent(context, StocksWidgetRemoteViewsService.class)
            );
            views.setEmptyView(R.id.stocks_widget_list, R.id.stocks_widget_empty);

            PendingIntent pendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(new Intent(context, DetailsActivity.class))
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.stocks_widget_list, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(QuoteSyncJob.ACTION_DATA_UPDATED)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.notifyAppWidgetViewDataChanged(
                    appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass())),
                    R.id.stocks_widget_list
            );
        } else {
            super.onReceive(context, intent);
        }
    }
}

