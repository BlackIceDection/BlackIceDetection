package at.kaindorf.db;

import at.kaindorf.Main;
import at.kaindorf.beans.ConnectionClass;
import at.kaindorf.lua.LuaJ;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;

/**
 * This class handles everything about the database itself and its connection.
 * It's also responsible for reading/writing InfluxDB data.
 * 
 * @author Nico Baumann
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Database extends ConnectionClass{
	
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
	public void connect(){
		try{
			client = InfluxDBClientFactory.create(url, token.toCharArray());
		}
		catch(Exception e){
			if(Main.debugWindow)
				JOptionPane.showMessageDialog(
						null,
						"Failed to connect to InfluxDB.",
						"InfluxDB Connection Error",
						JOptionPane.ERROR_MESSAGE
				);
			
			System.out.println("ERROR: Failed to connect to InfluxDB.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes the database connection and frees resources.
	 */
	public void disconnect(){
		try{
			if(client != null)
				client.close();
		}
		catch(Exception e){
			if(Main.debugWindow)
				JOptionPane.showMessageDialog(
						null,
						"Failed to close connection to InfluxDB.",
						"InfluxDB Connection Error",
						JOptionPane.ERROR_MESSAGE
				);
			
			System.out.println("ERROR: Unable to close InfluxDB connection.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Reloads Lua scripts.
	 */
	public void reload(){
		LuaJ.executeLuaScript("db.lua", new HashMap<String, Object>(){
			{
				put("database", instance);
			}
		});
	}
	
	/**
	 * Disconnects from DB and resets all variables.
	 */
	public void reset(){
		disconnect();
		
		client = null;
		url = username = password = token = bucket = org = "NaN";
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
