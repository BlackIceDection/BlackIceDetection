package at.kaindorf.db;

import at.kaindorf.lua.LuaJ;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

import java.util.HashMap;

/**
 * This class handles everything about the database itself and its connection.
 * SQL statements however, are handled by the external class {@link at.kaindorf.db.DatabaseAccess}.
 * 
 * @author Nico Baumann
 */
public class Database{
	
	// instance handle
	private static Database instance = null;
	
	// check for soft reload
	public boolean firstRun = true;
	
	// database client
	private InfluxDBClient client;
	
	// url to db
	private String url;
	// db username
	private String username;
	// db password
	private String password;
	// access token
	private String token;
	// influx bucket
	private String bucket;
	// users organization
	private String org;
	
	public static Database getInstance(){
		if(instance == null) instance = new Database();
		return instance;
	}
	
	/**
	 * Reads properties from .properties file and connects to database as client.
	 */
	public Database(){
		// execute boot script containing various (especially database-related)
		// instructions needed for starting up
		reloadStartupScript();
		
		// connect to database
		client = InfluxDBClientFactory.create(url, token.toCharArray());
	}
	
	/**
	 * Reloads the "boot.lua" script on demand, just in case something goes 
	 * horribly wrong and the application needs to restart.
	 */
	public void reloadStartupScript(){
		try{
			LuaJ.executeLuaScript("boot.lua", new HashMap<String, Object>(){
				{
					put("database", instance);
				}
			});
		}
		catch(Exception e){
			System.out.println("FATAL: Failed to execute boot script, aborting...");
			System.exit(1);
		}
	}
	
	/**
	 * Closes the database connection and frees resources.
	 */
	public void close(){
		client.close();
	}
	
	/**
	 * Resets the entire Backend.
	 */
	public void resetAll(){
		
	}
	
	/**
	 * Resets the Database only (not the database itself; just resets all variables and reconnects).
	 */
	public void reset(){
		
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////
	// GETTER & SETTER
	////////////////////////////////////////////////////////////////////////////
	
	public InfluxDBClient getClient(){
		return client;
	}
	
	public void setClient(InfluxDBClient client){
		this.client = client;
	}
	
	public String getUrl(){
		return url;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public String getUsername(){
		return username;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public String getToken(){
		return token;
	}
	
	public void setToken(String token){
		this.token = token;
	}
	
	public String getBucket(){
		return bucket;
	}
	
	public void setBucket(String bucket){
		this.bucket = bucket;
	}
	
	public String getOrg(){
		return org;
	}
	
	public void setOrg(String org){
		this.org = org;
	}
	
}
