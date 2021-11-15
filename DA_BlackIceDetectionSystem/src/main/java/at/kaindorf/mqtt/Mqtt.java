package at.kaindorf.mqtt;

import at.kaindorf.Main;
import at.kaindorf.beans.ConnectionClass;
import at.kaindorf.lua.LuaJ;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.swing.*;
import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = false)
public class Mqtt extends ConnectionClass{
	
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
	public void connect(){
		// initialize client if it isn't already
		if(client != null){
			try{
				client = new MqttClient(this.clientBroker, this.clientId, memPers);
			}
			catch(MqttException mqE){
				if(Main.debugWindow)
					JOptionPane.showMessageDialog(
							null,
							"Failed to create MQTT Client.",
							"MQTT Client Error",
							JOptionPane.ERROR_MESSAGE
					);
				
				System.out.println("ERROR: Failed to create MQTT Client.");
				mqE.printStackTrace();
			}
		}
		
		try{
			// check if client exists
			if(client != null){
				// connect to client with default options
				client.connect(connOpts);
			}
			else{
				if(Main.debugWindow)
					JOptionPane.showMessageDialog(
							null,
							"Failed to establish MQTT connection: The MQTT client doesn't exist.",
							"MQTT Connection Error",
							JOptionPane.ERROR_MESSAGE
					);
				System.out.println("ERROR: Unable to establish connection, MQTT client doesn't exist.");
			}
		}
		catch(MqttException mE){
			if(Main.debugWindow)
				JOptionPane.showMessageDialog(
						null,
						"Failed to establish MQTT connection.",
						"MQTT Connection Error",
						JOptionPane.ERROR_MESSAGE
				);
			
			System.out.println("ERROR: Failed to establish MQTT exception.");
			mE.printStackTrace();
		}
	}
	/**
	 * Setup MQTT connection, this time with special settings.
	 * @param opts Connection options.
	 */
	public void connect(MqttConnectOptions opts){
		try{
			// check if client exists
			if(client != null){
				// connect to client with special options
				client.connect(opts);
			}
			else{
				if(Main.debugWindow)
					JOptionPane.showMessageDialog(
							null,
							"Failed to establish MQTT connection: The MQTT client doesn't exist.",
							"MQTT Connection Error",
							JOptionPane.ERROR_MESSAGE
					);
				
				System.out.println("ERROR: Unable to establish connection, MQTT client doesn't exist.");
			}
		}
		catch(MqttException mE){
			if(Main.debugWindow)
				JOptionPane.showMessageDialog(
						null,
						"Failed to establish MQTT connection. Check your custom connection options.",
						"MQTT Connection Error",
						JOptionPane.ERROR_MESSAGE
				);
			
			System.out.println("ERROR: Failed to establish MQTT connection. Check the custom connection options.");
			mE.printStackTrace();
		}
	}
	
	/**
	 * Closes MQTT connection.
	 */
	public void disconnect(){
		try{
			if(client != null){
				client.disconnect();
				client.close();
			}
			else{
				JOptionPane.showMessageDialog(
						null,
						"Failed to close MQTT connection: MQTT client doesn't exist.",
						"MQTT Connection Error",
						JOptionPane.ERROR_MESSAGE
				);
				
				System.out.println("ERROR: Unable to disconnect, MQTT client doesn't exist.");
			}
		}
		catch(MqttException mE){
			JOptionPane.showMessageDialog(
					null,
					"Failed to close MQTT connection.",
					"MQTT Connection Error",
					JOptionPane.ERROR_MESSAGE
			);
			
			System.out.println("ERROR: Failed to close connection.");
			mE.printStackTrace();
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
	 * Closes connection and resets all variables.
	 */
	public void reset(){
		disconnect();
		
		client = null;
		memPers = null;
		connOpts = null;
		
		clientId = clientBroker = null;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////
	// COMMUNICATION
	////////////////////////////////////////////////////////////////////////////
	/*
	public void receiveMessage(){
		try{
			// TODO: implement message received event and handle it accordingly.
		}
		catch(MqttException mE){
			JOptionPane.showMessageDialog(
					null,
					"MQTT message listener failed.",
					"Event Error",
					JOptionPane.ERROR_MESSAGE
			);
			
			System.out.println("ERROR: Failed to send MQTT message.");
			mE.printStackTrace();
		}
	}
	*/
	public void sendMessage(String topic, MqttMessage message){
		try{
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
	
}
