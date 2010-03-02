package com.joshsera;

import android.util.*;

/**
 * Utility class for vector operations.
 * 
 *  order of components is to ensure compatibility with Androids sensors.
 */

public class Point3D {
	public float x = 0;
	public float y = 0;
	public float z = 0;
	
	public static double dot(Point3D a, Point3D b) {
		return ((double)a.x * (double)b.x + (double)a.y * (double)b.y + (double)a.z * (double)b.z);
	}
	
	/**
	 * finds the cross product of two 3d vectors. The result is stored in the out parameter to
	 * avoid the overhead for memory allocation/deallocation.
	 * 
	 * @param a first vector
	 * @param b second vector
	 * @param out here's where we store the result
	 */
	public static void cross(Point3D a, Point3D b, Point3D out) {
		out.x = a.y * b.z - a.z * b.y;
		out.y = a.z * b.x - a.x * b.z;
		out.z = a.x * b.y - a.y * b.x;
	}
	
	/**
	 * finds the cross product of two 3d vectors represented to float arrays
	 * @param a
	 * @param b
	 * @param out the result is stored here
	 */
	
	public static void cross(float[] a, float[] b, Point3D out) {
		out.x = a[1] * b[2] - a[2] * b[1];
		out.y = a[2] * b[0] - a[0] * b[2];
		out.z = a[0] * b[1] - a[1] * b[0];
	}
	
	/**
	 * Finds the length of the given 3d vector
	 * @param v
	 * @return the length of the vector
	 */
	
	public static float length(Point3D v) {
		return (float)Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
	}
	
	/**
	 * Sets a vector to magnitude len
	 * @param v the vector
	 * @param len the length of the vector
	 * @param newlen the length to set the vector to
	 */
	
	public static void setLength(Point3D v, float len, float newlen) {
		float ratio = newlen/len;
		v.x *= ratio;
		v.y *= ratio;
		v.z *= ratio;
	}
	
	/**
	 * normalizes the length of a vector
	 * @param v
	 */
	
	public static void normalize(Point3D v) {
		float len = length(v);
		v.x /= len;
		v.y /= len;
		v.z /= len;
	}
	
	/**
	 * copies the values of a float array to a vector
	 * @param a the float array to be copied from
	 * @param b the vector to be copied to
	 */
	
	public static void copy(float[] a, Point3D b) {
		b.x = a[0];
		b.y = a[1];
		b.z = a[2];
	}
	
	/**
	 * Copies the values from a Point3D object to another Point3D object.
	 * @param a
	 * @param b
	 */
	
	public static void copy(Point3D a, Point3D b) {
		b.x = a.x;
		b.y = a.y;
		b.z = a.z;
	}
	
	public static void reverse(Point3D v) {
		v.x *= -1;
		v.y *= -1;
		v.z *= -1;
	}
	
	/**
	 * convert a rotation in degrees to radians
	 * @param rot
	 * @return
	 */
	
	public static double rotToRad(float rot) {
		return (rot/180) * Math.PI;
	}
	
	/**
	 * rotates a vector on the X axis
	 * @param v the vector to be rotated
	 * @param rot amount of rotation
	 */
	
	public static void rotateX(Point3D v, double sinrot, double cosrot) {
		float y = v.y;
		float z = v.z;
		//
		v.y = (float)(cosrot * y - sinrot * z);
		v.z = (float)(sinrot * y + cosrot * z);
	}
	
	/**
	 * rotates a vector on the Y axis
	 * @param v
	 * @param rot
	 */
	
	public static void rotateY(Point3D v, double sinrot, double cosrot) {
		float x = v.x;
		float z = v.z;
		//
		v.x = (float)(cosrot * x - sinrot * z);
		v.z = (float)(sinrot * x + cosrot * z);
	}
	
	public static void rotateZ(Point3D v, double sinrot, double cosrot) {
		float x = v.x;
		float y = v.y;
		//
		v.x = (float)(cosrot * x - sinrot * y);
		v.y = (float)(sinrot * x + cosrot * y);
	}
	
	/**
	 * for debugging. Logs a vector.
	 * @param v
	 */
	
	public static void logVector(String appname, Point3D v) {
    	StringBuilder builder = new StringBuilder();
    	builder.append(v.x);
    	builder.append(", ");
    	builder.append(v.y);
    	builder.append(", ");
    	builder.append(v.z);
    	Log.d(appname, builder.toString());
    }
}
