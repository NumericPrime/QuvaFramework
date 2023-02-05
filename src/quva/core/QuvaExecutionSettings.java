package quva.core;

import static quva.core.QuvaUtilities.sumUp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/***/
public class QuvaExecutionSettings implements Serializable{
	/**Determines whether native implementation of the MinorMiner-algorithm
	 * should be used*/
	public static boolean useNative=false;
	private static final long serialVersionUID = -8159190501373912213L;
	public static int standardSetting=0;
	public static Map<String,String> settings=new HashMap<String,String>();
	public Map<String,String> settingsInstance=new HashMap<String,String>();
	/**Selects settings that are automatically added to the mode. Choosing things like AUTOTRUNCATE here. Will make it apply every time when calling execute unless the CUSTOMSETTINGS parameter has been added to execute.<br>
	 * @param settings settings to be applied. Settings that won't make the program out are:<br>
	 * AUTOTRUNCATE<br>
	 * AUTOCHAINSTRENGTH.<br>
	 * The DELAYED and CUSTOMSETTINGS will be ignored.<br>
	 * ANNEAL will make every execution not using CUSTOMSETTINGS run the program on the quantumannealer even if you chose SIMULATE.<br>NONE or choosing the same setting twice will make the program do all kinds of whacky stuff.
	 * @see quva.core.QuvaUtilities#AUTOTRUNCATE
	 * @see quva.core.QuvaUtilities#AUTOCHAINSTRENGTH
	 * @see quva.core.ExecuteProgram#execute(int, boolean, QUBOMatrix)*/
	public static void executionSettings(int... settings) {
		standardSetting=sumUp(settings);
		QuvaExecutionSettings.settings.put("execMode",standardSetting+"");
	}
	public static PrintStream ps;
	static {
		try {
			ps=new PrintStream(new File(ExecuteProgram.execScriptsRoot,"options.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ps=null;
		}
		samples(7500);
		settings.put("simAnnealingStartingTemp","auto");
		settings.put("simAnnealingIterations","1000");
		settings.put("simAnnealingTempChange","0.99");
		settings.put("simAnnealingSamples","500");
	}
	/**Sets the API-Token for execution
	 * @param token token to be used*/
	public static void apiToken(String token) {
		writeSetting("token",token);
	}
	/**Sets the number of samples for execution
	 * @param count number of samples. Must be between 1 and 10000*/
	public static void samples(int count) {
		if(count<=0) System.err.println("Warning: Negative or zero samples won't work!");
		if(count>10000) System.err.println("Warning: A count higher than 10000 won't work!");
		writeSetting("samples",count+"");
	}
	/**Adds a setting.
	 * @param setting setting to be used. Currently allowed settings are "token" and "samples"=7500 for execution on real Quantumannealers.<br>
	 * Also for simulating using the INTERNAL mode there are four settings. "simAnnealingStartingTemp"=700,
	 *  "simAnnealingIterations"=1000, "simAnnealingIterations"=1000,"simAnnealingTempChange"=0.9 (multiplier applied to the temperatur at the end of each iteration),
	 *  "simAnnealingSamples"=500.
	 * @param value value to be used for the setting
	 * @see quva.core.QuvaExecutionSettings#executionSettings(int[])
	 * @see quva.core.QuvaExecutionSettings#apiToken(String)*/
	public static void writeSetting(String setting,String value) {
		ps.println(setting+":"+value);
		settings.put(setting,value);
	}
	/**Resets all settings*/
	public static void reset() {
		try {
			ps=new PrintStream(new File(ExecuteProgram.execScriptsRoot,"options.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ps=null;
		}
		settings.clear();
		standardSetting=0;
	}
	/**Creates an Object holding the settings
	 * @param settings this holds all settings. You can add the "token" and "samples" settings. Also you can use "execMode" to put a single number representing the execution mode.*/
	public QuvaExecutionSettings(Map<String,String> settings){
		for(String s:settings.keySet()) settingsInstance.put(s,settings.get(s));
	}
	/**Creates an object holding  all the settings
	 * @return QuvaExecutionSettings object with all the settings
	 * @see quva.core.QuvaExecutionSettings#save(String)
	 * @see quva.core.QuvaExecutionSettings#pull()*/
	public static QuvaExecutionSettings push() {
		return new QuvaExecutionSettings(settings);
	}
	/**Reads and applies all settings read from the object
	 * @see quva.core.QuvaExecutionSettings#push()*/
	public void pull() {
		reset();
		for(String s:settingsInstance.keySet()) writeSetting(s,settingsInstance.get(s).replaceAll("\r",""));
		standardSetting=Integer.parseInt(settings.get("execMode").replaceAll("\r",""));
	}
	/**Saves the settings into a file
	 * @param path the absolute path of the file
	 * @see quva.core.QuvaExecutionSettings#loadFile(String)*/
	public void save(String path) {
		try {
			PrintStream st=new PrintStream(new File(path));
			for(String s:settings.keySet()) st.println(s+":"+settings.get(s));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	/**Loads a native implementation of the MinorMiner-algorithm
	 * @param path path to the .dll/.so file containing the JNI Code. A .dll file for Windows is already implemented 
	 * and can be accessed with the constant quva.core.QuvaUtilities.WINDOWS. Although it might not work on some machines.
	 * You can also generate the the shared object yourself by compiling /jni/include/find_embedding/dwimp_main_MinorMinerImplementation.cpp
	 * (This compiles a DWave-Library for generating a embedding) This file must be put in pythonScriptRoots. And the relative path to wich can be 
	 * used as argument.*/
	public static void loadNative(String path) {
		dwas.main.MinorMinerImplementation.loadNative(path);
		}
	/**Loads the settings from a file generated by {@link quva.core.QuvaExecutionSettings#save(String)}
	 * @param path the absolute path of the file
	 * @return object that holds the settings
	 * @see quva.core.QuvaExecutionSettings#save(String)*/
	public static QuvaExecutionSettings loadFile(String path) {
		try {
			Map<String,String> loaded=new HashMap<String,String>();
			String content=QuvaUtilities.readFile(path);
			String[] lines=content.split("\n");
			for(String s:lines) loaded.put(s.split(":")[0], s.split(":")[1].replaceAll("\r",""));
			return new QuvaExecutionSettings(loaded);
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
