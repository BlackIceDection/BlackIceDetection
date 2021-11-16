package at.kaindorf.mqtt;

import at.kaindorf.Main;
import at.kaindorf.lua.LuaJ;
import at.kaindorf.ui.DebugGUI;
import com.influxdb.client.domain.Run;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.swing.*;
import java.util.HashMap;

@Data
public class Mqtt implements MqttCallback{
	
	// instance
	private static Mqtt instance = null;
	
	// check for soft reload
	public static boolean firstRun = true;
	
	// client variables
	private String clientId, clientBroker;
	// the client itself
	private MqttClient client = null;
	
	//
	private MemoryPersistence memPers;
	
	// connection properties
	private MqttConnectOptions connOpts;
	
	// quality of service of messages
	private int qos;
	
	// general topic
	private String topic;
	
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates and returns an instance of this class.
	 * @return This instance.
	 */
	public static Mqtt getInstance(){
		if(instance == null) instance = new Mqtt();
		return instance;
	}
	
	public Mqtt(){
		//
		memPers = new MemoryPersistence();
		
		// initialize connection options
		connOpts = new MqttConnectOptions();
		// set default connection options
		connOpts.setCleanSession(true);
	}
	
	/**
	 * Set up MQTT connection.
	 */
	public void connect() throws MqttException, RuntimeException{
		// initialize client if it isn't already
		if(client == null){
			client = new MqttClient(this.clientBroker, this.clientId, memPers);
		}
		
		// check if client exists
		if(client != null){
			// set listener for client
			client.setCallback(this);
			
			// connect to client with default options
			client.connect(connOpts);
		}
		else{
			throw new RuntimeException("MQTT client doesn't exist");
		}
		
		// subscribe to message receiver
		client.subscribe(topic, qos);
	}
	/**
	 * Setup MQTT connection, this time with special settings.
	 * @param opts Connection options.
	 */
	public void connect(MqttConnectOptions opts) throws Exception{
		// initialize client if it isn't already
		if(client == null){
			client = new MqttClient(this.clientBroker, this.clientId, memPers);
		}
		
		// check if client exists
		if(client != null){
			// set listener for client
			client.setCallback(this);
			
			// connect to client with special options
			client.connect(opts);
		}
		else{
			throw new RuntimeException("MQTT client doesn't exist");
		}
		
		// subscribe to message receiver
		client.subscribe(topic, qos);
	}
	
	/**
	 * Closes MQTT connection.
	 */
	public void disconnect() throws MqttException, RuntimeException{
		if(client != null){
			client.disconnect();
			client.close();
		}
		else{
			throw new RuntimeException("MQTT client doesn't exist");
		}
	}
	
	/**
	 * Reloads lua scripts.
	 */
	public void reload(){
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
		client = null;
		memPers = null;
		connOpts = null;
		
		clientId = clientBroker = null;
	}
	
	public String getConnectionString(boolean html){
		if(client != null){
			if(!html)
				return String.format(
						"Client ID: %s\nBroker URL:%s",
						clientId, clientBroker
				);
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
	
	public void messageArrived(String topic, MqttMessage message) throws MqttException{
		DebugGUI.setMessageReceivedText(new String(message.getPayload()));
		
		System.out.format("---------------------------------\nMESSAGE ARRIVED:\n" +
				"Topic: %s\nMessage: %s\n---------------------------------\n",
				topic, new String(message.getPayload()));
	}
	
	public void sendMessage(String text){
		try{
			MqttMessage message = new MqttMessage(text.getBytes());
			message.setQos(qos);
			
			client.publish(topic, message);
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
	
	public void deliveryComplete(IMqttDeliveryToken token){}
	
	public void connectionLost(Throwable cause) {
		System.out.println("Connection lost because: " + cause);
		Main.instance.mqttFailed();
	}
	
}
