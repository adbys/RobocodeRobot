package resources;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public class OndaTiros {
	
	private Point2D.Double origem;
	private Point2D.Double ultimaPosicao;
	private double bearing;
	private double poder;
	private long tempoTiro;
	private int direcao;
	private int[] returnSegment;
	private long tempoRecente;
	
	public OndaTiros(Double origem, Double ultimaPosicao, double bearing, double poder, long tempoTiro, int direcao,
			int[] returnSegment, long tempoRecente) {
		super();
		this.origem = origem;
		this.ultimaPosicao = ultimaPosicao;
		this.bearing = bearing;
		this.poder = poder;
		this.tempoTiro = tempoTiro;
		this.direcao = direcao;
		this.returnSegment = returnSegment;
		this.tempoRecente = tempoRecente;
	}
	
	public double getVelocidadeBala() {
		return 20 - this.poder *3;
	}
	
	public double anguloMaxEscape() {
		return Math.asin(8 / this.getVelocidadeBala());
	}
	
	public boolean waveHit(Point2D.Double inimigo, long tempo) {
		
		long dt = tempo - this.tempoRecente;
		double dx = (inimigo.getX() - this.ultimaPosicao.getX()) / dt;
		double dy = (inimigo.getY() - this.ultimaPosicao.getY()) / dt;
		
		while(this.tempoRecente < tempo) {
			if(this.origem.distance(inimigo) <= (this.tempoRecente - this.tempoTiro) * this.getVelocidadeBala()) {
				double direcaoDesejada = Math.atan2(inimigo.getX() - this.origem.getX(), inimigo.getY() - this.origem.getY());
				double correcaoAngulo = robocode.util.Utils.normalRelativeAngle(direcaoDesejada - bearing);
				double guessFactor = Math.max(-1, Math.min(1, correcaoAngulo / this.anguloMaxEscape())) * this.direcao;
				int index = (int) Math.round((returnSegment.length - 1) / 2 * (guessFactor + 1));
				returnSegment[index]++;
				return true;
			}
			this.tempoRecente++;
			this.ultimaPosicao = new Point2D.Double(this.ultimaPosicao.getX() + dx, this.ultimaPosicao.getY() + dy);
		}
		return false;
	}
	


}
