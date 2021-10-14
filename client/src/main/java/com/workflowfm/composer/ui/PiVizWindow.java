package com.workflowfm.composer.ui;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.workflowfm.composer.utils.Utils;
import piviz.controller.PiControllerPanel;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ProcessStoreChangeListener;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.utils.UIUtils;

public class PiVizWindow implements GraphWindow, ProcessStoreChangeListener {

	public static final ImageIcon PIVIZ_ICON = UIUtils.getIcon("silk_icons/report_picture.png");
	
	private WindowManager manager;
	private ExceptionHandler handler;
	private CompositionSession session;
	private CProcess process;
	private String piviz;
	
	private ProcessGraph graph;
	//private JPanel panel;
	private PiControllerPanel pivizController;

	
	public PiVizWindow (CProcess process, String piviz, CompositionSession session, ExceptionHandler handler, WindowManager manager) {
		this.manager = manager;
		this.session = session;
		this.process = process;
		this.handler = handler;
		this.piviz = piviz;
	}

	@Override
	public void show() {
		if (process.handleInvalid(handler)) return;
		
		File tmpFile;
		try {
			tmpFile = File.createTempFile("piviz", ".txt");
			tmpFile.deleteOnExit();
			Utils.writeStringToFile(tmpFile, this.piviz);
		} catch (IOException e) {
			handler.handleException("Failed to write temporary PiViz file.", e);
			return;
		}
		
		System.out.println(tmpFile.getAbsolutePath());
		
		JPanel graphPanel = new JPanel(new BorderLayout(10, 10));
		graph = new ProcessGraph();
		graph.getGraphEngine().insertCells(process.getFullGraph().getGraphEngine().cloneAllCells());
		graph.layout();
		graphPanel.add(graph.getGraphEngine().getGraphComponent(), BorderLayout.CENTER);
		
		pivizController = new PiControllerPanel(tmpFile);
		//TODO markers 
		//			piviz.getFrame().setGraphListener(new GraphListener() {
		//
		//				@Override
		//				public void nodeClicked(String nodeName)
		//				{
		//					clearMarkers();
		//
		//					Log.d("Node clicked " + nodeName);
		//					graph.printValuesAndIds();
		//					Object node = graph.getServiceVertexByLabel(nodeName);
		//
		//					mxCellMarker marker = new mxCellMarker(graph.getGraphComponent());
		//					marker.highlight(graph.getView().getState(node), Color.green);
		//					markers.add(marker);
		//				}
		//
		//				@Override
		//				public void edgeClicked(String edgeName)
		//				{
		//					Log.d("Edge clicked " + edgeName);
		//				}
		//			});
		//pivizController.getFrame().setTitle(getName());
		//pivizController.getFrame().setJMenuBar(null);
		pivizController.getPanel().setEmbeddedPanel(graphPanel);
		//pivizController.getFrame().pack();
		//pivizController.getFrame().setPreferredSize(new Dimension(gui.getWidth() * 3 / 4, (int) gui.getHeight() * 3 / 4));
		//pivizController.getFrame().setSize(new Dimension(gui.getWidth() * 3 / 4, (int) gui.getHeight() * 3 / 4));

		
		//panel = new JPanel(new BorderLayout());
		//pivizController.getFrame().setLocationRelativeTo(panel);
		//panel.add(pivizController.getFrame().getJMenuBar(),BorderLayout.NORTH);
		//panel.add(pivizController.getFrame().getContentPane(),BorderLayout.CENTER);
		//pivizController.getFrame().setVisible(false);
		pivizController.getPanel().setName(getName());
		
		manager.addWindow(this);
		
		session.addChangeListener(this);
	}
	
	@Override
	public void dispose() {
		session.removeChangeListener(this);
		//pivizController.getFrame().dispose();
		manager.removeWindow(this);
	}
	
	@Override
	public String getName() {
		return "\u03C0-calculus for " + process.getName();
	}

	@Override
	public JPanel getPanel() {
		return this.pivizController.getPanel();
	}

	@Override
	public ProcessGraph getGraph() {
		return this.graph;
	}
	
	@Override
	public Icon getIcon() {
		return PiVizWindow.PIVIZ_ICON;
	}

	@Override
	public void processAdded(CProcess process) { }

	@Override
	public void processUpdated(String previousName, CProcess process) {
		if (previousName.equals(this.process.getName())) {
			graph.getGraphEngine().clear(); // TODO is that all we should be doing here?
			this.process = process;
		}
	}

	@Override
	public void processRemoved(CProcess process) {
		if (process.getName().equals(this.process.getName())) {
			graph.getGraphEngine().clear();
		}
	}

	@Override
	public WindowManager getManager() {
		return this.manager;
	}
}
