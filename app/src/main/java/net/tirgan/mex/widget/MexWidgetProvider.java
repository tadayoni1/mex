package net.tirgan.mex.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import net.tirgan.mex.R;
import net.tirgan.mex.ui.main.MainActivity;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MexWidgetConfigureActivity MexWidgetConfigureActivity}
 */
public class MexWidgetProvider extends AppWidgetProvider {

    public static final String WIDGET_INTENT_EXTRA_VENUE_KEY = "widget-intent-extra-venue-key";
    public static final String WIDGET_INTENT_EXTRA_APPWIDGET_ID = "widget-intent-extra-appwidget-id";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = MexWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        String key = MexWidgetConfigureActivity.loadKeyPref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mex_widget);
        views.setTextViewText(R.id.ap_mex_tv, widgetText);
        Intent intent = new Intent(context, MexWidgetRemoteViewsService.class);
        intent.putExtra(WIDGET_INTENT_EXTRA_VENUE_KEY, key);
        intent.putExtra(WIDGET_INTENT_EXTRA_APPWIDGET_ID, appWidgetId);
        views.setRemoteAdapter(R.id.ap_mex_lv, intent);

        // Handle clicking on widget to open the app
        Intent intent1 = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, 0);
        views.setOnClickPendingIntent(R.id.ap_mex_tv, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            MexWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

