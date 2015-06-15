package kg.delletenebre.rvcamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartActivity extends Activity {
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        startService(new Intent(App.getInstance(), DetectEasycapSignal.class));
        finish();
    }
}