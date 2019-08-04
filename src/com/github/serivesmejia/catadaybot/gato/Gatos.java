package com.github.serivesmejia.catadaybot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Gatos {

	Gato[] gatos;
	
	public Gatos(Gato[] gatos) {
		this.gatos = gatos;
	}
	
	public String toJson() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this).replace("\\u0026", "y").replace("\r", "");
	}
	
	
}
