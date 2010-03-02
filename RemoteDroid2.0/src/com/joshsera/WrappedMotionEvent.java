/**
 * 
 */
package com.joshsera;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.view.MotionEvent;

/**
 * Wraps the multitouch methods avaliable on Android's MotionEvent class,
 * invoking them by Reflection. This allows creating a SDK 3 version which will
 * accept multi-touch input if used on a compatible device.
 * 
 * @author Nicolas Frenay
 * 
 */
public class WrappedMotionEvent {

	private static Method mMotionEvent_GetPointerCount;
	private static Method mMotionEvent_GetPointerId;
	private static Method mMotionEvent_GetX;
	private static Method mMotionEvent_GetY;

	private static boolean mIsMultitouchCapable;

	/**
	 * Cached instance of empty Object array.
	 */
	private static Object[] mEmptyObjectArray = new Object[] {};

	static {
		initCompatibility();
	};

	private static void initCompatibility() {
		try {
			mMotionEvent_GetPointerCount = MotionEvent.class.getMethod("getPointerCount",
					new Class[] {});
			mMotionEvent_GetPointerId = MotionEvent.class.getMethod("getPointerId",
					new Class[] { int.class });
			mMotionEvent_GetX = MotionEvent.class.getMethod("getX", new Class[] { int.class });
			mMotionEvent_GetY = MotionEvent.class.getMethod("getY", new Class[] { int.class });
			/* success, this is a newer device */
			mIsMultitouchCapable = true;
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			mIsMultitouchCapable = false;
		}
	}

	public static boolean isMutitouchCapable() {
		return mIsMultitouchCapable;
	}

	/**
	 * Reflected method call.
	 * 
	 * @param event
	 * @see android.view.MotionEvent#getPointerCount()
	 * @return
	 */
	public static int getPointerCount(MotionEvent event) {
		try {
			int pointerCount = (Integer) mMotionEvent_GetPointerCount.invoke(event,
					mEmptyObjectArray);
			return pointerCount;
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		}
	}

	/**
	 * Reflected method call.
	 * 
	 * @param event
	 * @param pointerIndex
	 * @see android.view.MotionEvent#getPointerId(java.lang.Integer)
	 * @return
	 */
	public static int getPointerId(MotionEvent event, int pointerIndex) {
		try {
			int pointerCount = (Integer) mMotionEvent_GetPointerId.invoke(event,
					new Object[] { pointerIndex });
			return pointerCount;
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		}
	}

	/**
	 * Reflected method call.
	 * 
	 * @param event
	 * @param pointerIndex
	 * @see android.view.MotionEvent#getX(java.lang.Integer)
	 * @return
	 */
	public static float getX(MotionEvent event, int pointerIndex) {
		try {
			float pointerCount = (Float) mMotionEvent_GetX.invoke(event,
					new Object[] { pointerIndex });
			return pointerCount;
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		}
	}

	/**
	 * Reflected method call.
	 * 
	 * @param event
	 * @param pointerIndex
	 * @see android.view.MotionEvent#getY(java.lang.Integer)
	 * @return
	 */
	public static float getY(MotionEvent event, int pointerIndex) {
		try {
			float pointerCount = (Float) mMotionEvent_GetY.invoke(event,
					new Object[] { pointerIndex });
			return pointerCount;
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Reflected multitouch method failed!", e);
		}
	}

}
