package kg.delletenebre.rvcamera;

import android.app.Application;

public class App extends Application {

    private static App singleton;
    protected static boolean DEBUG = false;

    public static DetectEasycapSignal detectSignalService;
    private static boolean activityVisible, autodetectSignal, autodetectSignalPixels, isManualDeviceLocation;
    private static int detectDelay, brightnessThreshold;
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

    public static void setDetectDelay(int delay) {
        detectDelay = delay;
    }
    public static int getDetectDelay() {
        return detectDelay;
    }



    public static void setAutoDetectSignalPixels(boolean isAutoDetectPixels) {
        autodetectSignalPixels = isAutoDetectPixels;
    }
    public static boolean isAutoDetectSignalPixelsEnabled() {
        return autodetectSignalPixels;
    }

    public static void setBrightnessThreshold(int threshold) {
        brightnessThreshold = threshold;
    }
    public static int getBrightnessThreshold() {
        return brightnessThreshold;
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


    public static void setDebug(boolean state) {
        DEBUG = state;
    }
}