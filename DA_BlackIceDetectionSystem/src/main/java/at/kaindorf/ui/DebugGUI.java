package at.kaindorf.ui;

import at.kaindorf.Main;
import at.kaindorf.db.Database;
import at.kaindorf.mqtt.Mqtt;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The debugging window class. Will be disabled in release, but speeds up development time.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DebugGUI extends JFrame{
	
	// the main window
	public static JFrame frame = null;
	
	// layouts
	private GridLayout lyMainGrid, lyMqtt;
	
	// panels
	private JPanel paMqtt;
	
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
	private Database db;
	private Mqtt mqtt;
	
	public DebugGUI(){
		// set frame instance
		frame = this;
		
		// set panel instances
		paMqtt = new JPanel();
		
		// set button instances
		btConnectInflux = new JButton();
		btConnectMqtt = new JButton();
		// set label instances
		lbInflux = new JLabel();
		lbMqtt = new JLabel();
		
		// set label instances
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
		lyMainGrid = new GridLayout(4, 1);
		lyMqtt = new GridLayout(1, 2);
		
		// apply layouts
		frame.setLayout(lyMainGrid);
		paMqtt.setLayout(lyMqtt);
		
		// add components
		frame.add(btConnectInflux);
		frame.add(lbInflux);
		frame.add(btConnectMqtt);
		frame.add(paMqtt);
		
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
	}
	
	public void setMqttState(boolean state){
		changeMqttButtonVisuals(state);
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
						mqtt.connect();
						
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
	
	public static void setMessageReceivedText(String text){
		lbOutput.setText("<html>" + text.replace("\n", "</br>") + "</html>");
	}
	
}
