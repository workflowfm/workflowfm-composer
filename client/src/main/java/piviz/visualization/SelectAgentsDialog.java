/**
 * Created by Anja Bog. Do not edit this line.
 */
package piviz.visualization;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import piviz.executionEngine.ASTAgentDefinition;


public class SelectAgentsDialog extends JDialog implements ActionListener,ListSelectionListener{
	
	private ArrayList agents;
	
	private ArrayList selection = new ArrayList();
	
	private JList list;
	
	SelectAgentsDialog(JFrame owner, ArrayList _agents){
		super(owner, true);
		setTitle("Select Agents");
		agents = _agents;
	}
	
	public ArrayList open(){
		
		// row layout for the dialog
		this.getContentPane().setLayout(new BorderLayout(10,10));
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout());
		this.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		JButton ok = new JButton();
		ok.setActionCommand("OK");
		ok.setText("OK");
		ok.addActionListener(this);
		buttonPane.add(ok);
				
		JPanel listPane = new JPanel();
		listPane.setLayout(new FlowLayout());
		this.getContentPane().add(listPane,BorderLayout.CENTER);
		
		
		DefaultListModel listModel = new DefaultListModel();
		// add the agents to the list
		for (int i = 0; i < agents.size(); i++){
			ASTAgentDefinition n = (ASTAgentDefinition)agents.get(i);
			listModel.addElement(n.getAgentName());
			
		}		
		
		list = new JList(listModel);
		list.addListSelectionListener(this);		
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 300));
		listPane.add(listScroller);
		
		selection.clear();		
		this.pack();
		this.show();		
		return selection;
	}
	
	private void setSelection(){
		int[] indices = list.getSelectedIndices();
		for (int i = 0; i < indices.length; i++){
			if (!selection.contains(agents.get(indices[i])))
				selection.add(agents.get(indices[i]));
		}	
		this.dispose();
	}
	
	public void actionPerformed(ActionEvent e){
		if (e.getActionCommand().equals("OK"))
			setSelection();
	}
	
	public void valueChanged(ListSelectionEvent e) {}

}
