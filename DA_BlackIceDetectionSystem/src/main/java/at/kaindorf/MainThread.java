package at.kaindorf;

import java.util.Scanner;

/**
 * If the debugging window is disabled, we'll need a Thread to keep the console
 * running. I decided to export this part into its own class to that, if the
 * debug window is enabled, there aren't 2 separate threads running and conflicting.
 * 
 * @author Nico Baumann
 */
public class MainThread implements Runnable{
	
	private final Scanner SCANNER = new Scanner(System.in);
	
	@Override
	public void run(){
		try{
			do{
				String s = SCANNER.nextLine();
				
				switch(s.toLowerCase().trim())
				{
					case "exit":
						System.exit(0);
						break;
						
					default:
						System.out.println("Unknown Command.");
						break;
						
					// lua not working right, lua command temporarily removed.
				}
			} while(true); // endless loop; good enough for now
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
