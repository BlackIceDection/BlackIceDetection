package at.kaindorf.beans;

public abstract class ConnectionClass{
	
	// establish connection
	public abstract void connect();
	// close connection
	public abstract void disconnect();
	
	// execute lua scripts and initialize necessary instances
	public abstract void reload();
	// close all connections and reset variables
	public abstract void reset();
	
}
