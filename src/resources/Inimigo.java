package resources;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Vector;

public class Inimigo {
	
	public String nome;
	public Point2D.Double localizacao;
	public double energia;
	public double bearing;
	public double direcao;
	public Vector<OndaTiros> tiros;
	
	public Inimigo(String nome, Double localizacao, double energia, double bearing, double direcao,
			Vector<OndaTiros> tiros) {
		super();
		this.nome = nome;
		this.localizacao = localizacao;
		this.energia = energia;
		this.bearing = bearing;
		this.direcao = direcao;
		this.tiros = tiros;
	}

}
