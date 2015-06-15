package kg.delletenebre.rvcamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class BootCompletedReceiver extends BroadcastReceiver {
    private SharedPreferences _settings;

    public void onReceive(Context context, Intent intent) {
        _settings = PreferenceManager.getDefaultSharedPreferences(context);

        if(_settings.getBoolean("app_autostart", true)) {
            context.startService(new Intent(context, DetectEasycapSignal.class));
        }
    }
}
