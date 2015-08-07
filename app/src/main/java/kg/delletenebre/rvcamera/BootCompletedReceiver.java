package kg.delletenebre.rvcamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootCompletedReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        SharedPreferences _settings = PreferenceManager.getDefaultSharedPreferences(context);

        if(_settings.getBoolean("app_autostart", true)) {
            context.startService(new Intent(context, DetectEasycapSignal.class));
        }
    }
}
