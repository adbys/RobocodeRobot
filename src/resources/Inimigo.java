package resources;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Vector;

public class Inimigo {
	
	private String nome;
	private Point2D.Double localizacao;
	private double energia;
	private double bearing;
	private double direcao;
	
	public Inimigo(String nome, Double localizacao, double energia, double bearing, double direcao) {
		super();
		this.nome = nome;
		this.localizacao = localizacao;
		this.energia = energia;
		this.bearing = bearing;
		this.direcao = direcao;

	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Point2D.Double getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Point2D.Double localizacao) {
		this.localizacao = localizacao;
	}

	public double getEnergia() {
		return energia;
	}

	public void setEnergia(double energia) {
		this.energia = energia;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public double getDirecao() {
		return direcao;
	}

	public void setDirecao(double direcao) {
		this.direcao = direcao;
	}
	

}
