package com.xlythe.minecraftclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

public class ClockWidget extends AppWidgetProvider {
    public static final String ACTION_UPDATE = "com.xlythe.minecraftclock.CLOCK_WIDGET_UPDATE";
    private static final long INTERVAL_MS = 300_000L; // 5 mins

    @Override
    public void onEnabled(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, ClockWidget.class));
        for (int id : ids) {
            updateOne(context, mgr, id);
        }
        scheduleNext(context);
    }

    @Override
    public void onDisabled(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getUpdateIntent(context));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            updateOne(context, manager, id);
        }
        scheduleNext(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(context, ClockWidget.class));
            for (int id : ids) {
                updateOne(context, mgr, id);
            }
            scheduleNext(context);
        }
    }

    private static void updateOne(Context ctx, AppWidgetManager mgr, int id) {
        RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.widget);
        rv.setBitmap(R.id.dial, "setImageBitmap", BitmapUtil.getCurrentTimeAsBitmap(ctx));
        mgr.updateAppWidget(id, rv);
    }

    private void scheduleNext(Context ctx) {
        long now = System.currentTimeMillis();
        long next = now + INTERVAL_MS - (now % INTERVAL_MS);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, getUpdateIntent(ctx));
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, next, getUpdateIntent(ctx));
        }
    }

    private PendingIntent getUpdateIntent(Context ctx) {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_IMMUTABLE;
        Intent i = new Intent(ctx, ClockWidget.class).setAction(ACTION_UPDATE);
        return PendingIntent.getBroadcast(ctx, 0, i, flags);
    }
}
