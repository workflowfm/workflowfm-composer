package com.workflowfm.composer.ui;

import java.util.Collection;

public interface WindowManager {
	public void addWindow(Window window);
	public void removeWindow(Window window);
	public void removeWindow(String name);
	public Window getWindow(String name);
	public Window getActiveWindow();
	public boolean windowExists(String name);
	public Collection<Window> getWindows();
	
	public void addChangeListener(WindowManagerChangeListener listener);
	public void removeChangeListener(WindowManagerChangeListener listener);
	public Collection<WindowManagerChangeListener> getChangeListeners();
}
