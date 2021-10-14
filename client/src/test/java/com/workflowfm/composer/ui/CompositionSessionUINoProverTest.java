package com.workflowfm.composer.ui;



public class CompositionSessionUINoProverTest {
	
	private static void setLookAndFeel()
	{
		// Enable antialiasing for Swing
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
	}
	
	public static void main(String args[])
	{
		setLookAndFeel();
		CompositionSessionUITest gui = new CompositionSessionUITest(false);
		gui.setup();
		gui.addStuff();
		for (int i = 0; i < gui.getTabbedPane().getTabCount(); i++) {
			System.out.println(">> " +gui.getTabbedPane().getComponentAt(i).getName() ); 
		}
	}
}
