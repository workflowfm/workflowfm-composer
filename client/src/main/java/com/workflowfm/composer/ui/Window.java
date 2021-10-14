package com.workflowfm.composer.ui;

import javax.swing.Icon;
import javax.swing.JPanel;

public interface Window {
	public JPanel getPanel();
	public String getName();
	public Icon getIcon();
	
	public WindowManager getManager();
	public void show();
	public void dispose();
}
