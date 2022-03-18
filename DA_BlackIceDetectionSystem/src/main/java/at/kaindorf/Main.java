package at.kaindorf;

import at.kaindorf.db.Database;
import at.kaindorf.lua.LuaJ;
import at.kaindorf.mqtt.Mqtt;
import at.kaindorf.ui.DebugGUI;
import lombok.Data;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Data
/**
 * The Main class of this project.
 * Uses its own thread to keep running, even without GUI.
 */
public class Main{

	// main thread running command line args
	private static MainThread mainThread;

	public static Main instance; // Instance of this class, only relevant for Lua.

	// whether it has already loaded the main lua script
	public static boolean firstRun = true;

	// whether to show the debugging window
	public static boolean debugWindow;
	// the actual debugging window
	public static DebugGUI debugFrame = null;

	// Lua scripts location
	public static String loc_lua = "";

	// Lua scripts that should be executed after 'boot.lua'
	private String[] chainScripts;

	// InfluxDB connection instance
	private static Database db;
	// MQTT client/sender/receiver instance
	private static Mqtt mqtt;

	public static void main(String[] args){
		instance = new Main();

		// locate lua scripts
		// code to read a .properties file removed, just force the user
		// to have the scripts folder in the working directory of the program.
		// path is defined in at.kaindorf.lua.LuaJ.java

		// just use the reload method to initialize everything needed
		instance.reload();

		if(!debugWindow){
			System.out.println("Program started. Type 'exit' to shutdown.");
			mainThread = new MainThread();
			mainThread.run();
		}
		else{
			debugFrame = new DebugGUI();
			debugFrame.init();
		}
	}



	/**
	 * Executes all Lua scripts needed for startup and instantiates all necessary variables.
	 */
	public void reload(){
		try{
			System.out.println("executing 'boot.lua'...");
			// execute boot script
			LuaJ.executeLuaScript("boot.lua", new HashMap<String, Object>(){
				{
					put("main", instance);
				}
			});
			System.out.println("success!");

			// set up Influx
			System.out.println("creating database instance...");
			db = Database.getInstance();
			// set up MQTT
			System.out.println("creating MQTT instance...");
			mqtt = Mqtt.getInstance();

			System.out.println("adding instances to Lua object map...");
			// map containing all the objects to parse to Lua
			HashMap<String, Object> luaObjects = new HashMap<String, Object>(){
				{
					put("database", db);
					put("mqtt", mqtt);
				}
			};
			;
			// list containing all Lua scripts to execute after 'boot.lua'
			List<String> scripts = new ArrayList<>(Arrays.asList(chainScripts));

			// set up debugging window if necessary and append its instance to the Lua object map
			// and execute debugging window's script as well
			if(debugWindow){
				System.out.println("setting up debug window...");
				debugFrame = new DebugGUI();

				luaObjects.put("frame", debugFrame);
				scripts.add("gui.lua");
			}

			// execute scripts
			for(String script : scripts){
				System.out.format("executing script '%s'... ", script);
				LuaJ.executeLuaScript(script, luaObjects);
				System.out.println("success!");
			}
		}
		catch(Exception e){
			String message = "Failed to boot application, aborting...";
			if(debugWindow){
				JOptionPane.showMessageDialog(
						null,
						message,
						"",
						JOptionPane.ERROR_MESSAGE
				);
			}
			System.out.println("FATAL: " + message);
			System.out.println(e.toString());

			System.exit(1);
		}

		if(debugWindow) debugFrame.init();
	}

	/**
	 * Closes all connections and clears variables before shutting down/resetting.
	 */
	public static void reset(){
		// disconnect database if necessary
		if(db != null){
			try{
				db.disconnect();
			}
			catch(Exception e){
				String message = "Failed to disconnect database while performing reset, skipping...";
				if(Main.debugWindow){
					JOptionPane.showMessageDialog(
							null,
							message,
							"Influx Connection Error",
							JOptionPane.ERROR_MESSAGE
					);
				}
				System.out.println("WARNING: " + message);
				System.out.println(e.toString());
			}
		}

		// disable MQTT if necessary
		if(mqtt != null){
			try{
				mqtt.disconnect();
			}
			catch(Exception e){
				String message = "Failed to disconnect MQTT client while performing reset, skipping...";
				if(Main.debugWindow){
					JOptionPane.showMessageDialog(
							null,
							message,
							"MQTT Connection Error",
							JOptionPane.ERROR_MESSAGE
					);
				}
				System.out.println("WARNING: " + message);
				System.out.println(e.toString());
			}
		}

		// close debugging window if necessary
		if(debugFrame != null){
			debugFrame.dispose();
		}

		// reset instances
		db = null;
		mqtt = null;
		debugFrame = null;
	}

	public void mqttFailed(){
		debugFrame.setMqttState(false);

		JOptionPane.showMessageDialog(
				null,
				"MQTT Connection lost.",
				"MQTT Connection Error",
				JOptionPane.ERROR_MESSAGE
		);
	}

	/**
	 * Tests DB connection by reading and writing data to InfluxDB.
	 *//*
	private static void test_db(){
		Database db = Database.getInstance();
		Mqtt mqtt = new Mqtt();

		Scanner scan = new Scanner(System.in);

		LOOP:
		while(true){
			System.out.println(" (1) Print table data");
			System.out.println(" (2) Add data entry");
			System.out.println(" (3) Send MQTT Message");

			System.out.println(" (9) Exit");

			int in;

			try{
				in = Integer.parseInt(scan.nextLine().trim());
			}
			catch(NumberFormatException nfE){
				System.out.println("Invalid input, try again.");
				continue;
			}

			switch(in){
				case 1:
					//String query = scan.nextLine().trim();
					String query = String.format("from(bucket: \"%s\") |> range(start: -1h)", db.getBucket());
					System.out.println(query);
					List<FluxTable> result = DatabaseAccess.executeQuery(query);

					System.out.println(result.size() + " entries found:");
					for(FluxTable ft : result){
						System.out.println("----------------------------------------");
						System.out.println("Group Key:");
						ft.getGroupKey().forEach(System.out::println);
						System.out.println("----------------------------------------");
						System.out.println("Columns:");
						ft.getColumns().forEach(System.out::println);
						System.out.println("----------------------------------------");
						System.out.println("Records:");
						ft.getRecords().forEach(System.out::println);
						System.out.println("----------------------------------------");
					}

					break;

				case 2:
					System.out.println("Measurement: black_ice");
					String measurement = "black_ice";

					System.out.print("Temperature : ");
					Float temperature = Float.parseFloat(scan.nextLine().replace(",", "."));
					System.out.print("Humidity    : ");
					Float humidity = Float.parseFloat(scan.nextLine().replace(",", "."));
					System.out.print("Air Pressure: ");
					Float air_pressure = Float.parseFloat(scan.nextLine().replace(",", "."));
					System.out.print("Light Level : ");
					Float light_level = Float.parseFloat(scan.nextLine().replace(",", "."));

					Point point = Point
							.measurement(measurement)
							.addField("temperature", temperature)
							.addField("humidity", humidity)
							.addField("air_pressure", air_pressure)
							.addField("light_level", light_level)
							.time(Instant.now(), WritePrecision.NS);

					DatabaseAccess.writeDataPoint(point);
					break;

				case 3:
					mqtt.connect(null);

					// create test message
					System.out.print("Topic: "); // Schwarzi
					String topic = scan.nextLine().trim();
					System.out.print("Message: ");
					String content = scan.nextLine().trim();
					System.out.print("QOS: "); // 0
					int qos;
					try{
						qos = Integer.parseInt(scan.nextLine().trim());
					}
					catch(NumberFormatException nfE){
						System.out.println("Invalid input, try again.");
						continue;
					}

					MqttMessage message = new MqttMessage(content.getBytes());
					message.setQos(qos);

					// send message
					mqtt.sendMessage(topic, message);

					// disconnect client
					mqtt.disconnect();
					break;

				case 9:
					break LOOP;

				default:
					System.out.println("Invalid input, try again.");
					break;
			}
		}

		db.close();
	}*/

}
