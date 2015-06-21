package com.arksine.easycamlib;

import android.graphics.Bitmap;

public interface Easycam {
	Bitmap getFrame();
    void stop();
    boolean isAttached();
    boolean isDeviceConnected();

    Bitmap getEmptyBitmap();
    int getPixelsSum();
}
