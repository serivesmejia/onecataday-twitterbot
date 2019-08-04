package com.github.serivesmejia.catadaybot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;

import com.github.serivesmejia.catadaybot.gato.Gato;
import com.github.serivesmejia.catadaybot.gato.GatoStack;
import com.github.serivesmejia.catadaybot.gato.Gatos;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.sf.corn.httpclient.HttpClient;
import net.sf.corn.httpclient.HttpClient.HTTP_METHOD;
import net.sf.corn.httpclient.HttpForm;
import net.sf.corn.httpclient.HttpResponse;


public class Main {

	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static Gatos gatos;
	
	
	public static String APP_VERSION = "1.0.0";
	
	//Variables: $keyA = Maker Key
	//			 $value1A = Contenido del tweet
	//			 $catidA = ID del gato
	public static String iftttMakerUrl = "https://maker.ifttt.com/trigger/TwitterCatADay/with/key/$keyA?";
	public static String catImagesURL = "https://cataday.000webhostapp.com/g/$catidA.png";
	
	public static String iftttMakerKey = "";
	
	public static File P_APPDATA;
	public static File P_SAVEDATA;
	
	public static String[] javaArgs;
	
	Timer t = new Timer();
	
	public static SaveData save;
	
	public static Random random = new Random();
			
	public static void main(String[] args) throws IOException, URISyntaxException {
		javaArgs = args;
		
		String prefix = "-ifttt.key=";
		
		String arg = null;
		
		for(String a : javaArgs) {
			if(a.startsWith(prefix)) {
				arg = a.trim();
				break;
			}
		}
		
		if(arg == null) {
			System.out.println("ERROR: No se ha especificado el commandline arg -ifttt.key\nLa aplicacion terminara ahora (EXIT CODE: 1)");
			System.exit(1);
		}
		
		if(arg.replace(prefix, "").length() == 0) {
			System.out.println("ERROR: No se ha especificado el valor del commandline arg -ifttt.key\nLa aplicacion terminara ahora (EXIT CODE: 1)");
			System.exit(1);
		}
		
		
		iftttMakerKey = arg.replace(prefix, "");
		
		System.out.println("\"1 Cat a Day Twitter Bot\" " + APP_VERSION + "\n");
		P_APPDATA = defaultDir();
		System.out.println("AppData: " + P_APPDATA);
		System.out.println("\nCargando gatos del archivo gatos-cataday-twitter.json");
		
		String gatosJson = null;
		
		P_SAVEDATA = new File(P_APPDATA + "/save.json");
		P_APPDATA.mkdirs();
		
		try {
			gatosJson = isToString(Main.class.getResourceAsStream("/gatos-cataday-twitter.json"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		gatos = gson.fromJson(gatosJson, Gatos.class);
		if(gatos.gatos.length == 120) {
			System.out.println("\nGatos totales: 120/120 OK\n");
		}else{
			System.out.println("\nGatos totales: "+ gatos.gatos.length +"/120 ERROR!\nLa aplicacion terminara ahora (EXIT CODE: 1)");
			System.exit(1);
		}
		
		if(P_SAVEDATA.exists()) {
			
			if(!testIFTTT()) {
				System.out.println("ERROR: Intenta usar una maker key valida e intenta de nuevo.\nLa aplicacion terminara ahora (EXIT CODE: 1)");
				System.exit(1);
			}
			
			System.out.println("testIFTTT() = true");
			
			try {
				System.out.println("\nCargando archivo de guardado...");
				save = gson.fromJson(readFile(P_SAVEDATA.toPath(), Charset.defaultCharset()), SaveData.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			String tweetContent = "Bienvenido al bot de Twitter de 1 Cat a Day!\n\n- Llegara un nuevo gato cada dia a las 16:00 UTC\n- La comida que se dejara en la puerta sera aleatoria.\n- Hay 30% de probabilidad de que salga un gato repetido.\n- Si un gato sale repetido, se entregara al cocinero del barrio. - Cuando la barra del cocinero del barrio llegue al 100%, se dejara la comida especial en la puerta el siguiente dia.";
			String res = TwitterPost(tweetContent, 0);
			
			if(res.contains("{\"errors\":[{\"message\":\"You sent an invalid key.\"}]}")) {
				System.out.println("ERROR: " + res);
				System.out.println("Intenta usar una maker key valida e intenta de nuevo.\nLa aplicacion terminara ahora (EXIT CODE: 1)");
				System.exit(1);
			}
			
			System.out.println("\nEl archivo de guardado no existe! Empezando una partida nueva...");
			System.out.println("Se ha twitteado el mensaje de bienvenida.");
			ArrayList<GatoStack> gatosStacks = new ArrayList<GatoStack>();
			for(Gato g : gatos.gatos) {
				gatosStacks.add(new GatoStack(g.ID, 0));
			}
			
			save = new SaveData(0, 1, gatosStacks, false, "never", "undefined");
			try {
				FileWriter fw = new FileWriter(P_SAVEDATA);
				fw.append(gson.toJson(save));
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("\nCarga terminada!\nPara cerrar el programa de forma segura, escribe /stop\nPara ver una lista completa de los comandos, escribe /help");
		
		Scanner scanner = new Scanner(System.in);
		

		
		while(true) {
        	String input = scanner.nextLine();
        	
			if(!input.trim().equals("/stop") && !input.trim().equals("/help") && !input.trim().equals("/reset") || !input.startsWith("/")) {
				System.out.println("Introduciste un comando no valido, usa /help para ver la lista de comandos.");
			}else if(input.trim().equals("/stop")){
				System.out.println("Guardando archivo de guardado...");
				try {
					FileWriter fw = new FileWriter(P_SAVEDATA);
					fw.append(gson.toJson(save));
					fw.close();
				} catch (IOException e) {
					System.out.println("No se ha podido guardar el archivo de guardado.");
					e.printStackTrace();
					System.exit(0);
				}
				
				System.out.println("Saliendo del programa... (EXIT CODE: 0)");
				System.exit(0);
			}else if(input.trim().equals("/help")){
				System.out.println("Lista de comandos:\n/stop - Guarda tu progreso y cierra el programa\n/reset - Borra el archivo de guardado y empieza desde 0");
			}else if(input.trim().equals("/reset")) {
				System.out.println("Borrando el archivo de guardado...");
				Files.delete(P_SAVEDATA.toPath());
				
				System.out.println("El archivo de guardado no existe! Empezando una partida nueva...");
				
				String tweetContent = "Bienvenido al bot de Twitter de 1 Cat a Day!\n\n- Llegara un nuevo gato cada dia a las 16:00 UTC\n- La comida que se dejara en la puerta sera aleatoria.\n- Hay 30% de probabilidad de que salga un gato repetido.\nPara una lista completa de las reglas del bot, entra a "
						+ "";
				String res = TwitterPost(tweetContent, 0);
				
				System.out.println("Se ha twitteado el mensaje de bienvenida.");
				ArrayList<GatoStack> gatosStacks = new ArrayList<GatoStack>();
				for(Gato g : gatos.gatos) {
					gatosStacks.add(new GatoStack(g.ID, 0));
				}
				
				save = new SaveData(0, 1, gatosStacks, false, "never", "undefined");
				
				try {
					FileWriter fw = new FileWriter(P_SAVEDATA);
					fw.append(gson.toJson(save));
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static String isToString(InputStream is) throws IOException{
		StringBuilder sb = new StringBuilder();
		Reader reader = new BufferedReader(new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name())));
		
		int ch = 0;
		while((ch = reader.read()) != -1) {
			sb.append((char)ch);
		}
		
		return sb.toString();
	}

	public static File defaultDir() {
		String OS = System.getProperty("os.name").toUpperCase();
		
		System.out.println("OS: " + OS);
		if(OS.contains("WIN")) {
			return new File(System.getenv("APPDATA") + "/cataday");
		}else if(OS.contains("MAC")){
			return new File(System.getProperty("user.home") + "/Library/Application/cataday");
		}else if(OS.contains("NUX")) {
			return new File(System.getProperty("user.home") + "/cataday");
		}
		return new File(System.getProperty("user.dir") + "/cataday");
	}
	
    public static String readFile(Path path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, encoding);
    }
    
    public static String urlPaginaIfttt(){
    	return iftttMakerUrl.replace("$keyA", iftttMakerKey);
    }
    
    public static String TwitterPost(String tweetContent, int imageID) throws IOException, URISyntaxException {
    	HttpForm form = new HttpForm(new URI(urlPaginaIfttt()));
    	form.putFieldValue("value1", tweetContent);
    	form.putFieldValue("value2", catImagesURL.replace("$catidA", String.valueOf(imageID)));
    	HttpResponse response = form.doPost();
    	
    	return response.getData();
    }
    
    public static boolean testIFTTT() throws URISyntaxException, IOException {
    	
    	ArrayList<Character> charac = new ArrayList<Character>();
    	
    	for(int i = 0; i < 6; i++) {
    		int a = ThreadLocalRandom.current().nextInt(65, 90);
    		charac.add(Character.valueOf((char) a));
    	}
    	
    	for(int i = 0; i < 6; i++) {
    		int a = ThreadLocalRandom.current().nextInt(97, 122);
    		charac.add(Character.valueOf((char) a));
    	}
    	
    	StringBuilder sb = new StringBuilder();
    	
    	for(Character chara : charac) {
    		sb.append(chara.charValue());
    	}
    	
    	System.out.println("Caracteres aleatorios: " + sb.toString());
    	
    	HttpForm form = new HttpForm(new URI(urlPaginaIfttt().replace("TwitterCatADay", sb.toString())));
    	HttpResponse response = form.doPost();
    	
		String res = response.getData();;
		
		if(res.contains("{\"errors\":[{\"message\":\"You sent an invalid key.\"}]}")) {
			return false;
		}
		return true;
    }
	
}
