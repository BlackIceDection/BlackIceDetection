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
	
	// instance handle
	private static Database instance = null;
	
	// check for soft reload
	public static boolean firstRun = true;
	
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
		reset();
	}
	
	/**
	 * Establishes connection to InfluxDB.
	 */
	public void connect() throws Exception{
		client = InfluxDBClientFactory.create(url, token.toCharArray());
	}
	
	/**
	 * Closes the database connection and frees resources.
	 */
	public void disconnect() throws Exception{
		if(client != null)
			client.close();
		else
			throw new RuntimeException("No client present");
	}
	
	/**
	 * Reloads Lua scripts.
	 */
	public void reload() throws Exception{
		LuaJ.executeLuaScript("db.lua", new HashMap<String, Object>(){
			{
				put("database", instance);
			}
		});
	}
	
	/**
	 * Resets all variables
	 */
	public void reset(){
		client = null;
		url = username = password = token = bucket = org = "NaN";
	}
	
	public String getConnectionString(boolean html){
		if(client != null){
			if(!html)
				return String.format(
						"URL: %s\nUser:%s\nBucket:%s, Org: %s",
						url, username, bucket, org
				);
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
	
	/**
	 * We have the following data:
	 *  - Air Pressure (Field Key) - type Float
	 *  - Temperature (Field Key) - type Float
	 *  - Humidity (Field Key) - type Float
	 *  - Light (Field Key) - type Float
	 */
	
	/**
	 * Writes data to Influx directly via InfluxDB's Line Protocol.
	 *
	 * @param in The line protocol data
	 */
	public void writeLineProtocol(String in){
		try(WriteApi wapi = client.getWriteApi()){
			wapi.writeRecord(bucket, org, WritePrecision.NS, in);
		}
	}
	
	/**
	 * Writes data to Influx via Data Point. (Preferred)
	 *
	 * @param point The data point
	 */
	public void writeDataPoint(Point point){
		try(WriteApi wapi = client.getWriteApi()){
			wapi.writePoint(bucket, org, point);
		}
	}
	
	/**
	 * Executes InfluxQL query and returns its result.
	 * @param query The InfluxQL statement
	 * @return The query result
	 */
	public List<FluxTable> executeQuery(String query){
		//List<FluxTable> tables = db.getClient().getQueryApi().query(query, db.getOrg());
		return client.getQueryApi().query(query, org);
	}
	
}
