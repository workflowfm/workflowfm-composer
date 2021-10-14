/**
 * Created by Anja Bog. Do not edit this line.
 */
package piviz.visualization;


import java.awt.event.ActionListener;
import java.io.File;

import piviz.executionEngine.ASTReceive;
import piviz.executionEngine.ASTSend;
import piviz.executionEngine.ASTTau;
import piviz.executionEngine.PiExecutorInterface;


/**
 * Interface hiding the specialized graphical user interface.
 * @author Anja
 *
 */
public interface GUIInterface{

	public void addActionListener(ActionListener listener);

	public void removeActionListener(ActionListener listener);

	public File getFile();

	public ASTReceive getCurrentlySelectedReceiveNode();

	public ASTSend getCurrentlySelectedSendNode();

	public ASTTau getCurrentlySelectedTauNode();

	public void setExecutor(PiExecutorInterface executor);

	public void startNewExecution(boolean executionPossible);

	public void showError(String message);

	public PiExecutorInterface getExecuter();

	public void setStateAfterExecution(boolean moreSteps,
			boolean updateVis);
}
