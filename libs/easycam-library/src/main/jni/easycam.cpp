#include "easycam.h"
#include "util.h"
#include <cstring>

JNIEXPORT jint JNICALL Java_com_arksine_easycamlib_NativeEasycam_startDevice(JNIEnv* jenv, jobject thisObj,
		jobject rgbBuf, jstring deviceName, jint width, jint height, jint devType, jint regionStd, jint numBufs)
{
	char* devName = (char*)jenv->GetStringUTFChars(deviceName, 0);
	unsigned char* rgbBuffer = (unsigned char*)jenv->GetDirectBufferAddress(rgbBuf);

	DeviceSettings dSets;

	// Assign basic settings
	CLEAR(dSets);
	dSets.device_name = devName;
	dSets.frame_width = (int)width;
	dSets.frame_height = (int)height;
	dSets.device_type = (DeviceType)devType;
	dSets.standard_id = (VideoStandard)regionStd;
	dSets.num_buffers = (int)numBufs;


	if (vDevice == nullptr)
		vDevice = new VideoDevice(rgbBuffer, dSets);

	int result = vDevice->open_device();
	jenv->ReleaseStringUTFChars(deviceName, devName);
	if(result == ERROR_LOCAL) {
	        return result;
	}

	result = vDevice->init_device();
	if(result == ERROR_LOCAL) {
		return result;
	}

	result = vDevice->start_capture();
	if(result != SUCCESS_LOCAL) {
	        delete vDevice;
	        vDevice = nullptr;
	        LOGE("Unable to start capture, resetting device");
	}

	return result;

}
JNIEXPORT void JNICALL Java_com_arksine_easycamlib_NativeEasycam_getNextFrame(JNIEnv* jenv, jobject thisObj,
		jint bmpWidth, jint bmpHeight)
{
	if (vDevice) {
		vDevice->process_capture(jenv);
	}


}
JNIEXPORT jboolean JNICALL Java_com_arksine_easycamlib_NativeEasycam_isDeviceAttached(JNIEnv* jenv, jobject thisObj)
{
	if (vDevice) {
		return vDevice->video_device_attached();
	}
	else {
//		LOGE("Video Device Not Initialized");
		return false;
	}
}
JNIEXPORT void JNICALL Java_com_arksine_easycamlib_NativeEasycam_stopDevice(JNIEnv* jenv, jobject thisObj)
{

	delete vDevice;
	vDevice = nullptr;
}

JNIEXPORT jstring JNICALL Java_com_arksine_easycamlib_NativeEasycam_detectDevice(JNIEnv* jenv, jobject thisObj, jstring deviceName)
{
	DeviceType dType = NO_DEVICE;
	jstring result;

	char* devName = (char*)jenv->GetStringUTFChars(deviceName, 0);
	dType = VideoDevice::detect_device(devName);
	jenv->ReleaseStringUTFChars(deviceName, devName);

	switch(dType) {
	case UTV007:
		result = jenv->NewStringUTF("UTV007");
		break;
	case EMPIA:
		result = jenv->NewStringUTF("EMPIA");
		break;
	case STK1160:
		result = jenv->NewStringUTF("STK1160");
		break;
	case SOMAGIC:
		result = jenv->NewStringUTF("SOMAGIC");
		break;
	default:
		result = jenv->NewStringUTF("NODEVICE");
	}

	return result;
}
