package at.kaindorf;

import at.kaindorf.db.Database;
import at.kaindorf.db.DatabaseAccess;
import at.kaindorf.mqtt.MqttExample;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main{
	
	public static void main(String[] args){
		MqttExample mqtt = new MqttExample();
		
		try{
			mqtt.connect(null);
			
			// create test message
			String topic = "Schwarzi";
			String content = ":D";
			int qos = 0;
			
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(qos);
			
			// send message
			mqtt.sendMessage(topic, message);
			
			// disconnect client
			mqtt.disconnect();
		}
		catch(MqttException mqE){
			mqE.printStackTrace();
		}
	}
	
	private static void test_db(){
		Database db = Database.getInstance();
		
		Scanner scan = new Scanner(System.in);
		
		LOOP:
		while(true){
			System.out.println(" (1) Print table data");
			System.out.println(" (2) Add data entry");
			System.out.println("");
			
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
				
				case 9:
					break LOOP;
				
				default:
					System.out.println("Invalid input, try again.");
					break;
			}
		}
		
		db.close();
	}
	
}
