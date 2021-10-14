/**
 * Created by Anja Bog. Do not edit this line.
 */
/**
 The MIT License

 Copyright (c) 2006 Anja Bog

 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation files
 (the "Software"), to deal in the Software without restriction, 
 including without limitation the rights to use, copy, modify, merge, 
 publish, distribute, sublicense, and/or sell copies of the Software, 
 and to permit persons to whom the Software is furnished to do so, 
 subject to the following conditions:

 The above copyright notice and this permission notice shall be included 
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
 IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package piviz.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import piviz.exceptions.ControllerException;
import piviz.exceptions.PiExecutionException;
import piviz.exceptions.PiParserError;
import piviz.exceptions.RestrictionTableException;
import piviz.executionEngine.PiExecutor;
import piviz.executionEngine.PiExecutorInterface;
import piviz.helperClasses.EventValues;
import piviz.visualization.GUIInterface;
import piviz.visualization.PiMainFrame;

/**
 * Controller for Model View Controller Pattern - collects user input and
 * triggers appropriate execution steps of the Pi-Executor
 * 
 * @author Anja
 */
public class PiController implements ActionListener
{
  private PiMainFrame mainFrame;

  /**
   * Controller constructor
   */
  public PiController(File file)
  {
    mainFrame = new PiMainFrame(null);
    mainFrame.addActionListener(this);

    if (file != null)
      mainFrame.doOpenFile(file);
  }
  
  public PiMainFrame getFrame()
  {
    return mainFrame;
  }

  /**
   * Applications main method
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    new PiController(args.length > 0 ? new File(args[0]) : null);
  }

  /**
   * Catch the events from the observed objects. In our case this is the GUI.
   */
  public void actionPerformed(ActionEvent e)
  {
    GUIInterface mainFrame = (GUIInterface) e.getSource();
    PiExecutorInterface executer = mainFrame.getExecuter();
    int eventID = e.getID();
    switch (eventID)
    {
      case EventValues.START_EXECUTION:
        startExecution(mainFrame);
        break;
      case EventValues.AUTO_EXECUTION:
        autoExecute(mainFrame, executer);
        break;
      case EventValues.DOT_DEFINITION_TREE:
        dotDefTree(mainFrame, executer);
        break;
      case EventValues.DOT_EXEC_TREE:
        dotExecTree(mainFrame, executer);
        break;
      case EventValues.EXECUTE_COMMUNICATION:
        executeStep(mainFrame, executer, eventID);
        break;
      case EventValues.EXECUTE_COMMUNICATION_INVISIBLY:
        executeStep(mainFrame, executer, eventID);
        break;
      case EventValues.EXECUTE_TAU:
        executeStep(mainFrame, executer, eventID);
        break;
      case EventValues.EXECUTE_TAU_INVISIBLY:
        executeStep(mainFrame, executer, eventID);
        break;
      default:
        break;
    }
  }

  /**
   * Trigger execution of selected step.
   * 
   * @param mainFrame
   * @param executor
   * @param eventID
   */
  private void executeStep(GUIInterface mainFrame, PiExecutorInterface executor, int eventID)
  {
    try
    {
      if (mainFrame == null || executor == null)
        throw new ControllerException("Internal error: Main frame or executor object was null during start of execution.");

      if (eventID == EventValues.EXECUTE_COMMUNICATION || eventID == EventValues.EXECUTE_COMMUNICATION_INVISIBLY)
      {
        boolean moreSteps = executor.execute(mainFrame.getCurrentlySelectedSendNode(), mainFrame.getCurrentlySelectedReceiveNode(), null);
        if (eventID == EventValues.EXECUTE_COMMUNICATION)
          mainFrame.setStateAfterExecution(moreSteps, true);
        else
          mainFrame.setStateAfterExecution(moreSteps, false);
      }
      else
      {
        boolean moreSteps = executor.execute(null, null, mainFrame.getCurrentlySelectedTauNode());
        if (eventID == EventValues.EXECUTE_TAU)
          mainFrame.setStateAfterExecution(moreSteps, true);
        else
          mainFrame.setStateAfterExecution(moreSteps, false);
      }
    }
    catch (ControllerException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (PiExecutionException e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
    catch (RestrictionTableException e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Set up the pi-executor for a new execution.
   * 
   * @param mainFrame
   */
  private void startExecution(GUIInterface mainFrame)
  {
    try
    {
      if (mainFrame == null)
        throw new ControllerException("Main frame object was null during start of execution.");
      PiExecutorInterface executer = new PiExecutor(mainFrame.getFile());
      mainFrame.setExecutor(executer);
      boolean executionPossible;
      executionPossible = executer.startExecution();
      mainFrame.startNewExecution(executionPossible);
    }
    catch (FileNotFoundException e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
    catch (PiExecutionException e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
    catch (RestrictionTableException e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
    catch (PiParserError e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
    catch (ControllerException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Tell the executor to automatically select the next step to execute and
   * execute it.
   * 
   * @param mainFrame
   * @param executer
   */
  private void autoExecute(GUIInterface mainFrame, PiExecutorInterface executer)
  {
    try
    {
      if (mainFrame == null || executer == null)
        throw new ControllerException("Main frame or executer object was null during auto execution.");
      boolean moreSteps;
      moreSteps = executer.autoExecute();
      mainFrame.setStateAfterExecution(moreSteps, true);
    }
    catch (PiExecutionException e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
    catch (RestrictionTableException e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
    catch (ControllerException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Create a dot and a png file of the pi-systems definition tree.
   * 
   * @param mainFrame
   * @param executer
   */
  private void dotDefTree(GUIInterface mainFrame, PiExecutorInterface executer)
  {
    try
    {
      if (mainFrame == null || executer == null)
        throw new ControllerException("Main frame or executer object was null during dotting of definition tree.");
      executer.dotDefinitionTree();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
    catch (ControllerException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Create a dot and a png file for the pi-systems execution tree in the
   * current state.
   * 
   * @param mainFrame
   * @param executer
   */
  private void dotExecTree(GUIInterface mainFrame, PiExecutorInterface executer)
  {
    try
    {
      if (mainFrame == null || executer == null)
        throw new ControllerException("Main frame or executer object was null during dotting of execution tree.");
      executer.dotCurrentExecTree();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      mainFrame.showError(e.getMessage());
      e.printStackTrace();
    }
    catch (ControllerException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
