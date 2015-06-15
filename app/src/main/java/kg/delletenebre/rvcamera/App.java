package kg.delletenebre.rvcamera;

import android.app.Application;

public class App extends Application {

    private static App singleton;
    protected static final boolean DEBUG = true;

    public static DetectEasycapSignal detectSignalService;
    private static boolean activityVisible, autodetectSignal, isManualDeviceLocation;
    private static int detectDelay;
    private static String deviceLocation;


    public static App getInstance() {
        return singleton;
    }




    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }
    public static void activityResumed() {
        activityVisible = true;
    }
    public static void activityPaused() {
        activityVisible = false;
    }


    public static void setAutoDetectSignal(boolean isAutoDetect) {
        autodetectSignal = isAutoDetect;
    }
    public static boolean isAutoDetectSignalEnabled() {
        return autodetectSignal;
    }


    public static int getDetectDelay() {
        return detectDelay;
    }
    public static void setDetectDelay(int delay) {
        detectDelay = delay;
    }


    public static boolean isManualDeviceLocation() {
        return isManualDeviceLocation;
    }
    public static void setIsManualDeviceLocation(boolean value) {
        isManualDeviceLocation = value;
    }


    public static String getDeviceLocation() {
        return deviceLocation;
    }
    public static void setDeviceLocation(String location) {
        deviceLocation = location;
    }
}