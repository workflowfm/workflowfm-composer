package com.workflowfm.composer.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.workflowfm.composer.utils.PopupFrame;


public class PopupFrameTest {
	
	public PopupFrameTest() {
		
	}
	
	public void setup()
	{
		System.err.println("Setting up GUI.");
		
		final JPanel hPanel = new JPanel();
		hPanel.add(new JLabel("LOOK MA! I'M HOVERING!"));
		hPanel.add(new JButton("PRESS ME IF YOU CAN!"));
		hPanel.setPreferredSize(new Dimension(200, 100));
		
		final PopupFrame hFrame = new PopupFrame() {
			@Override
			public void init() {
				this.getWindow().getContentPane().add(hPanel);
			}
		};
		

		final JFrame frame = new JFrame();
		
		JPanel panel = new JPanel(new BorderLayout());
		
		panel.add(new JTextArea(),BorderLayout.CENTER);
		
		JButton button = new JButton("HOVER ABOVE ME!");
		button.addMouseListener(hFrame);
		button.addMouseMotionListener(hFrame);
		
		panel.add(button,BorderLayout.SOUTH);
		panel.setPreferredSize(new Dimension(800, 600));
		
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	private static void setLookAndFeel()
	{
		// Enable antialiasing for Swing
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
	}

	public static void main(String args[])
	{
		setLookAndFeel();
		PopupFrameTest gui = new PopupFrameTest();
		gui.setup();
	}
}
