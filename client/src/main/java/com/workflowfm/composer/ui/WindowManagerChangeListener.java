package com.workflowfm.composer.ui;

public interface WindowManagerChangeListener {
	public void windowAdded(Window window);
	public void windowActivated(Window window);
	public void windowRemoved(Window window);
}
