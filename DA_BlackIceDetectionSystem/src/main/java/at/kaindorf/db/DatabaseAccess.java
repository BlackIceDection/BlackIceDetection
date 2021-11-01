package at.kaindorf.db;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

import java.util.List;

/**
 * Handles all reading/writing tasks from/to InfluxDB.
 * 
 * @author Nico Baumann
 */
public class DatabaseAccess{
	
	// database handle
	private static Database db = Database.getInstance();
	
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
	public static void writeLineProtocol(String in){
		try(WriteApi wapi = db.getClient().getWriteApi()){
			wapi.writeRecord(db.getBucket(), db.getOrg(), WritePrecision.NS, in);
		}
	}
	
	/**
	 * Writes data to Influx via Data Point. (Preferred)
	 *
	 * @param point The data point
	 */
	public static void writeDataPoint(Point point){
		try(WriteApi wapi = db.getClient().getWriteApi()){
			wapi.writePoint(db.getBucket(), db.getOrg(), point);
		}
	}
	
	/**
	 * Executes InfluxQL query and returns its result.
	 * @param query The InfluxQL statement
	 * @return The query result
	 */
	public static List<FluxTable> executeQuery(String query){
		//List<FluxTable> tables = db.getClient().getQueryApi().query(query, db.getOrg());
		return db.getClient().getQueryApi().query(query, db.getOrg());
	}
	
}
