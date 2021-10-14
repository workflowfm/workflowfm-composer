package com.workflowfm.composer.ui.dialogs;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.workflowfm.composer.exceptions.ComponentExceptionHandler;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.prover.Prover;

public class ConnectToReasonerDialogTest {

  private ConnectToReasonerDialog dialog;

	public ConnectToReasonerDialogTest()
  {

  }
  
  public void show() {
  
    //Create and set up the window.
    JFrame frame = new JFrame("Connect To Reasoner Dialog Tester");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    ExceptionHandler handler = new ComponentExceptionHandler(frame);

    //Display the window.
    frame.setSize(560, 200);
    frame.setBounds(100, 100, 1000, 1000);
  
    try {
      SwingUtilities.invokeAndWait(new Runnable() { 
          public void run() {
            System.out.println("Let's goooooo!");
            
            dialog = new ConnectToReasonerDialog(frame, handler);
            dialog.setup();
            dialog.setVisible(true);
            
            Prover prover = dialog.getProver();
            boolean check = prover != null;
            System.out.println("Hmm? " + check);
            if (check)
              frame.setVisible(true);
            else 
              frame.dispose();
          }});
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
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
		ConnectToReasonerDialogTest gui = new ConnectToReasonerDialogTest();
    gui.show();   
	}
}
