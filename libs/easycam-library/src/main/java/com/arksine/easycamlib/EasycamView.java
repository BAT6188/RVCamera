package com.arksine.easycamlib;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class EasycamView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

	private static String TAG = "EasycamView";
	
	private Easycam capDevice;
	
	private Thread mThread = null;

    private Rect mViewWindow;
    private Context appContext;
    private volatile boolean mRunning = true;
    private volatile SurfaceHolder mHolder;

    SharedPreferences sharedPrefs;

   public EasycamView(Context context) {
       super(context);
       appContext = context;
       sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
       init();
   }

   public EasycamView(Context context, AttributeSet attrs) {
       super(context, attrs);
       appContext = context;
       sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
       init();
   }

   private void init() {
       Log.d(TAG, "EasycamView constructed");
       setFocusable(true);
       setBackgroundColor(0);

       mHolder = getHolder();
       mHolder.addCallback(this);
                
   }

   @Override
   public void run() {	   
       while(mRunning) {
           if (capDevice.isAttached()) {                 
               Canvas canvas = mHolder.lockCanvas();
               if(canvas != null) {
                   drawOnCanvas(canvas, capDevice.getFrame());
                   mHolder.unlockCanvasAndPost(canvas);
               }
           } else {
               mRunning = false;
           }
    	     	      	                                      
       }	   
   }

   protected void drawOnCanvas(Canvas canvas, Bitmap videoBitmap) {
       canvas.drawBitmap(videoBitmap, null, mViewWindow, null);
   }

   protected Rect getViewingWindow() {
       return mViewWindow;
   }
   
   private void setViewingWindow(int winWidth, int winHeight) {

        mViewWindow = new Rect(0,0,winWidth,winHeight);

   }
   
   public void resume() {
	    
       if (mThread != null && mThread.isAlive())
       {
    	   mRunning = false;
    	   try { 		   
    		   mThread.join();
    	   } catch (InterruptedException e) {
    		   e.printStackTrace();
    	   }
       }
       
       capDevice = new NativeEasycam(sharedPrefs, appContext);
       if(!capDevice.isDeviceConnected()) {
           Toast.makeText(appContext, "Error connecting to device", Toast.LENGTH_SHORT).show();

           Log.e(TAG, "Error connecting device");
           mRunning = false;
           return;
       }
       Log.i(TAG, "View resumed");

       mRunning = true;  
       mThread = new Thread(this);
       mThread.start();
   }
   
   public void pause()  {

       mRunning = false;
       if (mThread != null) {
           boolean retry = true;
           while (retry) {
               try {
                   mThread.join();
                   retry = false;
               } catch (InterruptedException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
               }
           }
       }
	   capDevice.stop();
       Log.i(TAG, "View paused");
	}

   @Override
   public void surfaceCreated(SurfaceHolder holder) {
       Log.d(TAG, "Surface created");
       
       resume();
   }

   @Override
   public void surfaceDestroyed(SurfaceHolder holder) {
       Log.d(TAG, "Surface destroyed");
      
       pause();
   }

   @Override
   public void surfaceChanged(SurfaceHolder holder, int format, int winWidth, int winHeight) {
       Log.d("Easycam", "surfaceChanged");

       setViewingWindow (winWidth, winHeight);
       
   }
}
