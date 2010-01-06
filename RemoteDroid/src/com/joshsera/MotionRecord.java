package com.joshsera;

import android.hardware.*;

/**
 * Keeps a record of motion. Separates the gravity vector from the motion vector
 * @author jsera
 *
 */
public class MotionRecord {
	public Point3D g;
	public Point3D m;
	
	public MotionRecord() {
		this.g = new Point3D();
		this.m = new Point3D();
	}
	
	/**
	 * Sets gravity and motion based on the motion vectors, and the orientation
	 * @param motion
	 * @param orientation
	 */
	
	public void findComponents(Point3D motion, Point3D orientation) {
		Point3D.copy(motion, this.m);
		// figure out if we need to add to the roll
		if (motion.z > 0) {
			// need to add
			float absZ = Math.abs(orientation.y);
			float dir = orientation.y/absZ;
			orientation.y = (90 + (90 - absZ)) * dir;
		}
		// rotate G
		double xRot = Point3D.rotToRad(orientation.y);
		double yRot = Point3D.rotToRad(orientation.z);
		this.g.x = 0;
		this.g.y = 0;
		this.g.z = -SensorManager.GRAVITY_EARTH;
		Point3D.rotateX(this.g, Math.sin(xRot), Math.cos(xRot));
		Point3D.rotateY(this.g, Math.sin(yRot), Math.cos(yRot));
		// subtract g
		this.m.x -= this.g.x;
		this.m.y -= this.g.y;
		this.m.z -= this.g.z;
		//
		this.m.x *= -1;
	}
}
