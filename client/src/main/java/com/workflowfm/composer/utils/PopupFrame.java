package com.workflowfm.composer.utils;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JWindow;
import javax.swing.Timer;

public abstract class PopupFrame extends MouseAdapter  {
	
	private final int showDelay = 400;
	private final int hideDelay = 200;
	private Timer showTimer;
	private Timer hideTimer;
	
	private JWindow window;
	private MouseListener popupMouseListener;
	protected int x;
	protected int y;
	
	protected boolean entered = false;
	
	public PopupFrame() {		
		this.showTimer = new Timer(showDelay,new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (window == null || !window.isVisible())
					PopupFrame.this.show();
			}
		});
		this.showTimer.setRepeats(false);
		this.showTimer.setCoalesce(true);
		
		this.hideTimer = new Timer(hideDelay,new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (window != null && window.isVisible())
					window.dispose();
			}
		});
		this.hideTimer.setRepeats(false);
		this.hideTimer.setCoalesce(true);
	}
	
	public void start() {
		showTimer.restart();
	}
	
	public void cancel() {
		showTimer.stop();
	}
	
	public JWindow getWindow() {
		return this.window;
	}
	
	public MouseListener getPopupMouseListener() {
		return this.popupMouseListener;
	}

	public void show() {
		window = new JWindow();
		
		popupMouseListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				hideTimer.stop();
			}
			@Override
			public void mouseExited(MouseEvent e) {
		        if (!mouseInPopup(e))
		        	hideTimer.restart();
			}
		};
		
		window.addMouseListener(popupMouseListener);
		
		//frame.setUndecorated(true);
		//frame.setResizable(false);
		window.setAlwaysOnTop(true);
		//frame.setOpacity(0.6f);
		//frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		init();
		
		window.pack();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		int xPos = x + 10;
		if (xPos + window.getWidth() > screenSize.getWidth()) 
			xPos = x - window.getWidth() - 10;
		
		int yPos = y + 10;
		if (yPos + window.getHeight() > screenSize.getHeight()) 
			yPos = y - window.getHeight() - 10;
				
		window.setLocation(xPos, yPos);
		window.setVisible(true);
	}

	public void mouseInHighlightArea(MouseEvent e) {
		mouseInHighlightArea(e.getXOnScreen(), e.getYOnScreen());
	}
	
	public void mouseInHighlightArea(int x, int y) {
		entered = true;
		this.x = x;
		this.y = y;
		showTimer.restart();
	}
	
	public void mouseOutsideHighlightArea() {
		entered = false;
		showTimer.stop();
		if (popupVisible()) {
			hideTimer.restart();
		}
	}
	
	public boolean popupVisible() {
		return (window != null && window.isVisible());
	}
	
	public boolean mouseInPopup(MouseEvent e) {
		return (popupVisible() && window.getContentPane().getBounds().contains(e.getPoint()));
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		if (entered) {
			mouseInHighlightArea(e);
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		mouseInHighlightArea(e);
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		mouseOutsideHighlightArea();
	}
	
	public abstract void init();
}
