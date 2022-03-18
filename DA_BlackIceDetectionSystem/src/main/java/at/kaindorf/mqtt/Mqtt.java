package at.kaindorf.mqtt;

import at.kaindorf.Main;
import at.kaindorf.lua.LuaJ;
import at.kaindorf.ui.DebugGUI;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.swing.*;
import java.util.HashMap;

@Data
/**
 * Handles all communications via MQTT.
 *
 * @author Nico Baumann
 */
public class Mqtt implements MqttCallback{

	private static Mqtt instance = null; // Instance of this class.
	public static boolean firstRun = true; // Soft reload, was used for Lua.

	public boolean isConnected = false;

	private String clientId; // ID of the MQTT client.
	private String clientBroker; // Broker of the MQTT client.
	private MqttClient client = null; // The MQTT client itself.

	private MemoryPersistence memPers; // Memory persistence of MQTT. NOT to be confused with 'MemBlackIce' for Influx.

	private MqttConnectOptions connOpts; // Properties of MQTT connection.
	private int qos; // The quality of service to use for MQTT messages.

	private String topic1, topic2; // MQTT topics.


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Creates and returns an instance of this class.
	 * @return This instance.
	 */
	public static Mqtt getInstance(){
		if(instance == null) instance = new Mqtt(); // If instance hasn't been created yet, create a new one.
		return instance;
	}

	/**
	 * Initializes relevant variables for MQTT connection.
	 */
	public Mqtt(){
		memPers = new MemoryPersistence(); // Create new memory persistence for MQTT.
		connOpts = new MqttConnectOptions(); // Initialize connection options.

		// Add default parameters for MQTT connections.
		connOpts.setCleanSession(true); //
	}

	/**
	 * Set up MQTT connection.
	 */
	public void connect(MqttConnectOptions opts) throws MqttException{
		// Initialize client if it isn't already.
		if(client == null)
			client = new MqttClient(this.clientBroker, this.clientId, memPers);

		client.setCallback(this); // Set Listener for MQTT client.
		// If special connection options were passed, use those instead of the defaults.
		if(opts != null)
			client.connect(connOpts); // Connect client with options set by the constructor.
		// If not, just use the one set in the constructor.
		else
			client.connect(connOpts); // Connect client with options set by the constructor.

		// Subscribe to message listener for every topic.
		client.subscribe(topic1, qos);
		client.subscribe(topic2, qos);

		isConnected = true;
	}

	/**
	 * Closes MQTT connection.
	 */
	public void disconnect() throws MqttException, RuntimeException{
		// Check whether MQTT client even exists.
		if(client != null){
			client.disconnect(); // Disconnect client from MQTT.
			client.close(); // Close and reset the client.

			isConnected = false;
		}
		// If not, throw an exception.
		else
			throw new RuntimeException("MQTT client doesn't exist");
	}

	/**
	 * Reloads MQTT relevant Lua scripts.
	 */
	public void reload(){
		// Pass instance of this class to 'mqtt.lua' and execute it.
		LuaJ.executeLuaScript("mqtt.lua", new HashMap<String, Object>(){
			{
				put("mqtt", instance);
			}
		});
	}

	/**
	 * Resets all variables.
	 */
	public void reset(){
		// Reset client if it exists.
		if(client != null){
			try{
				disconnect(); // Try to disconnect client.
			}
			catch(Exception e){
				// Client wasn't even connected. Print a warning message to the console, just to be safe.
				System.out.println("WARNING: Attempted to close non-existent MQTT connection.");
			}
			finally{
				// Delete MQTT client.
				client = null;
			}
		}
		memPers = null; // Delete memory persistence.
		connOpts = null; // Delete default connection options.

		clientId = clientBroker = null; // Reset MQTT client relevant variables.

		isConnected = false;
	}

	/**
	 * Returns string containing information about the MQTT connection and client.
	 * Only used for debugging purposes.
	 * @param html Whether the string should be HTML formatted (for JLabel).
	 * @return The information string.
	 */
	public String getConnectionString(boolean html){
		// Check whether MQTT client exists.
		if(client != null){
			// Return a plain string.
			if(!html)
				return String.format(
						"Client ID: %s\nBroker URL:%s",
						clientId, clientBroker
				);
			// Return HTML formatted string.
			else
				return String.format(
						"<html>Client ID: %s<br/><br/>Broker URL:%s</html>",
						clientId, clientBroker
				);
		}

		return "Not connected.";
	}



	////////////////////////////////////////////////////////////////////////////
	// COMMUNICATION
	////////////////////////////////////////////////////////////////////////////

	// TODO: Find out why Java throws EndOfFileException when unexpected MQTT interrupt occurs.

	/**
	 * The message received event for MQTT messages.
	 * @param topic The topic the message was received on.
	 * @param message The message string itself.
	 * @throws MqttException Used by earlier build, currently unused.
	 * @throws RuntimeException If a non-MQTT related error occurs.
	 */
	public void messageArrived(String topic, MqttMessage message) throws MqttException, RuntimeException{
		// Check the topic from where the message came from.
		switch(topic){
			// Topic containing the different measurements of the sensors (temperature, humidity etc.).
			case "temperature":
				// Debugging GUI
				if(Main.debugWindow) DebugGUI.setMessageReceivedText(new String(message.getPayload()));

				// Print info message to console.
				System.out.format("---------------------------------\nMESSAGE ARRIVED:\n" +
								"Topic: %s\nMessage: %s\n---------------------------------\n",
						topic, new String(message.getPayload()));

				// Lua script that alerts user if black ice formation is possible
				/*
				LuaJ.executeLuaScript("data_science.lua", new HashMap<String, Object>(){
					{
						put("var_handler", Main.debugFrame);
					}
				});
				*/
				break;

			// Topic containing the image of the camera, formatted as Base64 string.
			case "thermal":
				// Print info message to console.
				System.out.format("---------------------------------\nPICTURE ARRIVED:\n" +
								"Topic: %s\n---------------------------------\n",
						topic);

				// The PNG image as Base64 string
				String pngBase64 = new String(message.getPayload());

				// Show image on the debugging GUI
				if(Main.debugWindow)
					DebugGUI.setCameraOutput(pngBase64);

				break;
		}
	}

	/**
	 * Sends an MQTT message to the other end. Since the final product never actually send MQTT messages, this method
	 * goes unused.
	 * @param text The message to send.
	 */
	public void sendMessage(String text){
		try{
			MqttMessage message = new MqttMessage(text.getBytes());
			message.setQos(qos);

			client.publish(topic1, message);
		}
		catch(MqttException mE){
			JOptionPane.showMessageDialog(
					null,
					"Failed to send MQTT message.",
					"Message Delivery Error",
					JOptionPane.ERROR_MESSAGE
			);

			System.out.println("ERROR: Failed to send MQTT message.");
			mE.printStackTrace();
		}
	}
	/**
	 * Honestly, I forgot what this is supposed to do lol.
	 * I think this checked whether a MQTT message sent from the server was actually received by the other end,
	 * but since the server in the final product never actually sends MQTT messages, this goes unused/irrelevant.
	 * Not that there's any code left to begin with, but I'm leaving this in anyways because why not.
	 * @param token ?
	 */
	public void deliveryComplete(IMqttDeliveryToken token){}

	/**
	 * Gets called by MQTT itself when the connection was interrupted.
	 * @param cause The reason for the disconnect.
	 */
	public void connectionLost(Throwable cause) {
		System.out.println("Connection lost because: " + cause);
		Main.instance.mqttFailed();
	}

}
