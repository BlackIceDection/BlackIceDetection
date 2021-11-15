package at.kaindorf.ui;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The debugging window class. Will be disabled in release, but speeds up development time.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DebugGUI extends JFrame{
	
	// the main window
	public static JFrame frame = null;
	
	// main layout
	private GridLayout lyMainGrid;
	
	// buttons
	private JButton btConnectInflux, btConnectMqtt;
	
	// labels
	private JLabel lbInflux, lbMqtt;
	
	// lua variables
	private int gridRows, gridCols;
	
	public DebugGUI(){
		// set frame instance
		frame = this;
		
		// set button instances
		btConnectInflux = new JButton();
		btConnectMqtt = new JButton();
		
		// set label instances
		lbInflux = new JLabel();
		lbMqtt = new JLabel();
		
		// layout will be handles in init()
	}
	
	public void init(){
		// window initialization is handled by Lua
		
		//      configure buttons
		// influx
		btConnectInflux.setText("Connect to InfluxDB");
		
		// mqtt
		btConnectMqtt.setText("Initialize MQTT");
		
		
		
		//      configure layout
		// create grid layout
		lyMainGrid = new GridLayout(gridRows, gridCols);
		
		// add buttons
		lyMainGrid.addLayoutComponent("btConnectInflux", btConnectInflux);
		lyMainGrid.addLayoutComponent("btConnectMqtt", btConnectMqtt);
		
		
		
		
		// add all events
		setEvents();
		
		// apply layout to frame
		frame.setLayout(lyMainGrid);
		
		//      finalizing
		// when everything's done, show the window
		frame.setVisible(true);
	}
	
	private void setEvents(){
		// influx
		btConnectInflux.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				System.out.println("pepe :3");
			}
		});
		
		// mqtt
		btConnectMqtt.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				System.out.println("lmao");
			}
		});
	}
	
}
