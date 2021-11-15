package at.kaindorf;

/**
 * If the debugging window is disabled, we'll need a Thread to keep the console
 * running. I decided to export this part into its own class to that, if the
 * debug window is enabled, there aren't 2 separate threads running and conflicting.
 * 
 * @author Nico Baumann
 */
public class MainThread /*extends Thread*/{
	
	////////////////////////////////////////////////////////////////////////////
	//
	//  THIS CLASS IS CURRENTLY DISABLED FOR SIMPLICITY PURPOSES, DO NOT MODIFY!
	//
	////////////////////////////////////////////////////////////////////////////
	/*
	public void discard(){
		
	}
	
	@Override
	public void run(){
		try{
			init();
			loop();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	*/
}
