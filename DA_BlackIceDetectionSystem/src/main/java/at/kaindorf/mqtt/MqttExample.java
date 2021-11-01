package at.kaindorf.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttExample{
	
	// client variables
	private String clientId, clientBroker;
	// the client itself
	private MqttClient client = null;
	
	private MemoryPersistence memPers;
	
	// connection properties
	private MqttConnectOptions connOpts;
	
	public MqttExample(){
		// setter
		this.clientId = "Nico";
		this.clientBroker = "tcp://192.168.0.101:1883";
		
		// 
		memPers = new MemoryPersistence();
		
		// initialize connection options
		connOpts = new MqttConnectOptions();
		// set connection properties
		setConnectionOptions(connOpts);
		
		// initialize client
		try{
			createClient();
		}
		catch(MqttException mqE){
			System.out.println("ERROR: Failed to create MQTT Client");
			mqE.printStackTrace();
		}
	}
	
	public void sendMessage(String topic, MqttMessage message) throws MqttException{
		client.publish(topic, message);
	}
	
	public void connect(MqttConnectOptions opts) throws MqttException{
		if(client != null){
			if(opts != null) client.connect(opts);
			else client.connect(connOpts);
		}
		else
			System.out.println("ERROR: Unable to establish connection, client doesn't exist.");
	}
	
	public void disconnect() throws MqttException{
		if(client != null){
			client.disconnect();
			client.close();
		}
		else
			System.out.println("ERROR: Unable to disconnect, client doesn't exist.");
	}
	
	private void createClient() throws MqttException{
		if(client == null)
			client = new MqttClient(this.clientBroker, this.clientId, memPers);
	}
	
	private void setConnectionOptions(MqttConnectOptions opts){
		// ?
		opts.setCleanSession(true);
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////
	// GETTER & SETTER
	////////////////////////////////////////////////////////////////////////////
	
	public boolean isClientCreated(){
		return client != null;
	}
	
}
