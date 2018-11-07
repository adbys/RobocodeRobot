package deadShot;
import java.util.HashMap;

import robocode.*;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * Test - a robot by (your name here)
 */
public class DeadShot extends Robot {
	
	String target;
	
	
	public void run() {
		this.target = null;
		
		// Initialization of the robot should be put here
		
		//Allow robot's base, gun, and radar to rotate independently
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		// Robot main loop
		while(true) {
			
			if (target == null) {
				turnRadarRight(360);
			} else {
				
			}
			//Radar turns clockwise
//			turnRadarRight(Double.POSITIVE_INFINITY);
			
			// Replace the next 4 lines with any behavior you would like
			ahead(100);
			turnGunRight(360);
			back(100);
			turnGunRight(360);
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		String name = e.getName();
		
		if (this.target == null) {
			this.target = name;
		}
		
		
		double power = getOthers();
				
		// Replace the next line with any behavior you would like
		fire(1);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		back(20);
	}	
}
