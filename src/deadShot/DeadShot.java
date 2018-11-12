package deadShot;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import resources.Inimigo;
import robocode.*;
//import java.awt.Color;
import robocode.util.Utils;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * Test - a robot by (your name here)
 */
public class DeadShot extends Robot {
	
	Inimigo alvo;
	HashMap<String, Inimigo> inimigos;
	Point2D.Double posicaoAtual;
	Point2D.Double posicaoAnterior;
	Point2D.Double proximaPosicao;
	int direcao;
	static HashMap<String,int[][][][]> estatisticas = new HashMap<String,int[][][][]>();	// HashMap stores segmented guessfactor information for enemies, static to persist through rounds.
	double perpendicularDirection = 1;
	int hits;
	
	public void run() {
	
		this.alvo = null;
		this.inimigos = new HashMap<String, Inimigo>();
		this.proximaPosicao = null;
		this.direcao = 1;
		this.hits = 0;
		Rectangle2D campoBatalha = new Rectangle2D.Double(50, 50, getBattleFieldWidth() - 100, getBattleFieldHeight() - 100);
		//Allow robot's base, gun, and radar to rotate independently
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		while(true) {
			this.posicaoAtual = new Point2D.Double(getX(), getY());
			
			if (this.alvo == null) {
				turnRadarRight(360);
			} else {
				double anguloRadar = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(
						this.calcularAngulo(this.posicaoAtual, this.alvo.getLocalizacao())) - getRadarHeading()
						);
				this.alvo = null;
				turnRadarRight(anguloRadar);
				if (this.alvo == null) { //caso o alvo tenha se mexido
					turnRadarRight(anguloRadar < 0 ? -360 - anguloRadar : 360 - anguloRadar);
				}
			}
			
			
			if (this.alvo != null) {
				if (getOthers() > 1) {
					if (this.proximaPosicao == null ) {
						this.proximaPosicao = this.posicaoAnterior = this.posicaoAtual;
					}
					//Calcula risco de 100 pontos
					for (int i = 0; i < 100; i++) {
						double d = (Math.random() * 100) + 100;
						
						Point2D.Double p = this.calcularPonto(Math.toRadians(Math.random() * 360), d);
						
						if(campoBatalha.contains(p) && (this.calcularRisco(p) < this.calcularRisco(this.proximaPosicao))) {
							this.proximaPosicao = p;
						}
						
					}
				} else {
					// Attempts to remain perpendicular in 1v1:
					double d = (Math.random() * 100) + 150;
				// Changes perpendicular direction of movement to enemy if a valid point (at least absolute enemy bearing +- 60deg) does not exist in the current direction OR pseudo-randomly, hindering enemy targeting algorithms. Randomness largely based on the number of enemy hits landed, striking a balance between excellent head-on targeting avoidance and a reasonable level of true randomness to deter more advanced targeting algorithms.
					if(!campoBatalha.contains(this.calcularPonto(this.calcularAngulo(this.posicaoAtual, alvo.getLocalizacao()) + Math.PI / 3 * perpendicularDirection, d)) || ((Math.random() * (hits % 5) > 0.6))) {
						perpendicularDirection = -perpendicularDirection;
					}
				// If possible, will select an angle perpendicular to enemy, else finds nearest valid angle
					double angulo = this.calcularAngulo(this.posicaoAtual, this.alvo.getLocalizacao()) + (Math.PI / 2) * perpendicularDirection;
					while(!campoBatalha.contains(this.calcularPonto(angulo, d))) {
						angulo -= perpendicularDirection * 0.1;
					}
					this.proximaPosicao = this.calcularPonto(angulo, d);
					
					// Calculate absolute distance and angle to point; update prevLoc
					double distance = this.posicaoAtual.distance(this.proximaPosicao);
					double moveAngle = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(this.calcularAngulo(this.posicaoAtual, this.proximaPosicao)) - getHeading());
					this.posicaoAnterior = this.posicaoAtual;
					
				// Calculate values for smallest turn and movement to reach point
					if(Math.abs(moveAngle) > 90) {
						moveAngle = robocode.util.Utils.normalRelativeAngleDegrees(moveAngle + 180);
						distance = -distance;
					}
					turnRight(moveAngle);
					ahead(distance);
				}
			}
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		String nome = e.getName();
		Inimigo inimigo;
		
		if(this.inimigos.get(nome) == null) {
			inimigo = new Inimigo(nome,
					this.calcularPonto(Math.toRadians(getHeading() + e.getBearing()), e.getDistance()),
					e.getEnergy(),
					e.getBearing(),
					e.getHeading());
		} else {
			inimigo = new Inimigo(nome,
					this.calcularPonto(Math.toRadians(getHeading() + e.getBearing()), e.getDistance()),
					e.getEnergy(),
					e.getBearing(),
					e.getHeading());
			
		}
		
		this.inimigos.put(nome, inimigo);
		
		if (this.alvo == null || this.alvo.getNome().equals(nome)) {
			this.alvo = inimigo;
		}
		
		double firePower = Math.min(500 / this.posicaoAtual.distance(inimigo.getLocalizacao()), 3);
		double bulletSpeed = 20 - firePower * 3;
		long time = (long)(this.posicaoAtual.distance(inimigo.getLocalizacao()) / bulletSpeed);
		
		double absoluteBearing = Math.toRadians(getHeading() + inimigo.getBearing());
		System.out.println("velocidade: " + e.getVelocity());
		System.out.println("bearing: " + absoluteBearing);
		if(e.getVelocity() != 0) {
			System.out.println("velocidade" + e.getVelocity());
			if(Math.sin(Math.toRadians(inimigo.getDirecao()) - absoluteBearing) * e.getVelocity() < 0) {
				this.direcao = -1;
			} else {
				this.direcao = 1;
			}
			double inimigoX = getX() + e.getDistance() * Math.sin(absoluteBearing);
			double inimigoY = getY() + e.getDistance() * Math.cos(absoluteBearing);
			
			double predicaoX = inimigoX + Math.sin(e.getHeading()) * e.getVelocity();	
			double predicaoY = inimigoY + Math.cos(e.getHeading()) * e.getVelocity();
			
			System.out.println(predicaoX);
			System.out.println(predicaoY);
			
			double theta = Math.toDegrees(Utils.normalAbsoluteAngle(Math.atan2(predicaoX - getX(), predicaoY - getY())));
			turnGunRight(theta - getGunHeading());
			fire(firePower);
			
		} else {			
			double anguloRadar = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(
					this.calcularAngulo(this.posicaoAtual, this.alvo.getLocalizacao())) - getGunHeading()
					);
			turnGunRight(anguloRadar);					
			fire(3);
		}
		
				
	}

	private Point2D.Double calcularPonto(double angulo, double distancia) {
		return new Point2D.Double(this.posicaoAtual.getX() + distancia * Math.sin(angulo),
				this.posicaoAtual.getY() + distancia * Math.cos(angulo));
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
	
	public double calcularRisco(Point2D ponto) {
		double risco = 0;
		Iterator<Inimigo> it = this.inimigos.values().iterator();
	// Uses antigravity to repel from enemies; also accounts for high-energy enemies being a greater risk
		while(it.hasNext()) {
			Inimigo inimigo = it.next();
			risco += (inimigo.getEnergia() + 50) / ponto.distanceSq(inimigo.getLocalizacao());
		}
	// Repels from last and current locations to prevent staying too close to a single spot
		risco += 0.1 / ponto.distanceSq(this.posicaoAnterior);
		risco += 0.1 / ponto.distanceSq(this.posicaoAtual);
		
		return risco;
	}
	
	public double calcularAngulo(Point2D p, Point2D q) {
		return Math.atan2(q.getX() - p.getX(), q.getY() - p.getY());
	}
	
}
