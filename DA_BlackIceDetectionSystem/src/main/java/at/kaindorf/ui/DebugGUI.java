package at.kaindorf.ui;

import at.kaindorf.Main;
import at.kaindorf.db.Base64Handler;
import at.kaindorf.db.Database;
import at.kaindorf.db.ImageHandler;
import at.kaindorf.mqtt.Mqtt;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.paho.client.mqttv3.MqttException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The debugging window. Only used for, well, debugging. Disabled in the final product.
 *
 * @author Nico Baumann
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DebugGUI extends JFrame{

	// the main window
	public static JFrame frame = null;

	// layouts
	private GridLayout lyMainGrid, lyParams, lyMqtt;

	// panels
	private JPanel paMqtt, paParams;
	private static ImagePanel paCamera;

	// buttons
	private JButton btConnectInflux, btConnectMqtt;
	// button states
	private boolean conInflux = false, conMqtt = false;

	// labels
	private JLabel lbInflux, lbMqtt;
	private static JLabel lbOutput;

	// color indicators for buttons
	private final Color COL_DISCONNECTED = new Color(0xFFAAAA);
	private final Color COL_CONNECTED = new Color(0xAAFFAA);

	// get influx and MQTT instances
	private static Database db;
	private static Mqtt mqtt;

	// temp vars
	public static float temp = 0, humid = 0, light = 0, air = 0;

	public DebugGUI(){
		// set frame instance
		frame = this;

		// set panel instances
		paMqtt = new JPanel();
		paParams = new JPanel();
		paCamera = new ImagePanel(null);

		// set button instances
		btConnectInflux = new JButton();
		btConnectMqtt = new JButton();

		// set label instances
		lbInflux = new JLabel();
		lbMqtt = new JLabel();
		lbInflux = new JLabel();
		lbMqtt = new JLabel();
		lbOutput = new JLabel();

		// get database/MQTT instances
		db = Database.getInstance();
		mqtt = Mqtt.getInstance();

		// layout will be handled in init()
	}

	public void init(){
		// window initialization is handled by Lua

		//      configure buttons
		// influx
		changeInfluxButtonVisuals(false);

		// mqtt
		changeMqttButtonVisuals(false);

		lbOutput.setText("Here goes the output");

		//      configure layout
		// create grid layout
		lyMainGrid = new GridLayout(1, 2);
		lyParams = new GridLayout(4, 1);
		lyMqtt = new GridLayout(1, 2);

		// apply layouts
		frame.setLayout(lyMainGrid);
		paParams.setLayout(lyParams);
		paMqtt.setLayout(lyMqtt);

		// add components
		frame.add(paParams);
		frame.add(paCamera);

		paParams.add(btConnectInflux);
		paParams.add(lbInflux);
		paParams.add(btConnectMqtt);
		paParams.add(paMqtt);

		paMqtt.add(lbMqtt);
		paMqtt.add(lbOutput);


		// add all events
		setEvents();

		//      finalizing
		// when everything's done, show the window
		frame.setVisible(true);
	}

	private void changeInfluxButtonVisuals(boolean connected){
		conInflux = connected;

		if(!connected){
			btConnectInflux.setText("Establish InfluxDB Connection");
			btConnectInflux.setBackground(COL_DISCONNECTED);
		}
		else{
			btConnectInflux.setText("Disconnect from InfluxDB");
			btConnectInflux.setBackground(COL_CONNECTED);
		}

		lbInflux.setText(db.getConnectionString(true));
	}

	private void changeMqttButtonVisuals(boolean connected){
		conMqtt = connected;

		if(!connected){
			btConnectMqtt.setText("Initialize MQTT Client");
			btConnectMqtt.setBackground(COL_DISCONNECTED);
		}
		else{
			btConnectMqtt.setText("Close MQTT Connection");
			btConnectMqtt.setBackground(COL_CONNECTED);
		}

		lbMqtt.setText(mqtt.getConnectionString(true));

		/*if(connected){
			setMessageReceivedText("Current off-chip sensor temperature = 29 Celsius\n" +
					"Current onboard sensor brightness = 151 Lux\n" +
					"Current onboard sensor temperature = 30 Celsius\n" +
					"Current onboard sensor humidity = 27 %\n" +
					"Current barometer temperature = 33 Celsius\n" +
					"Current barometer pressure = 99421 pascal\n" +
					"Live body detected within 5 seconds!");
		}*/
	}

	public void setMqttState(boolean state){
		changeMqttButtonVisuals(state);
	}

	private void dbTestQuery(){
		String fluxQuery = String.format(
				"from(bucket: \"%s\")\n" +
				"  |> range(start: '%s', stop: '%s')\n" +
				"  |> filter(fn: (r) => r[\"_measurement\"] == \"%s\")\n" +
				"  |> filter(fn: (r) => r[\"_field\"] == \"temperature\" or r[\"_field\"] == \"humidity\" or r[\"_field\"] == \"light_level\" or r[\"_field\"] == \"air_pressure\")\n" +
				//"  |> aggregateWindow(every: v.windowPeriod, fn: mean, createEmpty: false)\n" +
				"  |> yield(name: \"mean\")",
				db.getBucket(),
				"2021-11-18T08:40:00Z",
				"2021-11-18T09:40:00Z",
				db.getMeasurement()
		);

		QueryApi qApi = db.getClient().getQueryApi();

		List<FluxTable> tables = qApi.query(fluxQuery);

		System.out.println("=================== QUERY RESULTS ===================");
		for(FluxTable table : tables){
			List<FluxRecord> records = table.getRecords();

			for(FluxRecord record : records){
				System.out.println(record.getTime() + ": " + record.getValueByKey("temperature"));
			}
		}
		System.out.println("=====================================================");
	}

	private void setEvents(){
		// influx
		btConnectInflux.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				// if Influx isn't connected yet
				if(!conInflux){
					System.out.println("connecting to influx...");

					try{
						// connect to Influx
						db.connect();

						// change button visuals
						changeInfluxButtonVisuals(true);

						System.out.println("success!");

						//dbTestQuery();
					}
					catch(Exception e){
						String message = "Failed to establish database connection.";
						if(Main.debugWindow){
							JOptionPane.showMessageDialog(
									null,
									message,
									"Influx Connection Error",
									JOptionPane.ERROR_MESSAGE
							);
						}
						System.out.println("ERROR: " + message);
						System.out.println(e.toString());
						//e.printStackTrace();

						changeInfluxButtonVisuals(false);
					}
				}
				// if influx is already connected
				else{
					System.out.println("disconnecting from influx...");

					try{
						// disconnect from Influx
						db.disconnect();
						db.reset();
						db.reload();
					}
					catch(Exception e){
						String message = "Failed to disconnect from database.";
						if(Main.debugWindow){
							JOptionPane.showMessageDialog(
									null,
									message,
									"Influx Connection Error",
									JOptionPane.ERROR_MESSAGE
							);
						}
						System.out.println("ERROR: " + message);
						System.out.println(e.toString());
					}

					// change button visuals
					changeInfluxButtonVisuals(false);
				}
			}
		});

		// mqtt
		btConnectMqtt.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				// if MQTT client doesn't exist yet
				if(!conMqtt){
					System.out.println("creating MQTT client...");

					try{
						// create client
						mqtt.connect(null);

						// change button visuals
						changeMqttButtonVisuals(true);

						System.out.println("success!");
					}
					catch(MqttException mqE){
						String message = "Failed to create MQTT client.";
						if(Main.debugWindow){
							JOptionPane.showMessageDialog(
									null,
									message,
									"MQTT Client Error",
									JOptionPane.ERROR_MESSAGE
							);
						}
						System.out.println("ERROR: " + message);
						System.out.println(mqE.toString());
						mqE.printStackTrace();
						changeMqttButtonVisuals(false);
					}
					catch(RuntimeException runE){
						String message = "Client creation was successful, but its instance is null.";
						if(Main.debugWindow){
							JOptionPane.showMessageDialog(
									null,
									message,
									"MQTT Client Error",
									JOptionPane.ERROR_MESSAGE
							);
						}
						System.out.println("ERROR: " + message);
						System.out.println(runE.toString());
						runE.printStackTrace();
						changeMqttButtonVisuals(false);
					}
				}
				// if MQTT client already exists
				else{
					System.out.println("disposing MQTT client...");

					try{
						// dispose client
						mqtt.disconnect();
						mqtt.reset();
						mqtt.reload();
					}
					catch(MqttException mqE){
						String message = "Failed to dispose of MQTT client.";
						if(Main.debugWindow){
							JOptionPane.showMessageDialog(
									null,
									message,
									"MQTT Error",
									JOptionPane.ERROR_MESSAGE
							);
						}
						System.out.println("ERROR: " + message);
						System.out.println(mqE.toString());
					}
					catch(RuntimeException runE){
						String message = "Failed to dispose MQTT client, it doesn't exist.";
						if(Main.debugWindow){
							JOptionPane.showMessageDialog(
									null,
									message,
									"MQTT Error",
									JOptionPane.ERROR_MESSAGE
							);
						}
						System.out.println("WARNING: " + message);
						System.out.println(runE.toString());
					}

					// change button visuals
					changeMqttButtonVisuals(false);
				}
			}
		});
	}

	/**
	 * Sets the received MQTT message (with database related values) to label.
	 * @param text The text to display
	 */
	public static void setMessageReceivedText(String text){
		String[] lines = text.toLowerCase().split("\n");

		for(String line : lines){
			if(line.contains("temperature")){
				if(line.contains("off-chip")){
					temp = (float)locateNumber(line);
				}
				else{
					continue;
				}
			}

			if(line.contains("brightness"))
				light = (float)locateNumber(line);

			if(line.contains("humidity"))
				humid = (float)locateNumber(line);

			if(line.contains("pressure"))
				air = (float)locateNumber(line);
		}

		lbOutput.setText(String.format(
				"<html>" +
				"Timestamp: %s <br/>" +
				"<br/>" +
				"Temperature: %.1f °C <br/>" +
				"Humidity: %.1f Percent <br/>" +
				"Light Level: %.1f Lux <br/>" +
				"Air Pressure: %.1f Pa" +
				"</html>",
				LocalDateTime.now(),
				temp, humid, light, air
		));

		if(temp < 4)
			System.out.println("WARNING: Temperature below 4°C, black ice might be present!");

		//addToDb(temp, humid, air, light);
	}

	/**
	 * Displays the output of the camera to a dedicated label.
	 * @param pngBase64 The camera image as Base64 string
	 */
	public static void setCameraOutput(String pngBase64){
		byte[] pngData = Base64Handler.decodeToByteArr(pngBase64);

		InputStream is = new ByteArrayInputStream(pngData);
		BufferedImage image = null;

		try{
			image = ImageIO.read(is);
			is.close();
		}
		catch(IOException ioE){
			throw new RuntimeException("Failed to read received image.");
		}

		paCamera.setImage(ImageHandler.getBufferedImage(
				image.getScaledInstance(800, 600, Image.SCALE_SMOOTH)
		));

		//addToDb(pngBase64);
	}

	private static void addToDb(float temp, float humid, float air, float light){
		Point p = Point
				.measurement("black_ice")
				.addField("temperature", temp)
				.addField("humidity", humid)
				.addField("air_pressure", air)
				.addField("light_level", light)
				.time(Instant.now(), WritePrecision.NS);

		db.writeDataPoint(p);
	}
	private static void addToDb(String image64){
		/*Point p = Point
				.measurement("black_ice")
				.addField("temperature", temp)
				.addField("humidity", humid)
				.addField("air_pressure", air)
				.addField("light_level", light)
				.time(Instant.now(), WritePrecision.NS);

		db.writeDataPoint(p);*/
	}

	private static int locateNumber(String s){
		String[] parts = s.split(" ");

		for(int i = 0; i < parts.length; i++){
			try{
				int num = Integer.parseInt(parts[i]);
				return num;
			}
			catch(NumberFormatException nfE){
				continue;
			}
		}

		return -1;
	}

}
