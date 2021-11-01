package at.kaindorf.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Handles general IO tasks.
 * 
 * @author Nico Baumann
 */
public class FileAccess{
	
	/**
	 * Loads information about the database, such as login data.
	 * @param filepath The relative path of the properties file
	 * @return The Properties object
	 * @throws IOException If the file could not be loaded
	 */
	public static Properties loadDbProperties(String filepath) throws IOException{
		// generate properties object
		Properties prop = new Properties();
		
		// get input stream of file
		InputStream is = FileAccess.class.getClassLoader().getResourceAsStream(filepath);
		
		// check whether the file was found
		if(is != null)
			// if yes, load it into the Properties object
			prop.load(is);
		else
			// if not, throw exception
			throw new FileNotFoundException("Property file '" + filepath + "' was not found.");
		
		return prop;
	}
	
}
