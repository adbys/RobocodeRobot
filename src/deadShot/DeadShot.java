package deadShot;
import robocode.*;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import deadShot.helpers.*;

public class DeadShot extends Robot {
	
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
			
			if(target != null) {
				double radarAngle = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(calcAngle(myLoc, target.loc)) - getRadarHeading());
				turnGunRight(radarAngle);
				fire(1);
			}
		}
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		String name = event.getName();
		Enemy enemy;
		
		
		//vamos colocar todos os inimigos
		if(enemies.get(name) == null) {
			enemy = new Enemy(name, calcPoint(myLoc, Math.toRadians(getHeading() + event.getBearing()), event.getDistance()), event.getEnergy(), event.getBearing(), event.getHeading(), new Vector<BulletWave>());
		} else {
			enemy = new Enemy(name, calcPoint(myLoc, Math.toRadians(getHeading() + event.getBearing()), event.getDistance()), event.getEnergy(), event.getBearing(), event.getHeading(), enemies.get(name).waves);
		}
		enemies.put(name, enemy);
		
		//provavelmente onde vamos setar o target para o inimigo de menor life
		if(target == null) {
			target = getSmallerEnergy();
			System.out.println("menor alvo no momento");
			System.out.println(target.name);
		}
		
		
	}
	
	//provavel função para calcular o que tem menor life
	public Enemy getSmallerEnergy() {
		double menor =  Double.POSITIVE_INFINITY;
		
		Enemy returnEnemy = null;
		Iterator<Enemy> it = enemies.values().iterator();
		
		while(it.hasNext()) {
			Enemy enemy = it.next();
			
			if(enemy.energy < menor) {
				returnEnemy = enemy;
			}
		}
		
		return returnEnemy;
	}
	
	public void onWin(WinEvent e) {
		turnRight(15);
		
		while(true) {
			turnLeft(30);
			turnRight(30);
		}
	}
	
	// Função que calcula o risco de se mover para um determinado ponto
		public double calcRisk(Point2D point) {
			double risk = 0;
			Iterator<Enemy> it = enemies.values().iterator();
		// Uses antigravity to repel from enemies; also accounts for high-energy enemies being a greater risk
			while(it.hasNext()) {
				Enemy enemy = it.next();
				risk += (enemy.energy + 50) / point.distanceSq(enemy.loc);
			}
		// Repels from last and current locations to prevent staying too close to a single spot
			risk += 0.1 / point.distanceSq(prevLoc);
			risk += 0.1 / point.distanceSq(myLoc);
			
			return risk;
		}
		
		// Function calculates a point at a given angle to and distance from an origin point:
		public Point2D.Double calcPoint(Point2D origin, double angle, double distance) {
			return new Point2D.Double(origin.getX() + distance * Math.sin(angle), origin.getY() + distance * Math.cos(angle));
		}
		
		public double calcAngle(Point2D p, Point2D q) {
			return Math.atan2(q.getX() - p.getX(), q.getY() - p.getY());
		}
}
