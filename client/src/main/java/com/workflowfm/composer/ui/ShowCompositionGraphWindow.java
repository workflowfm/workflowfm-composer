package com.workflowfm.composer.ui;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ProcessStoreChangeListener;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.utils.UIUtils;

// TODO Add show graph action and popup panel for composite components
public class ShowCompositionGraphWindow implements GraphWindow, ProcessStoreChangeListener {

	public static final ImageIcon SHOWCOMPOSITIONGRAPH_ICON = UIUtils.getIcon("silk_icons/chart_organisation.png");
	
	private WindowManager manager;
	private CompositionSession session;
	protected CProcess process;
	
	private ProcessGraph graph;
	private JPanel panel;
	
	public ShowCompositionGraphWindow (CProcess process, CompositionSession session, WindowManager manager) {
		this.manager = manager;
		this.process = process;
		this.session = session;
	}

	@Override
	public void show() {
		graph = new ProcessGraph();
		graph.insertGraph(process.getFullGraph());
		graph.layout();

		panel = new JPanel(new BorderLayout());
	
		panel.add(graph.getGraphEngine().getGraphComponent(),BorderLayout.CENTER);
		panel.setName(getName());
		
		manager.addWindow(this);
		
		session.addChangeListener(this);
	}
	
	@Override
	public void dispose() {
		session.removeChangeListener(this);
		manager.removeWindow(this);
	}
	
	@Override
	public String getName() {
		return "Graph of " + process.getName();
	}

	@Override
	public JPanel getPanel() {
		return this.panel;
	}

	@Override
	public ProcessGraph getGraph() {
		return this.graph;
	}
	
	@Override
	public Icon getIcon() {
		return ShowCompositionGraphWindow.SHOWCOMPOSITIONGRAPH_ICON;
	}

	@Override
	public WindowManager getManager() {
		return this.manager;
	}

	@Override
	public void processAdded(CProcess process) { }

	@Override
	public void processUpdated(String previousName, CProcess process) {
		if (previousName.equals(this.process.getName())) {
			graph.getGraphEngine().clear();
			graph.insertGraph(process.getFullGraph());
			graph.layout();
			this.process = process;
		}		
	}

	@Override
	public void processRemoved(CProcess process) {
		//if (process.getName().equals(this.process.getName()))
		// TODO need to also check dependencies! leave as is?
		dispose();
	}
}
