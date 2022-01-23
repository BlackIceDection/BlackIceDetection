package at.kaindorf.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Custom panel for drawing {@code BufferedImage} to the debugging GUI.
 * Only used for debugging purposes, will go unused in final product.
 * 
 * @author Nico Baumann
 */
public class ImagePanel extends JPanel{
	
	private BufferedImage image; // The image to draw.
	
	public ImagePanel(BufferedImage image){
		this.image = image;
	}
	
	/**
	 * Main paint method.
	 * @param g Graphics object, handled by Java.
	 */
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		
		// If custom image is present, draw it onto the panel.
		if(image != null){
			g.drawImage(image, 0, 0, this);
		}
		// If not, draw an alternate text onto the panel.
		else{
			g.drawString("No image found.", 24, 24);
		}
	}
	
	public void setImage(BufferedImage image){
		this.image = image;
		
		this.repaint();
	}
	
}
