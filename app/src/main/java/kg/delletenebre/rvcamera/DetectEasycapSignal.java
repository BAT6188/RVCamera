package kg.delletenebre.rvcamera;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.arksine.easycamlib.Easycam;
import com.arksine.easycamlib.NativeEasycam;

import java.io.File;

public class DetectEasycapSignal extends Service {
    private String TAG = "********";//getClass().getName();

    private static boolean running = true;
    private CheckSignal checkSignal;
    private MainActivity mainActivity;
    private Easycam device;
    private boolean isSignal;
    private Bitmap emptyBitmap;

    private SharedPreferences _settings = PreferenceManager.getDefaultSharedPreferences( App.getInstance() );

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        App.detectSignalService = this;

        Intent notificationIntent = new Intent(App.getInstance(), SettingsActivity.class);
        notificationIntent.setFlags(
                          Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );

        PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance(), 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder( App.getInstance() )
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText( App.getInstance().getResources().getString(R.string.notification_bar_subtitle) )
                .setContentTitle( App.getInstance().getResources().getString(R.string.notification_bar_title) )
                .setContentIntent( pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true);

        notificationManager.notify(0, builder.build());

        App.setAutoDetectSignal(_settings.getBoolean("app_autodetect", true));
        App.setDetectDelay(Integer.parseInt(_settings.getString("app_detect_delay", "500")));
        App.setAutoDetectSignalPixels(_settings.getBoolean("app_autodetect_pixels", false));
        App.setBrightnessThreshold(Integer.parseInt(_settings.getString("app_autodetect_pixels_threshold", "200")));
        App.setIsManualDeviceLocation(_settings.getBoolean("pref_key_manual_set_dev_loc", false));
        App.setDeviceLocation(_settings.getString("pref_select_dev_loc", "/dev/video0"));

        isSignal = false;
        running = true;
        checkSignal = new CheckSignal();
        checkSignal.start();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        running = false;
        if (checkSignal != null) {
            boolean retry = true;
            while (retry) {
                try {
                    checkSignal.join();
                    retry = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
    public void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }


    public Easycam getDevice() {
        return device;
    }

    private class CheckSignal extends Thread {
        private String deviceName;
        private boolean deviceNameChanged = false;

        @Override
        public void run() {
            while (running) {
                try {
                    if(!App.isAutoDetectSignalEnabled()) {
                        sleep(3000);
                    } else {
                        if (App.isManualDeviceLocation() && !App.getDeviceLocation().equals(deviceName)) {
                            deviceNameChanged = true;
                        }

                        if (device == null || deviceName == null || deviceNameChanged) {
                            sleep(1500);

                            if (deviceName == null || deviceNameChanged) {
                                device = null;
                                deviceName = checkDevices();
                                deviceNameChanged = false;
                            }

                            if (deviceName != null && !deviceName.isEmpty()) {
                                if(App.DEBUG) Log.d(TAG, "Find device: " + deviceName);

                                device = new NativeEasycam(_settings, App.getInstance());
                                emptyBitmap = device.getEmptyBitmap();
                            }

                        } else {
                            sleep(App.getDetectDelay());

                            if (device.isDeviceConnected() && device.isAttached()) {
                                try {
                                    //device.getFrame();

                                    if( device.getFrame().sameAs(emptyBitmap) || ( App.isAutoDetectSignalPixelsEnabled() && device.getPixelsSum() <= App.getBrightnessThreshold() ) ) {
                                        isSignal = false;

                                    } else if (mainActivity == null && !App.isActivityVisible()) {
                                        isSignal = true;

                                        Intent intent = new Intent(App.getInstance(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        App.getInstance().startActivity(intent);
                                    }


                                } catch (Exception e) {
                                    isSignal = false;
                                    device.stop();
                                    device = null;
                                    deviceName = null;
                                }
                            }
                        }

                        if (!isSignal) {
                            if (mainActivity != null && !App.isActivityVisible()) {
                                mainActivity.finish();
                                mainActivity = null;
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        private boolean checkFileExists(String path) {
            File file = new File(path);

            return file.exists() && file.canRead();
        }

        private String checkDevices() {
            String fName;

            if (App.isManualDeviceLocation()) {
                fName = App.getDeviceLocation();

                if (checkFileExists(fName)) {
                    return fName;
                }

            } else {
                String path[] = {
                        "/dev/ec_video",
                        "/dev/video",
                        "/dev/easycap"
                };

                for (String aPath : path) {
                    for (int i = 0; i < 10; i++) {
                        fName = aPath + String.valueOf(i);

                        if (checkFileExists(fName)) {
                            return fName;
                        }
                    }
                }
            }

            return null;
        }
    }
}
