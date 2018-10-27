package io.ruszkipista.digitalclock;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.DateFormat;

import java.util.Calendar;

public class DigitalClockWidgetProvider extends AppWidgetProvider {
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        context.startService(new Intent(context,UpdateTimeService.class));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, UpdateTimeService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("DigitalClock","onUpdate");
        Intent serviceIntent = new Intent(context,UpdateTimeService.class);
        context.startService(serviceIntent);
    }

    public static final class UpdateTimeService extends Service {
        static final String UPDATE_TIME = "io.ruszkipista.digitalclock.action.UPDATE_TIME";

        private Calendar mCalendar;
        private final static IntentFilter mIntentFilter = new IntentFilter();

        static {
            mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
            mIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
            mIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        }

        private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTime();
            }
        };

        @Override
        public void onCreate() {
            super.onCreate();
            Log.d("DigitalClock","onCreate");
            mCalendar = Calendar.getInstance();
            registerReceiver(mTimeChangedReceiver, mIntentFilter);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            unregisterReceiver(mTimeChangedReceiver);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, flags, startId);

            if (intent != null) {
                if (UPDATE_TIME.equals(intent.getAction())) {
                    updateTime();
                }
            }

            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private void updateTime() {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.digital_clock_widget);
            mRemoteViews.setTextViewText(R.id.Hours, DateFormat.format(getString(R.string.hour_format), mCalendar));
            mRemoteViews.setTextViewText(R.id.Minutes, DateFormat.format(getString(R.string.minute_format), mCalendar));
            mRemoteViews.setTextViewText(R.id.Day, DateFormat.format(getString(R.string.date_format), mCalendar));

            ComponentName mComponentName = new ComponentName(this, DigitalClockWidgetProvider.class);
            AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(this);
            mAppWidgetManager.updateAppWidget(mComponentName, mRemoteViews);
        }
    }
}

