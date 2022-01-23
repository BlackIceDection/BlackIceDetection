package at.kaindorf.db;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Converts between image formats (Java classes).
 * Only used for debugging purposes.
 * 
 * @author Nico Baumann
 */
public class ImageHandler{
	
	/**
	 * Converts a Java {@code Image} to Java {@code BufferedImage}.
	 * @param img The Java {@code Image}.
	 * @return The Java {@code BufferedImage}.
	 */
	public static BufferedImage getBufferedImage(Image img){
		// If the passed Java Image IS already a BufferedImage then just typecast and return it.
		if(img instanceof BufferedImage) return (BufferedImage) img;
		else{
			// Create a new BufferedImage with default params.
			BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			// Create graphics object for the BufferedImage.
			Graphics2D g = bi.createGraphics();
			// Draw the regular Image onto the BufferedImage.
			g.drawImage(img, 0, 0, null);
			// Dispose the graphics object.
			g.dispose();
			
			// Return the finished BufferedImage.
			return bi;
		}
	}
	
}
