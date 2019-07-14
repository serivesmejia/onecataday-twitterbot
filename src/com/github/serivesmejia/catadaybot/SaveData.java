package com.github.serivesmejia.catadaybot;

import java.util.ArrayList;

public class SaveData {

	public int gatosConseguidos = 0;
	public int nivel = 1;
	public boolean comidaEspecial = false;
	
	public String lastGatoObtenidoDate = "never";
	public String obtenerProximoGatoDate = "undefined";
	
	public ArrayList<GatoStack> gatosStacks = new ArrayList<GatoStack>();
	
	public SaveData(int gatosConseguidos, int nivel, ArrayList<GatoStack> gatosStack, boolean comidaEspecial, String lastGatoObtenidoDate, String obtenerProximoGatoDate) {
		if(gatosStack.size() != 120) return;
		
		for(GatoStack gs : gatosStack) {
			this.gatosStacks.add(gs);
		}
		
		this.gatosConseguidos = gatosConseguidos;
		this.nivel = nivel;
		this.comidaEspecial = comidaEspecial;
		this.obtenerProximoGatoDate = obtenerProximoGatoDate;
		this.lastGatoObtenidoDate = lastGatoObtenidoDate;
		
	}
}