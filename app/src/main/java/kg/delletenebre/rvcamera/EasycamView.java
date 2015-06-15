package kg.delletenebre.rvcamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.arksine.easycamlib.Easycam;

public class EasycamView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static String TAG = "EasycamView";

    private Easycam capDevice;

    private Thread mThread = null;

    private Rect mViewWindow;
    private Context appContext;
    private volatile boolean mRunning = true;
    private volatile SurfaceHolder mHolder;

    private boolean isMirrored;

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
        if(App.DEBUG) Log.d(TAG, "EasycamView constructed");
        setFocusable(false);
        setBackgroundColor(0);

        isMirrored = sharedPrefs.getBoolean("camera_mirrored", false);

        mHolder = getHolder();
        mHolder.addCallback(this);

    }

    @Override
    public void run() {

        while(mRunning) {
            if ( capDevice != null && capDevice.isAttached() ) {
                Canvas canvas = mHolder.lockCanvas();
                if(canvas != null) {
                    try {
                        drawOnCanvas(canvas, capDevice.getFrame());
                    } catch (Exception e) {

                    }
                    mHolder.unlockCanvasAndPost(canvas);
                }
            } else {
                mRunning = false;
            }

        }
    }

    protected void drawOnCanvas(Canvas canvas, Bitmap videoBitmap) {
        if ( isMirrored ) {
            Matrix flipHorizontalMatrix = new Matrix();
            flipHorizontalMatrix.setScale(-1, 1);
            flipHorizontalMatrix.postTranslate(videoBitmap.getWidth(), 0);
            Bitmap flipedSprite = Bitmap.createBitmap(videoBitmap, 0, 0,
                    videoBitmap.getWidth(),
                    videoBitmap.getHeight(), flipHorizontalMatrix, false);
            canvas.drawBitmap(flipedSprite, null, mViewWindow, null);
        } else {
            canvas.drawBitmap(videoBitmap, null, mViewWindow, null);
        }
    }

    protected Rect getViewingWindow() {
        return mViewWindow;
    }

    private void setViewingWindow(int winWidth, int winHeight) {

        mViewWindow = new Rect(0,0,winWidth,winHeight);

    }

    public void resume() {

        if (mThread != null && mThread.isAlive()) {
            mRunning = false;
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        capDevice = App.detectSignalService.getDevice();
        if(capDevice != null && !capDevice.isDeviceConnected()) {
            Toast.makeText(appContext, "Error connecting to device", Toast.LENGTH_SHORT).show();

            if(App.DEBUG) Log.e(TAG, "Error connecting device");
            mRunning = false;
            return;
        }

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
                    e.printStackTrace();
                }
            }
        }

        capDevice = null;
        if(App.DEBUG) Log.i(TAG, "View paused");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(App.DEBUG) Log.d(TAG, "Surface created");

        resume();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(App.DEBUG) Log.d(TAG, "Surface destroyed");

        pause();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int winWidth, int winHeight) {
        if(App.DEBUG) Log.d("Easycam", "surfaceChanged");

        setViewingWindow (winWidth, winHeight);

    }
}
