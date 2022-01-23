package at.kaindorf.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.util.Map;

/**
 * Reads and executes Lua scripts.
 * 
 * By the way, here an explanation as to why we decided to use Lua.
 * 1. To speed up development of the backend.
 * 2. To use less static code/variables
 * 3. To make maintaining this project easier.
 * 
 * Lua is very simplistic, fast and especially easy to implement. Rather than defining static strings or asking for
 * user input like database URL and login data every single time the server (re)starts, we just use a simple Lua
 * script that sets these variables for us.
 * 
 * This allows other people, who have never worked on this project, to easily modify values used for connections, as an
 * example. There's no need to recompile the program every single time a variable changes, in fact, you don't even
 * need to restart the backend. Everything can be done on the fly with a simple command to reload the scripts.
 * 
 * Admittedly though, a simple, plaintext '.properties' file would've done the job as well. Then you wouldn't even have
 * to understand how lua variables work. However, Lua was simpler for us developers to use/implement :3.
 * 
 * @author Nico Baumann
 */
public class LuaJ{
	// TODO: Add encryption options for Lua scripts to up security; they're currently in plaintext.
	// TODO: Nevermind. We would have to provide an en-/decoder for DCCS if they needed to change something (or a more elegant solution), but we currently don't have the time to implement this anymore, unfortunately.
	
	// The absolute path to the Lua script location.
	/*private static String LUA_PATH = System.getProperty("user.dir")
			+ File.separator + "src"
			+ File.separator + "main"
			+ File.separator + "resources"
			+ File.separator + "script"
			+ File.separator;
	*/
	private static String LUA_PATH = System.getProperty("user.dir")
			+ File.separator
			+ "script"
			+ File.separator;
	
	/**
	 * Executes given Lua script, passing all objects from the map to the script.
	 * @param script The script's filename.
	 * @param objects The objects to parse to Lua (for usage within the given script only).
	 */
	public static void executeLuaScript(String script, Map<String, Object> objects){
		// Create default global values for Lua.
		Globals globals = JsePlatform.standardGlobals();
		
		// Add all passed objects (if present).
		if(objects != null){
			for(String name : objects.keySet()){
				globals.set(name, CoerceJavaToLua.coerce(objects.get(name)));
			}
		}
		
		// Load the Lua script.
		LuaValue chunk = globals.loadfile(LUA_PATH + script);
		// Execute Lua instructions.
		chunk.call();
	}
	
}
