package at.kaindorf.db;

import at.kaindorf.io.FileAccess;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * This class handles everything about the database itself and its connection.
 * SQL statements however, are handled by the external class {@link at.kaindorf.db.DatabaseAccess}.
 * 
 * @author Nico Baumann
 */
public class Database{
	
	// instance handle
	private static Database instance = null;
	
	// database information
	private Properties prop;
	
	// database client
	private InfluxDBClient client = null;
	
	public static Database getInstance(){
		if(instance == null) instance = new Database();
		return instance;
	}
	
	/**
	 * Reads properties from .properties file and connects to database as client.
	 */
	public Database(){
		// get database properties
		// if it fails, exit program
		try{
			getProperties();
		}
		catch(IOException ioE){
			System.out.println("ERROR: Failed to obtain database properties, aborting...");
			System.exit(1);
		}
		
		// connect to database
		client = InfluxDBClientFactory.create(getUrl(), prop.getProperty("token").toCharArray());
	}
	
	/**
	 * Retrieves all properties from the properties file and saves it as {@code Properties} object.
	 * @throws IOException If the Properties file could not be located
	 */
	private void getProperties() throws IOException{
		prop = FileAccess.loadDbProperties("database/db.properties");
	}
	
	/**
	 * Returns the InfluxDB client.
	 * @return The client
	 */
	public InfluxDBClient getClient(){
		return client;
	}
	
	/**
	 * Retrieves any property from the {@code Properties} object and returns it as {@code String}.
	 * @param p The property name
	 * @return The property value
	 */
	public String getProperty(String p){
		return prop.getProperty(p);
	}
	
	/**
	 * Get URL of Influx connection
	 * @return The URL
	 */
	public String getUrl(){
		return prop.getProperty("url");
	}
	
	/**
	 * Get the current user's username
	 * @return The username
	 */
	public String getUsername(){
		return prop.getProperty("username");
	}
	
	/**
	 * Get the current bucket
	 * @return The bucket
	 */
	public String getBucket(){
		return prop.getProperty("bucket");
	}
	
	/**
	 * Get the current user's organization
	 * @return The organization
	 */
	public String getOrg(){
		return prop.getProperty("org");
	}
	
	/**
	 * Closes the database connection and frees resources.
	 */
	public void close(){
		client.close();
	}
	
}
