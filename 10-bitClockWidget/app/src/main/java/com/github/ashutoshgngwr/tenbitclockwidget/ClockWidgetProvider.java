/*
 * Copyright 2017 Ashutosh Gangwar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.ashutoshgngwr.tenbitclockwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class ClockWidgetProvider extends BroadcastReceiver {

    protected static final String ACTION_UPDATE_CLOCK = "tenbitclockwidget.clock_update";
    private static final int REQUEST_CODE = 0x012F;

    @Override
    public void onReceive(Context context, Intent intent) {
        switch(intent.getAction()) {
            case AppWidgetManager.ACTION_APPWIDGET_ENABLED: // Update clock when widget is enabled.
            case AppWidgetManager.ACTION_APPWIDGET_UPDATE: // Update clock when update is requested.
            case Intent.ACTION_TIME_CHANGED: // Update clock when system time is changed.
            case ACTION_UPDATE_CLOCK:
                onUpdate(context);
                break;
            case AppWidgetManager.ACTION_APPWIDGET_DISABLED:
                onDisabled(context);
                break;
        }
    }

    private void onUpdate(Context context) {
        // Send widget ids to ClockWidgetUpdateService via Intent
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

        // get widget ids for all available instances
        int ids[] = widgetManager.getAppWidgetIds(
                new ComponentName(context.getPackageName(), getClass().getName()));

        if(ids.length == 0)
            return; // No widget is added to home screen. Bailing out!

        // start update service
        Intent serviceIntent = new Intent(context, ClockWidgetUpdateService.class);
        serviceIntent.putExtra("ids", ids);
        context.startService(serviceIntent);

        // set alarm for next update
        setUpdateAlarm(context);
    }

    private void onDisabled(Context context) {
        // Cancel all existing alarms for clock update.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createClockUpdateIntent(context));
    }

    private void setUpdateAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC,
                // set fire time according to frequency set by user. Default is 15000 millis.
                System.currentTimeMillis() + ClockWidgetSettings.getUpdateFrequency(),
                createClockUpdateIntent(context));
    }

    private PendingIntent createClockUpdateIntent(Context context) {
        return PendingIntent.getBroadcast(context, REQUEST_CODE, new Intent(ACTION_UPDATE_CLOCK),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
