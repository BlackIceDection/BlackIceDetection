package at.kaindorf.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.util.Map;

/**
 * Handles everything Lua-related.
 */
public class LuaJ{
	
	// the path to the Lua scripts
	private static final String LUA_PATH = System.getProperty("user.dir")
			+ File.separator + "src"
			+ File.separator + "main"
			+ File.separator + "resources"
			+ File.separator + "script"
			+ File.separator;
	
	/**
	 * Executes given Lua scripts.
	 * @param script The script's filename
	 * @param objects The objects to parse to Lua (for usage within the given script)
	 */
	public static void executeLuaScript(String script, Map<String, Object> objects){
		// create default global values for lua
		Globals globals = JsePlatform.standardGlobals();
		
		// add all parsed classes (if present)
		if(objects != null){
			for(String name : objects.keySet()){
				globals.set(name, CoerceJavaToLua.coerce(objects.get(name)));
			}
		}
		
		// load Lua script
		LuaValue chunk = globals.loadfile(LUA_PATH + script);
		// execute Lua code
		chunk.call();
	}
	
}
