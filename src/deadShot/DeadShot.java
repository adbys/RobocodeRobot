package deadShot;
import robocode.*;
import java.awt.Color;
import deadShot.helpers.*;

public class DeadShot extends Robot {
	
	//inimigo que a gente vai detectar
	Enemy target;
	
	public void run() {
		setBodyColor(Color.white);
		setGunColor(Color.red);
		setRadarColor(Color.black);
		setBulletColor(Color.red);
		setScanColor(Color.white);
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		String name = event.getName();
		
		
	}
	
	public void onWin(WinEvent e) {
		turnRight(15);
		
		while(true) {
			turnLeft(30);
			turnRight(30);
		}
	}
}
