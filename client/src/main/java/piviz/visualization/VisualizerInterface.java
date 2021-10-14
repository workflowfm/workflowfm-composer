package piviz.visualization;

import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JScrollPane;

import piviz.exceptions.VisualizationException;
import piviz.executionEngine.ASTReceive;
import piviz.executionEngine.ASTSend;
import piviz.executionEngine.ASTTau;


/**
 * Interface hiding the specialized visualizer for Pi-systems
 * @author Anja
 *
 */
public interface VisualizerInterface {
	public void addActionListener(ActionListener listener);
	
	public void removeActionListener(ActionListener listener);
	
	public JScrollPane createVisualOutput(ArrayList tauNodes,
			ArrayList commNodes, ArrayList allProcesses,
			ArrayList blockedNodes, Hashtable scope, boolean scaleToFit)
			throws FileNotFoundException, VisualizationException;
	
	public void clearSelection();
	
	public ASTReceive getCurrentlySelectedReceiveNode();
	
	public ASTSend getCurrentlySelectedSendNode();
	
	public ASTTau getCurrentlySelectedTauNode();
	
	public void updateAfterAgentImport();
	
	public void setGraphListener(GraphListener graphListener);
}
