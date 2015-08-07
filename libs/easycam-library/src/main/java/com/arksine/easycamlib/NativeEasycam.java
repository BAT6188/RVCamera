package com.arksine.easycamlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.nio.ByteBuffer;

public class NativeEasycam implements Easycam {

	
    private static String TAG = "NativeEasycam";
    private EasycapSettings deviceSets;
    boolean deviceConnected = false;

    private ByteBuffer rgbBuffer;
    private Bitmap mBitmap, mBitmap_cleared;
    private int mWidth;
    private int mHeight;
    private int pixelColorSum;
    
    private native int startDevice(ByteBuffer rgbBuf, String deviceName, int width, int height, 
    		int devType, int regionStd, int numBufs);
    private native void getNextFrame();
    private native boolean isDeviceAttached();
    private native void stopDevice();
    private static native String detectDevice(String deviceName);

    private Context _context;
    

    static {
        System.loadLibrary("easycam");
    }

    public NativeEasycam(SharedPreferences sharedPrefs, Context context) {
        _context = context;
    	
    	deviceSets = new EasycapSettings(sharedPrefs);
        mWidth = deviceSets.frameWidth;
        mHeight = deviceSets.frameHeight;
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
        mBitmap_cleared = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
        pixelColorSum = 0;
        
        // allocate an array of bytes to hold the entire size of the bitmap
        // at 32 bits per pixel
        rgbBuffer = ByteBuffer.allocateDirect(mWidth * mHeight * 2);

        boolean useToasts = false;//sharedPrefs.getBoolean("pref_key_layout_toasts", false);
        if (useToasts) {
            // Lets show a toast telling the user what device has been set
            Toast.makeText(context,
                    "Device set as " + deviceSets.devType.first + " at " + deviceSets.devName,
                    Toast.LENGTH_SHORT).show();
        }

        connect();
    }

    public Bitmap getEmptyBitmap() {
        return mBitmap_cleared;
    }

    private void connect() {
        boolean deviceReady = true;

        File deviceFile = new File(deviceSets.devName);
        if(deviceFile.exists()) {
            if(!deviceFile.canRead()) {
                Log.d(TAG, "Insufficient permissions on " + deviceSets.devName +
                        " -- does the app have the CAMERA permission?");
                deviceReady = false;
            }
        } else {
            Log.w(TAG, deviceSets.devName + " does not exist");
            deviceReady = false;
        }

        if(deviceReady) {
            Log.i(TAG, "Preparing camera with device name " + deviceSets.devName);
            if(startDevice(rgbBuffer, deviceSets.devName, deviceSets.frameWidth,
                    deviceSets.frameHeight, deviceSets.devType.second,
                    deviceSets.devStandard.second, deviceSets.numBuffers) == -1) {

                deviceConnected = false;
            } else {
                deviceConnected = true;
            }

        }
    }

    public Bitmap getFrame() {
        getNextFrame();
        mBitmap.copyPixelsFromBuffer(rgbBuffer);
        rgbBuffer.clear();

        pixelColorSum = 0;
        for(int x = 0; x < mBitmap.getWidth(); x += 10) { for (int y = 0; y < mBitmap.getHeight(); y += 10) {
            int pixel = mBitmap.getPixel(x,y);

            int r = Color.red(pixel);
            int g = Color.green(pixel);
            int b = Color.blue(pixel);

            pixelColorSum += (r + b + g) / 3;
        } }

        return mBitmap;
    }

    public int getPixelsSum() {
        return pixelColorSum / 255;
    }

    public void stop() {
            stopDevice();
    }

    public boolean isAttached() {
        return isDeviceAttached();
    }

    public boolean isDeviceConnected() {return deviceConnected;}
    
    static public String autoDetectDev(String dName)
    {
    	return detectDevice(dName);
    }
}
