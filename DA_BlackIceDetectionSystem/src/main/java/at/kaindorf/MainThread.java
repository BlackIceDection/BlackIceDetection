package at.kaindorf;

import at.kaindorf.db.Database;
import at.kaindorf.mqtt.Mqtt;
import com.influxdb.client.service.CellsService;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Scanner;

/**
 * If the debugging window is disabled, we'll need a Thread to keep the console
 * running. I decided to export this part into its own class to that, if the
 * debug window is enabled, there aren't 2 separate threads running and conflicting.
 *
 * @author Nico Baumann
 */
public class MainThread implements Runnable{

	private final Scanner SCANNER = new Scanner(System.in);

	private static Database db;
	private static Mqtt mqtt;

	@Override
	public void run(){
		db = Database.getInstance();
		mqtt = Mqtt.getInstance();

		try{
			do{
				String s = SCANNER.nextLine();

				switch(s.toLowerCase().trim())
				{
					// print help dialog
					case "help":
						printHelp();
						break;

					case "reload":
						if(stopInflux() == 1)
							System.out.println("WARNING: Failed to stop InfluxDB client.");
						if(stopMqtt() == 1)
							System.out.println("WARNING: Failed to stop MQTT client.");

						if(startInflux() == 1)
							System.out.println("ERROR: Failed to start InfluxDB client.");
						else
							System.out.println("Starting InfluxDB client... success!");
						if(startMqtt() == 1)
							System.out.println("ERROR: Failed to start MQTT client.");
						else
							System.out.println("Starting MQTT client... success!");
						break;

					case "start":
						if(startInflux() == 1)
							System.out.println("ERROR: Failed to start InfluxDB client.");
						else
							System.out.println("Starting InfluxDB client... success!");
						if(startMqtt() == 1)
							System.out.println("ERROR: Failed to start MQTT client.");
						else
							System.out.println("Starting MQTT client... success!");
						break;

					case "stop":
						byte i = stopInflux();
						byte m = stopMqtt();

						if(i == 1)
							System.out.println("WARNING: Failed to stop InfluxDB client.");
						else if(i == 2)
							System.out.println("Failed to stop InfluxDB client: client does not exist.");
						else
							System.out.println("Stopping InfluxDB client... success!");

						if(m == 1)
							System.out.println("WARNING: Failed to stop MQTT client.");
						else if(m == 2)
							System.out.println("Failed to stop MQTT client: client does not exist.");
						else
							System.out.println("Stopping MQTT client... success!");
						break;

					case "start influx":
						if(startInflux() == 1)
							System.out.println("ERROR: Failed to start InfluxDB client.");
						else
							System.out.println("Starting InfluxDB client... success!");
						break;

					case "start mqtt":
						if(startMqtt() == 1)
							System.out.println("ERROR: Failed to start MQTT client.");
						else
							System.out.println("Starting MQTT client... success!");
						break;

					case "stop influx":
						byte stopI = stopInflux();

						if(stopI == 1)
							System.out.println("WARNING: Failed to stop InfluxDB client.");
						else if(stopI == 2)
							System.out.println("Failed to stop InfluxDB client: client does not exist.");
						else
							System.out.println("Stopping InfluxDB client... success!");
						break;

					case "stop mqtt":
						byte stopM = stopMqtt();

						if(stopM == 1)
							System.out.println("WARNING: Failed to stop MQTT client.");
						else if(stopM == 2)
							System.out.println("Failed to stop MQTT client: client does not exist.");
						else
							System.out.println("Stopping MQTT client... success!");
						break;

					case "status":
						System.out.format("  InfluxDB is %sconnected.\n%s\n",
								db.isConnected ? "" : "not ",
								db.isConnected ? db.getConnectionString(false) : "");
						System.out.format("  MQTT client is %sconnected.\n%s\n\n",
								mqtt.isConnected ? "" : "not ",
								mqtt.isConnected ? mqtt.getConnectionString(false) : "");
						break;

					case "exit":
						System.out.println("Stopping the system...");
						try{
							db.disconnect();
							mqtt.disconnect();
						}
						catch(Exception e){}

						System.exit(0);

					default:
						System.out.println("Unknown Command.");
						break;

					// lua not working right, lua command temporarily removed.
				}
			} while(true); // endless loop; good enough for now
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private byte startInflux(){
		try{
			// load lua scripts
			db.reset();
			db.reload();

			// connect to influx
			db.connect();
		}
		catch(Exception e){
			return 1;
		}

		return 0;
	}
	private byte stopInflux(){
		try{
			db.disconnect();
		}
		catch(Exception e){
			if(!db.isConnected) return 2;
			else return 1;
		}

		return 0;
	}

	private byte startMqtt(){
		try{
			// load lua scripts
			mqtt.reset();
			mqtt.reload();

			// start MQTT
			mqtt.connect(null);
		}
		catch(Exception e){
			return 1;
		}

		return 0;
	}
	private byte stopMqtt(){
		try{
			mqtt.disconnect();
		}
		catch(Exception e){
			if(!mqtt.isConnected) return 2;
			return 1;
		}

		return 0;
	}

	private void printHelp(){
		System.out.println("List of commands:");
		System.out.println("   help          prints this dialog");
		System.out.println("   reload        reloads all Lua scripts and refreshes clients");
		System.out.println("   status        shows information about clients");
		System.out.println("   start mqtt    starts MQTT client only");
		System.out.println("   start influx  starts InfluxDB client only");
		System.out.println("   start         starts both clients at once");
		System.out.println("   stop mqtt     stops MQTT client");
		System.out.println("   stop influx   stops InfluxDB client");
		System.out.println("   stop          stops both clients at once");
		System.out.println("   exit          closes all connections and ends the program");
		System.out.println("");
	}

}
