package at.kaindorf.db;

import at.kaindorf.lua.LuaJ;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * This class handles everything about the database itself and its connection.
 * It's also responsible for reading/writing InfluxDB data.
 * 
 * @author Nico Baumann
 */
@Data
public class Database{
	
	private static Database instance = null; // Instance of this class
	public static boolean firstRun = true; // Soft reload, used for Lua.
	
	private InfluxDBClient client = null; // InfluxDB client
	
	private String url;         // Database URL.
	private String username;    // Username of user (admin).
	private String password;    // Password of user (admin).
	private String org;         // Organization of the user.
	private String token;       /** Private access token for database connection. Value in {@code db.lua}.*/
	private String bucket;      // Influx-Bucket.
	private String measurement; // Measurement for sending/receiving data from Influx.
	
	/**
	 * Instance handler.
	 * @return Instance of this class.
	 */
	public static Database getInstance(){
		if(instance == null) instance = new Database(); // If there's no instance yet, create one.
		return instance;
	}
	
	/**
	 * Sets up first-time database connection.
	 */
	public Database(){
		/**
		 * Since {@code reset()} refreshes all variables and resets the client anyways, we can just use it here in the
		 * constructor for initialization.
		 */
		reset();
	}
	
	/**
	 * Establishes connection to InfluxDB.
	 */
	public void connect() throws Exception{
		// Create new client that connects to Influx via URL and the private access token.
		client = InfluxDBClientFactory.create(url, token.toCharArray());
	}
	
	/**
	 * Closes the database connection and frees resources.
	 */
	public void disconnect() throws Exception{
		// If client has already been created, close the connection.
		if(client != null)
			client.close();
		// Otherwise, throw an exception, just for fun.
		else
			throw new RuntimeException("No client present");
	}
	
	/**
	 * Reloads Lua scripts.
	 */
	public void reload() throws Exception{
		// Pass the instance of this class to the 'db.lua' script and execute it.
		LuaJ.executeLuaScript("db.lua", new HashMap<String, Object>(){
			{
				put("database", instance);
			}
		});
	}
	
	/**
	 * Resets all variables and connections.
	 */
	public void reset(){
		// If client exists, close connection first and then reset the client.
		if(client != null){
			client.close();
			client = null;
		}
		
		// Reset all relevant variables.
		url = username = password = token = bucket = org = "NaN";
	}
	
	/**
	 * Returns a string containing information about the database (connection).
	 * Only for debugging purposes.
	 * @param html Whether the string should be formatted using HTML (for JLabel).
	 * @return The information string.
	 */
	public String getConnectionString(boolean html){
		// Only construct a string if client has been created.
		if(client != null){
			// Construct a plain, boring string.
			if(!html)
				return String.format(
						"URL: %s\nUser:%s\nBucket:%s, Org: %s",
						url, username, bucket, org
				);
			// Construct a HTML formatted string.
			else
				return String.format(
						"<html>URL: %s<br/><br/>User: %s<br/><br/>Bucket: %s<br/><br/>Org: %s</html>",
						url, username, bucket, org
				);
		}
		
		return "Not connected.";
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////
	// COMMUNICATION
	////////////////////////////////////////////////////////////////////////////
	
	/*
	 * We have the following data:
	 *  - Air Pressure (Field Key) - type Float
	 *  - Inner Temperature (Field Key) - type Float
	 *  - Outer Temperature (Field Key) - type Float 
	 *  - Humidity (Field Key) - type Float
	 *  - Light (Field Key) - type Float
	 */
	
	/**
	 * Writes data to Influx directly via InfluxDB's Line Protocol.
	 * Untested because we found a better way to do it, but still leaving this in just because.
	 * @param in The line protocol data.
	 */
	public void writeLineProtocol(String in){
		try(WriteApi wapi = client.getWriteApi()){
			wapi.writeRecord(bucket, org, WritePrecision.NS, in);
		}
	}
	
	/**
	 * Writes data to Influx via Data Point.
	 * This is the preferred (and easiest) way to write data to the database.
	 * @param point The data point to write.
	 */
	public void writeDataPoint(Point point){
		try(WriteApi wapi = client.getWriteApi()){
			wapi.writePoint(bucket, org, point);
		}
	}
	
	/**
	 * TODO: finally finish and test this.
	 */
	/**
	 * Executes InfluxQL query and returns its result.
	 * @param query The InfluxQL statement.
	 * @return The query result.
	 */
	public List<FluxTable> executeQuery(String query){
		//List<FluxTable> tables = db.getClient().getQueryApi().query(query, db.getOrg());
		return client.getQueryApi().query(query, org);
	}
	
}
