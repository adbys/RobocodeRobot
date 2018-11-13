package deadShot;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;

import resources.Inimigo;
import robocode.*;
//import java.awt.Color;
import robocode.util.Utils;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * Test - a robot by (your name here)
 */
public class DeadShot extends AdvancedRobot {
	
	Inimigo alvo;
	HashMap<String, Inimigo> inimigos;
	Point2D.Double posicaoAtual;
	Point2D.Double posicaoAnterior;
	Point2D.Double proximaPosicao;
	int direcao;
	double direcaoPerpendicular = 1;
	int hits;
	
	public void run() {
		
		this.alvo = null;
		this.inimigos = new HashMap<String, Inimigo>();
		this.proximaPosicao = null;
		this.direcao = 1;
		this.hits = 0;
		Rectangle2D campoBatalha = new Rectangle2D.Double(50, 50, getBattleFieldWidth() - 100, getBattleFieldHeight() - 100);
		//radar e arma giram de forma independente
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
						System.out.println("risco de todas as posições");
						System.out.println(calcularRisco(p));
						if(campoBatalha.contains(p) && (this.calcularRisco(p) < this.calcularRisco(this.proximaPosicao))) {
							this.proximaPosicao = p;
						}
						
					}
					System.out.println("essa é a posição com menor risco:");
					System.out.println(this.proximaPosicao);
					System.out.println("esse é o menor risco:");
					System.out.println(calcularRisco(this.proximaPosicao));
					
				} else {
					// Continuar em uma rota perpendicular batalha 1v1 :
					double d = (Math.random() * 100) + 150;
					//Adicionar aleatoriedade no movimento para tentar evitar algoritmos de mira mais sofisticados
					if(!campoBatalha.contains(this.calcularPonto(this.calcularAngulo(this.posicaoAtual, alvo.getLocalizacao()) + Math.PI / 3 * direcaoPerpendicular, d)) || ((Math.random() * (hits % 5) > 0.6))) {
						direcaoPerpendicular = -direcaoPerpendicular;
					}
					//seleciona um angulo perpendicular ao inimigo ou o angulo valido mais proximo
					double angulo = this.calcularAngulo(this.posicaoAtual, this.alvo.getLocalizacao()) + (Math.PI / 2) * direcaoPerpendicular;
					while(!campoBatalha.contains(this.calcularPonto(angulo, d))) {
						angulo -= direcaoPerpendicular * 0.1;
					}
					this.proximaPosicao = this.calcularPonto(angulo, d);
					
					//Calcula a distancia e angulo para mirar
					double distance = this.posicaoAtual.distance(this.proximaPosicao);
					double moveAngle = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(this.calcularAngulo(this.posicaoAtual, this.proximaPosicao)) - getHeading());
					this.posicaoAnterior = this.posicaoAtual;
					
					//Calcular menor valor para giro e movimento para chegar no ponto
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
		
		System.out.println(e.getName());
		String nome = e.getName();
		Inimigo inimigo;
		
		if(this.inimigos.get(nome) == null) {
			inimigo = new Inimigo(nome,
					this.calcularPonto(Math.toRadians(getHeading() + e.getBearing()), e.getDistance()),
					e.getEnergy(),
					e.getBearing(),
					e.getHeading(),
					e.getVelocity(),
					getHeadingRadians());
		} else {
			inimigo = new Inimigo(nome,
					this.calcularPonto(Math.toRadians(getHeading() + e.getBearing()), e.getDistance()),
					e.getEnergy(),
					e.getBearing(),
					e.getHeading(),
					e.getVelocity(),
					e.getHeadingRadians());
			
		}
		
		this.inimigos.put(nome, inimigo);
		
		if (this.alvo == null || this.alvo.getNome().equals(nome)) {
			this.alvo = inimigo;
		} else {
			Inimigo menorLife = inimigo;
			
			for (String chave : this.inimigos.keySet()) {
				if (this.inimigos.get(chave).getEnergia() < inimigo.getEnergia()) {
					menorLife = this.inimigos.get(chave);
				}
			}
			
			this.alvo = menorLife;
		}
		
		double poderBala = Math.min(500 / this.posicaoAtual.distance(inimigo.getLocalizacao()), 3);
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		 
		double delta = 0;
		double campoBatalhaAltura = getBattleFieldHeight(), 
		       campoBatalhaComprimento = getBattleFieldWidth();
		double predicaoX = inimigo.getLocalizacao().getX();
		double predicaoY = inimigo.getLocalizacao().getY();
		
		while((++delta) * (20.0 - 3.0 * poderBala) < 
		      Point2D.Double.distance(getX(), getY(), predicaoX, predicaoY)){		
			predicaoX += Math.sin(inimigo.getDirecaoRad()) * inimigo.getVelocidade();	
			predicaoY += Math.cos(inimigo.getDirecaoRad()) * inimigo.getVelocidade();
			if(	predicaoX < 18.0 
				|| predicaoY < 18.0
				|| predicaoX > campoBatalhaComprimento - 18.0
				|| predicaoY > campoBatalhaAltura - 18.0){
				predicaoX = Math.min(Math.max(18.0, predicaoX), 
		                    campoBatalhaComprimento - 18.0);	
				predicaoY = Math.min(Math.max(18.0, predicaoY), 
		                    campoBatalhaAltura - 18.0);
				break;
			}
		}
		double teta = Utils.normalAbsoluteAngle(Math.atan2(
		    predicaoX - getX(), predicaoY - getY()));
		 
		setTurnRadarRightRadians(
		    Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
		setTurnGunRightRadians(Utils.normalRelativeAngle(teta - getGunHeadingRadians()));
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10)
			fire(poderBala);
				
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
		//Utiliza algoritmo antigravidade, inimigos com maior energia sao um risco maior
		while(it.hasNext()) {
			Inimigo inimigo = it.next();
			risco += (inimigo.getEnergia() + 50) / ponto.distanceSq(inimigo.getLocalizacao());
		}
		//Foge das ultima localizacoes
		risco += 0.1 / ponto.distanceSq(this.posicaoAnterior);
		risco += 0.1 / ponto.distanceSq(this.posicaoAtual);
		
		return risco;
	}
	
	public double calcularAngulo(Point2D p, Point2D q) {
		return Math.atan2(q.getX() - p.getX(), q.getY() - p.getY());
	}
	
}
