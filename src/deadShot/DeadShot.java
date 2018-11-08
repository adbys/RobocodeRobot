package deadShot;
import robocode.*;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import deadShot.helpers.*;

public class DeadShot extends AdvancedRobot {
	
	//local do nosso robo
	Point2D.Double myLoc;
	
	//local anterior
	Point2D.Double prevLoc;
	
	//inimigo que a gente vai detectar
	Enemy target;
	
	//informações de inimigos que foram escaneados
	HashMap<String,Enemy> enemies;
	
	public void run() {
		
		//Cor do robot
		setBodyColor(Color.white);
		setGunColor(Color.red);
		setRadarColor(Color.black);
		setBulletColor(Color.red);
		setScanColor(Color.white);
		
		//inicializar as variáveis
		target = null;
		Rectangle2D battlefield = new Rectangle2D.Double(50, 50, getBattleFieldWidth() - 100, getBattleFieldHeight() - 100);	// Battlefield bounded by a further 50px on each side for wall avoidance, preventing collisions & ease of targeting by wall-crawlers
		enemies = new HashMap<String,Enemy>();

		
		//rodar o robo para achar o que tem menor life
		while(true){
			myLoc = new Point2D.Double(getX(), getY());
			
			if(target == null) {
				turnRadarRight(360);
			}else {
				double radarAngle = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(calcAngle(myLoc, target.loc)) - getRadarHeading());
				target = null;
				turnRadarRight(radarAngle);
				if(target == null) {	// If target has moved, acquire new target
					turnRadarRight(radarAngle < 0 ? -360 - radarAngle : 360 - radarAngle);
				}
			}
			
		}
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		String name = event.getName();
		Enemy enemy;
		
		if(enemies.get(name) == null) {
			enemy = new Enemy(name, calcPoint(myLoc, Math.toRadians(getHeading() + event.getBearing()), event.getDistance()), event.getEnergy(), event.getBearing(), event.getHeading(), new Vector<BulletWave>());
		} else {
			enemy = new Enemy(name, calcPoint(myLoc, Math.toRadians(getHeading() + event.getBearing()), event.getDistance()), event.getEnergy(), event.getBearing(), event.getHeading(), enemies.get(name).waves);
		}
		enemies.put(name, enemy);
		
		// calcula o ponto certo do outro robo
		double absoluteBearing = getHeading() + event.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

		// caso esteja muito perto
		if (Math.abs(bearingFromGun) <= 3) {
			turnGunRight(bearingFromGun);
			//calculo do fire
			if (getGunHeat() == 0) {
				fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
			}
		} 
		//se não está perto
		else {
			turnGunRight(bearingFromGun);
		}
		if (bearingFromGun == 0) {
			scan();
		}
		
	}
	
	@Override
	public void scan() {
		turnRadarRight(360);
		target = getSmallerEnergy();
	}
	
	//provavel função para calcular o que tem menor life
	public Enemy getSmallerEnergy() {
		double menor =  Double.NEGATIVE_INFINITY;
		
		Enemy returnEnemy = null;
		Iterator<Enemy> it = enemies.values().iterator();
		
		while(it.hasNext()) {
			Enemy enemy = it.next();
			
			if(enemy.energy > menor) {
				returnEnemy = enemy;
			}
		}
		
		System.out.println(returnEnemy.name);
		return returnEnemy;
	}
	
	public void onWin(WinEvent e) {
		turnRight(15);
		
		while(true) {
			turnLeft(30);
			turnRight(30);
		}
	}
	
		public double calcRisk(Point2D point) {
			double risk = 0;
			Iterator<Enemy> it = enemies.values().iterator();
			while(it.hasNext()) {
				Enemy enemy = it.next();
				risk += (enemy.energy + 50) / point.distanceSq(enemy.loc);
			}
		
			risk += 0.1 / point.distanceSq(prevLoc);
			risk += 0.1 / point.distanceSq(myLoc);
			
			return risk;
		}
		
		
		public Point2D.Double calcPoint(Point2D origin, double angle, double distance) {
			return new Point2D.Double(origin.getX() + distance * Math.sin(angle), origin.getY() + distance * Math.cos(angle));
		}
		
		public double calcAngle(Point2D p, Point2D q) {
			return Math.atan2(q.getX() - p.getX(), q.getY() - p.getY());
		}
}