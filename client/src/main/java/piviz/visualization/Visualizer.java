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
package piviz.visualization;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.EventListenerList;

import piviz.att.grappa.Edge;
import piviz.att.grappa.Element;
import piviz.att.grappa.Graph;
import piviz.att.grappa.Grappa;
import piviz.att.grappa.GrappaConstants;
import piviz.att.grappa.GrappaPanel;
import piviz.att.grappa.Node;
import piviz.att.grappa.Parser;
import piviz.att.grappa.Subgraph;
import piviz.exceptions.VisualizationException;
import piviz.executionEngine.ASTReceive;
import piviz.executionEngine.ASTReplication;
import piviz.executionEngine.ASTSend;
import piviz.executionEngine.ASTTau;
import piviz.executionEngine.SimpleNode;
import piviz.helperClasses.PidGenerator;


/**
 * Visualize the internal data structure and create a process graph.
 * 
 * @author Anja
 * 
 */
public class Visualizer implements VisualizerInterface {
	private Graph graph;

	// / Tau node which has been selected by the user
	private ASTTau currentlySelectedTauNode;

	// / Send node which has been selected by the user
	private ASTSend currentlySelectedSendNode;

	// / Receive node which has been selected by the user
	private ASTReceive currentlySelectedReceiveNode;

	// / Contains information about the pool agents belong to.
	private Hashtable poolTable;

	// / For sending changed events to those who are interested.
	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * Contains the information if the nodes within a pool are visible (value =
	 * true) or not. poolVisibleTable = <key: poolname><value: boolean>
	 */
	private Hashtable poolVisibleTable;

	// / This keeps track which nodes belong to which edge, so if an edge is
	// / clicked, these nodes can be executed.
	private Hashtable executionNodeLookup;

	private Hashtable tauNodeLookup;

	private static Properties props;
	
  private MyGrappaListener listener;
  
  private GraphListener graphListener;

	/**
	 * Constructor.
	 * 
	 * @param _frame
	 *            Reference to parent frame for showing message dialogs.
	 */
	public Visualizer(Hashtable _poolTable, Properties properties) {
		this.executionNodeLookup = new Hashtable();
		this.tauNodeLookup = new Hashtable();
		this.poolTable = _poolTable;
		this.poolVisibleTable = new Hashtable();
		this.currentlySelectedTauNode = null;
		graph = null;

		props = properties;

		// fill the poolVisibleTable
		if (poolTable.size() > 0) {
			for (Enumeration e = poolTable.elements(); e.hasMoreElements();) {
				String poolName = (String) e.nextElement();
				if (!poolVisibleTable.containsKey(poolName)) {
					poolVisibleTable.put(poolName, new Boolean(true));
				}
			}
		}
	}
	
	public void setGraphListener(GraphListener graphListener)
	{
	  this.graphListener = graphListener;
	  
	  if (listener != null)
	    listener.setGraphListener(graphListener);
	}

	public void updateAfterAgentImport() {
		// update the pool visible table if new agents have been imported
		// fill the poolVisibleTable
		if (poolTable.size() > 0) {
			for (Enumeration e = poolTable.elements(); e.hasMoreElements();) {
				String poolName = (String) e.nextElement();
				if (!poolVisibleTable.containsKey(poolName)) {
					poolVisibleTable.put(poolName, new Boolean(true));
				}
			}
		}
	}

	public ASTTau getCurrentlySelectedTauNode() {
		return currentlySelectedTauNode;
	}

	public ASTSend getCurrentlySelectedSendNode() {
		return currentlySelectedSendNode;
	}

	public ASTReceive getCurrentlySelectedReceiveNode() {
		return currentlySelectedReceiveNode;
	}

	/**
	 * Create the graphical components needed to display the graph and trigger
	 * creation of the graph itself.
	 * 
	 * @param tauNodes
	 *            All currently executable tau nodes.
	 * @param commNodes
	 *            All currently executable communications.
	 * @param allProcesses
	 *            All running processes.
	 * @param blockedNodes
	 *            All nodes currently blocked.
	 * @return ScrollPane containing the visualization.
	 * @throws VisualizationException
	 */
	public JScrollPane createVisualOutput(ArrayList tauNodes,
			ArrayList commNodes, ArrayList allProcesses,
			ArrayList blockedNodes, Hashtable scope, boolean scaleToFit)
			throws FileNotFoundException, VisualizationException {

		/*
		 * Before actually creating the output we have to check if there are
		 * closed pools and if so all possible actions within these pools have
		 * to be executed otherwise the user might be confronted with "invisible
		 * actions".
		 */

		ArrayList closedPools = new ArrayList();
		for (Enumeration e = poolVisibleTable.keys(); e.hasMoreElements();) {
			String poolName = (String) e.nextElement();
			if (!((Boolean) poolVisibleTable.get(poolName)).booleanValue()) {
				closedPools.add(poolName);
			}
		}

		// Copy the Arrays
		ArrayList copyTauNodes = new ArrayList(tauNodes);
		ArrayList copyCommNodes = new ArrayList(commNodes);
		boolean redo = true;
		// boolean commRedo, tauRedo;

		while (redo) {
			// commRedo = false;
			// tauRedo = false;
			redo = false;
			if (closedPools.size() > 0) {
				// auto-execute the taus
				if (copyTauNodes.size() > 0) {
					for (int i = 0; i < copyTauNodes.size(); i++) {
						ASTTau tau = (ASTTau) copyTauNodes.get(i);
						if (tau != null) {
							String poolName = (String) poolTable
									.get(PidGenerator.getNameWithoutId(tau
											.getProcessNameAtRoot()));
							/*
							 * if this pool is closed, execute the tau node
							 */
							if (poolName != null) {
								if (closedPools.contains(poolName)) {
									/*
									 * See if the selected tau node is not a
									 * descendent of a replication node
									 * otherwise we would end up in endless
									 * loops
									 */
									ASTReplication repNode = tau
											.getTopMostReplicationNode(null);
									if (repNode == null) {
										executeTauNode(tau, false);
										/*
										 * reset the i after this is done and
										 * again copy the new tauNodes List
										 */
										redo = true;
										i = -1;
										copyTauNodes.clear();
										copyTauNodes.addAll(tauNodes);
										copyCommNodes.clear();
										copyCommNodes.addAll(commNodes);
									}
								}
							}
						}
					}
				}
				// auto-execute the communications
				if (copyCommNodes.size() > 0) {
					for (int i = 0; i < copyCommNodes.size(); i++) {
						ArrayList nodeList = (ArrayList) copyCommNodes.get(i);
						if (nodeList != null) {
							ListIterator nodeListIter = nodeList.listIterator();
							while (nodeListIter.hasNext()) {
								SimpleNode n = (SimpleNode) nodeListIter.next();
								if (n != null) {
									if (n.toString().equals("Send")) {
										/*
										 * Check if the send node is within one
										 * closed pool, if not skip.
										 */

										String poolName = (String) poolTable
												.get(PidGenerator
														.getNameWithoutId(n
																.getProcessNameAtRoot()));
										/*
										 * if this pool is closed, find a
										 * receive node which also is in this
										 * pool and then execute them.
										 */

										if (poolName != null) {
											if (closedPools.contains(poolName)) {
												ListIterator nodeListIterRec = nodeList
														.listIterator();
												while (nodeListIterRec
														.hasNext()) {
													SimpleNode rec = (SimpleNode) nodeListIterRec
															.next();
													if (rec != null) {
														if (rec
																.toString()
																.equals(
																		"Receive")) {
															String recPoolName = (String) poolTable
																	.get(PidGenerator
																			.getNameWithoutId(rec
																					.getProcessNameAtRoot()));
															if (recPoolName != null) {
																if (recPoolName
																		.equals(poolName)) {
																	// TODO:
																	// Do
																	// not
																	// auto-execute
																	// if
																	// both
																	// are
																	// under
																	// a
																	// replication
																	executeCommunication(
																			(ASTSend) n,
																			(ASTReceive) rec,
																			false);
																	/*
																	 * Reset the
																	 * i and
																	 * make a
																	 * copy of
																	 * the
																	 * changed
																	 * commNodes
																	 * list
																	 */
																	redo = true;
																	copyCommNodes
																			.clear();
																	copyCommNodes
																			.addAll(commNodes);
																	copyTauNodes
																			.clear();
																	copyTauNodes
																			.addAll(tauNodes);
																	i = -1;

																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			// redo = commRedo || tauRedo;
		}

		// create the graph
		if (scope == null)
			scope = new Hashtable();
		createGraph(tauNodes, commNodes, allProcesses, blockedNodes, scope);

		if (!filterGraph(graph)) {
			System.err.println("ERROR: somewhere in filterGraph");
		}

		JScrollPane jsp = new JScrollPane();
		jsp.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		jsp.getViewport().setBackground(Color.WHITE);

		GrappaPanel gp = new GrappaPanel(graph);
		listener = new MyGrappaListener(this);
		
		if (graphListener != null)
		  listener.setGraphListener(graphListener);
    
		gp.addGrappaListener(listener);
		gp.setScaleToFit(scaleToFit);

		jsp.setViewportView(gp);

		return jsp;

	}

	/**
	 * Trigger execution of the selected edge. Callback for MyGrappaListener
	 * 
	 * @param edge
	 *            Edge the user has selected for execution.
	 */
	public void executeStepForEdge(Edge edge) {

		if (executionNodeLookup.containsKey(edge.getName())) {
			setSelection(edge);
			SimpleNode[] nodes = (SimpleNode[]) executionNodeLookup.get(edge
					.getName());
			if (nodes != null) {
				executeCommunication((ASTSend) nodes[0], (ASTReceive) nodes[1],
						true);
			} else
				clearSelection();
		}
	}

	/**
	 * Send a signal for executing the selected communication.
	 * 
	 * @param send
	 * @param rec
	 * @param updateVis
	 *            True if the visual output has changed and the view has to be
	 *            updated, else false.
	 */
	private void executeCommunication(ASTSend send, ASTReceive rec,
			boolean updateVis) {
		currentlySelectedReceiveNode = rec;
		currentlySelectedSendNode = send;
		ActionEvent e = null;
		if (updateVis)
			e = new ActionEvent(this, 0, "executeCommunication");
		else
			e = new ActionEvent(this, 0, "executeCommunicationNoVisUpdate");
		fireActionPerformed(e);
	}

	/**
	 * Callback for MyGrappaListener
	 * 
	 * @param node
	 */
	public void executeTauNode(Node node) {
		if (tauNodeLookup.containsKey(node.getName())) {
			setSelection(node);
			executeTauNode((ASTTau) tauNodeLookup.get(node.getName()), true);
		}
	}

	/**
	 * Send a signal for executing the selected tau node.
	 * 
	 * @param node
	 * @param updateVis
	 *            True if the visual output has changed and the view has to be
	 *            updated, else false.
	 */
	private void executeTauNode(ASTTau node, boolean updateVis) {
		currentlySelectedTauNode = node;
		ActionEvent e = null;
		if (updateVis)
			e = new ActionEvent(this, 0, "executeTauNode");
		else
			e = new ActionEvent(this, 0, "executeTauNodeNoVisUpdate");
		fireActionPerformed(e);
	}

	/**
	 * When clicked on a pool the pool closes or opens to show its inner nodes.
	 * 
	 * @param g
	 */
	public void togglePoolVisibility(String name) {
		if (name == null)
			return;
		if (poolVisibleTable.containsKey(name)) {
			boolean val = ((Boolean) poolVisibleTable.get(name)).booleanValue();
			poolVisibleTable.remove(name);
			poolVisibleTable.put(name, new Boolean(!val));
			// emit event that the visualization has changed - so the Main Frame
			// can redraw.
			ActionEvent e = new ActionEvent(this, 0, "updateGrappaPanel");
			fireActionPerformed(e);
		}
	}

	/**
	 * Clears the selection if the user decides not to execute the selected
	 * communication.
	 * 
	 */
	public void clearSelection() {
		if (graph.currentSelection instanceof Element)
			((Element) graph.currentSelection).highlight = GrappaConstants.HIGHLIGHT_OFF;
		graph.currentSelection = null;
		graph.getGraph().repaint();

	}

	/**
	 * Set the selection to show which edge he has selected.
	 * 
	 * @param edge
	 *            Edge to be marked.
	 */
	private void setSelection(Element elem) {
		elem.highlight |= GrappaConstants.SELECTION_MASK;
		graph.currentSelection = elem;
		graph.getGraph().repaint();

	}

	/**
	 * Create a graph and add all the nodes and the communications.
	 * 
	 * @param tauNodes
	 *            All currently executable tau nodes.
	 * @param commNodes
	 *            All possible communication steps.
	 * @param allProcesses
	 *            All of the existing processes.
	 * @throws VisualizationException
	 */
	private void createGraph(ArrayList tauNodes, ArrayList commNodes,
			ArrayList allProcesses, ArrayList blockedNodes, Hashtable scope)
			throws VisualizationException {

		if (graph == null) {
			graph = new Graph("G");
		} else {
			graph.reset();
			executionNodeLookup.clear();
			tauNodeLookup.clear();

		}

		graph.setMenuable(true);
		graph.setNodeAttribute("shape", "circle");
		graph.setEdgeAttribute("color", "black");
		// Grappa does not understand this style
		// graph.setEdgeAttribute("arrowhead", "dot");

		// create the subgraphs for the pools and fill the poolVisibleTable
		if (poolVisibleTable.size() > 0) {
			for (Enumeration e = poolVisibleTable.keys(); e.hasMoreElements();) {
				String poolName = (String) e.nextElement();
				makeSubgraph(poolName);
			}
		}

		Hashtable resultingScopes = new Hashtable();

		/*
		 * Create a node for each running process. This ensures that agents are
		 * displayed in the graphical representation, even if they have no
		 * capabilities in the current state. - - Special case restriction:
		 * don't create a node if the subtree splits to different agents.
		 * (processname in the subtree is different from the one in the roots
		 * child).
		 */
		if (allProcesses != null) {
			for (int i = 0; i < allProcesses.size(); i++) {
				SimpleNode n = (SimpleNode) allProcesses.get(i);
				if (n != null) {
					if (n.toString().equals("Replication")) {
						if (n.jjtGetNumChildren() > 0) {
							SimpleNode child = (SimpleNode) n.jjtGetChild(0);
							// go all the way down
							if (child != null) {
								while (child.jjtGetNumChildren() > 0
										&& child.jjtGetChild(0) != null) {
									child = (SimpleNode) child.jjtGetChild(0);
								}
							}
							// if processnames match -> make node
							if (child.getProcessName().equals(
									n.getProcessName()))
								makeNode(n, scope, resultingScopes);
						}

					} else
						makeNode(n, scope, resultingScopes);
				}
			}
		}

		// go through the executable nodes and create nodes and edges between
		// these nodes.
		makeNodesAndEdges(commNodes, false, scope, resultingScopes);

		// go through the blocked nodes and create nodes and edges between them
		makeNodesAndEdges(blockedNodes, true, scope, resultingScopes);

		// handle the tau nodes
		if (tauNodes.size() > 0) {
			ListIterator iter = tauNodes.listIterator();
			while (iter.hasNext()) {
				ASTTau tauNode = (ASTTau) iter.next();
				if (tauNode != null) {
					Node node = makeNode(tauNode, scope, resultingScopes);
					if (node != null) {
						node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
						node.setAttribute(GrappaConstants.FILLCOLOR_ATTR,
								"gray");
						tauNodeLookup.put(node.getName(), tauNode);
					}
				}

			}
		}

		// handle scopes
		makeScopes(resultingScopes);

		graph.removeEmptySubgraphs();
		
		// Layout graph horizontally instead of the vertical default
		graph.setAttribute(GrappaConstants.RANKDIR_ATTR, "LR");
	}

	/**
	 * For all the send and receive nodes in the sortedNodes array create new
	 * nodes if they do not exist yet and edges between them. Depending on if
	 * these edges are currently blocked from execution or not, the have a
	 * different appearance. Blocked edges are gray and the others are black.
	 * Edges that are not blocked are inserted into a hash table with the
	 * corresponding nodes for execution.
	 * 
	 * @param sortedNodes
	 *            Array containing the nodes.
	 * @param execBlocked
	 *            True if edge is blocked from execution, otherwise false.
	 * @throws VisualizationException
	 */
	private void makeNodesAndEdges(ArrayList sortedNodes, boolean execBlocked,
			Hashtable scope, Hashtable resultingScopes)
			throws VisualizationException {
		// go through the nodes and create nodes and edges between
		// these nodes.
		if (sortedNodes != null) {
			ListIterator commIter = sortedNodes.listIterator();
			while (commIter.hasNext()) {
				// get a nodeList regarding one channel
				ArrayList nodeList = (ArrayList) commIter.next();
				if (nodeList != null) {
					// find the send nodes and for each send node add the
					// receive nodes
					// and the channels in between.
					ListIterator nodeListIter = nodeList.listIterator();
					while (nodeListIter.hasNext()) {
						SimpleNode n = (SimpleNode) nodeListIter.next();
						if (n != null) {
							if (n.toString().equals("Send")) {
								Node sendGraphNode = makeNode(n, scope,
										resultingScopes);

								// go through the nodeList and for each receive
								// node add this receive node
								// and the channel
								ListIterator nodeListIterRec = nodeList
										.listIterator();
								while (nodeListIterRec.hasNext()) {
									SimpleNode rec = (SimpleNode) nodeListIterRec
											.next();
									if (rec != null) {
										if (rec.toString().equals("Receive")) {
											Node recGraphNode = makeNode(rec,
													scope, resultingScopes);
											makeEdge(sendGraphNode,
													recGraphNode, (ASTSend) n,
													(ASTReceive) rec,
													execBlocked);

										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Use the dot engine to do the layout.
	 * 
	 * @param graph
	 * @return
	 * @throws VisualizationException
	 */
	private static boolean filterGraph(Graph graph)
			throws FileNotFoundException, VisualizationException {

		OutputStream toFilterRaw = null;
		File file = null;
		try {
			file = File.createTempFile("piviz", ".dot");
			file.deleteOnExit();
		} catch (IOException e) {
			throw new VisualizationException(e.getMessage());
		}

		BufferedReader fromFilter = null;
		StringBuffer newGraph = null;
		toFilterRaw = new FileOutputStream(file);

		BufferedWriter toFilter = new BufferedWriter(new OutputStreamWriter(
				toFilterRaw));
		String content = null;
		boolean status = true;
		graph.filterMode = true;
		try {
			StringWriter theGraph = new StringWriter();
			graph.printGraph(theGraph);
			theGraph.flush();
			content = theGraph.toString();
			theGraph.close();
		} catch (Exception ex) {
			throw new VisualizationException(ex.getMessage());
		} finally {
			graph.filterMode = false;
		}
		try {
			toFilter.write(content, 0, content.length());
			toFilter.flush();
			toFilter.close();
		} catch (Exception ex) {
			throw new VisualizationException(ex.getMessage());
		}
		// now use the layouting engine dot to receive a better layout

		Process proc = null;
		try {
			
			proc = Runtime.getRuntime().exec(
					props.getProperty("pathToDot") + " " + file.getPath());

			fromFilter = new BufferedReader(new InputStreamReader(proc
					.getInputStream()));
			newGraph = new StringBuffer(content.length() + 128);

			String line = null;
			while ((line = fromFilter.readLine()) != null) {
				newGraph.append(line);
				// assume a lone right-brace on a line is the end-of-graph

				if (line.equals("}") || line.equals("}\r")) {
					break;
				}
				/*
				 * Need to append new-line on the chance that there was a
				 * backslash-newline (otherwise need to test for a lone
				 * backslash at the end of the string and remove it... cheaper
				 * to just append a newline.
				 */
				newGraph.append(Grappa.NEW_LINE);
			}
		} catch (IOException ex) {
			throw new VisualizationException(
					"An installation of Graphviz dot is required for the layout\n" +
					"of the graphical representation.\n"
							+ "Executable packages for installation can be found at:\n"
							+ "   http://www.graphviz.org/\n\n"
							+ "Configure the execution path of Graphviz dot with the option\n"
							+ "  'Set dot execution path..' in the File menu.\n"
							+ "Example:   C:\\Programme\\GraphvizDot\\Graphviz\\bin\\dot\n"
							+ "If no correct path is given, the graph will not be displayed.");


		}
		try {
			fromFilter.close();
		} catch (IOException io) {
		}
		Reader fromReader = null;
		try {
			fromReader = new StringReader(newGraph.toString());
		} catch (Exception ex) {
			throw new VisualizationException(ex.getMessage());
		}
		graph.reset();
		Parser program = new Parser(fromReader, graph.getErrorWriter(), graph);
		try {
			program.parse();
		} catch (Exception ex) {
			try {
				fromReader.close();
				fromReader = new StringReader(content);
			} catch (Exception ex2) {
				throw new VisualizationException(ex2.getMessage());
			}
			program = new Parser(fromReader, graph.getErrorWriter(), graph);
			try {
				program.parse();
			} catch (Exception ex2) {
				throw new VisualizationException(ex2.getMessage());
			}
			throw new VisualizationException(ex.getMessage());
		}
		return status;
	}

	/**
	 * Create a new node if it does not already exist.
	 * 
	 * @param n
	 *            Corresponding node of the data structure.
	 * @param scope
	 *            Hashtable containing the scope name and the list of process
	 *            identifiers used to build the resultingScopes table for
	 *            rendering of scopes.
	 * @param resultingScopes
	 *            Hashtable containing the scope name and the list of grappa
	 *            nodes contained in that scope
	 * @return The created node or pointer to existing node.
	 * @throws VisualizationException
	 */
	private Node makeNode(SimpleNode n, Hashtable scope,
			Hashtable resultingScopes) throws VisualizationException {
		String label = PidGenerator.getNameWithoutId(n.getProcessNameAtRoot());
		String nodeName = n.getProcessName();
		// Node graphNode = (Node) graph.findNodeByName(n.getPid());
		Node graphNode = (Node) graph.findNodeByName(nodeName);// label
		if (graphNode == null) {
			// graphNode = new Node(graph, n.getPid());
			// see if the new node belongs to a pool
			String poolName = (String) poolTable.get(PidGenerator
					.getNameWithoutId(n.getProcessNameAtRoot()));
			Subgraph sg = graph;
			if (poolName != null) {
				// see if the pool is closed or open
				Element subg = makeSubgraph(poolName);
				if (subg == null)
					throw new VisualizationException(
							"Visualizer.makeNode(): Subgraph of the node does not exist, but it should have been created previously.");
				else if (subg instanceof Subgraph) {
					graphNode = new Node((Subgraph) subg, nodeName);// label
					graphNode.setAttribute(GrappaConstants.LABEL_ATTR, label);
					graphNode.setAttribute(GrappaConstants.TIP_ATTR, label
							+ " " + n.getPid());
					graphNode.setAttribute(GrappaConstants.SHAPE_ATTR,
							"ellipse");
				} else if (subg instanceof Node) {
					graphNode = (Node) subg;
					graphNode
							.setAttribute(GrappaConstants.LABEL_ATTR, poolName);
					graphNode.setAttribute(GrappaConstants.TIP_ATTR,
							"Click to open the pool.");
				}
			} else {
				graphNode = new Node(sg, nodeName);// label
				graphNode.setAttribute(GrappaConstants.LABEL_ATTR, label);
				graphNode.setAttribute(GrappaConstants.TIP_ATTR, label + " "
						+ n.getPid());
				graphNode.setAttribute(GrappaConstants.SHAPE_ATTR, "ellipse");
			}

		}
		/*
		 * Take care of the scope --> put it in the result scope table which
		 * will be rendered seperately
		 */
		checkNodeScope(graphNode, n, scope, resultingScopes);

		return graphNode;

	}

	/**
	 * Create a new subgraph for the pool named label.
	 * 
	 * @param label
	 */
	private Element makeSubgraph(String label) {
		if (((Boolean) poolVisibleTable.get(label)).booleanValue()) {
			Subgraph sg = graph.findSubgraphByName("cluster_" + label);
			if (sg == null) {
				sg = new Subgraph(graph, "cluster_" + label);
				sg.setAttribute(GrappaConstants.LABEL_ATTR, label);
				sg.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
				sg.setAttribute(GrappaConstants.COLOR_ATTR, "gray96");
				sg.setNodeAttribute(GrappaConstants.STYLE_ATTR, "filled");
				sg.setNodeAttribute(GrappaConstants.COLOR_ATTR, "white");

			}
			return sg;
		} else {
			Node sg = graph.findNodeByName("cluster_" + label);
			if (sg == null) {
				sg = new Node(graph, "cluster_" + label);
				sg.setAttribute(GrappaConstants.SHAPE_ATTR, "box");
				sg.setAttribute(GrappaConstants.LABEL_ATTR, label);
				sg.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
				sg.setAttribute(GrappaConstants.COLOR_ATTR, "gray96");
			}
			return sg;
		}

	}

	/**
	 * Create a new edge and add this edge to the table for look up of
	 * executable edges if this is an executable edge.
	 * 
	 * @param sendNode
	 *            Tail node of the edge.
	 * @param recNode
	 *            Head node of the edge.
	 * @param send
	 *            Actual send node to be executed.
	 * @param receive
	 *            Actual receive node to be executed.
	 * @param execBlocked
	 *            If this communication is currently blocked (execBlocked =
	 *            true).
	 */
	private void makeEdge(Node sendNode, Node recNode, ASTSend send,
			ASTReceive receive, boolean execBlocked) {
		/*
		 * String edgeName = send.getProcessName() + send.getPid() + "-" +
		 * send.getChannelName() + "->" + receive.getProcessName() +
		 * receive.getPid();
		 */
		String channelName = send.getChannelName();
    String edgeName = sendNode.toString() + "-" + channelName
				+ "->" + recNode.toString();
		Edge edge = graph.findEdgeByName(edgeName);
		if (edge == null) {
			edge = new Edge(graph, sendNode, recNode, edgeName);
			
			String parameters = "";
      for (Object o : send.getParameters())
      {
        // Remove the channel name if one is found
        String messageAndChannelName = (String)o;
        int i = messageAndChannelName.indexOf('#');
        String message = (i == -1) ? messageAndChannelName : messageAndChannelName.substring(0, i);
        
        parameters += message + " ";
      }

      /*
      // String will contain message names and channel names
			String parameters = send.getParameters().toString();
			
			edge.setAttribute(GrappaConstants.LABEL_ATTR, channelName);
      edge.setAttribute(GrappaConstants.TIP_ATTR, "Sending names: " + parameters);
			*/
      
      edge.setAttribute(GrappaConstants.LABEL_ATTR, parameters);
      edge.setAttribute(GrappaConstants.TIP_ATTR, channelName);
		}

		if (!execBlocked) {
			executionNodeLookup.put(edgeName,
					new SimpleNode[] { send, receive });
		} else {
			// don't override this edge if it is already an executable one.
			if (!executionNodeLookup.containsKey(edge.getName())) {
				edge.setAttribute(GrappaConstants.COLOR_ATTR, "gray");
			}
		}

		/*
		 * Do not make an edge between two nodes of the same pool, in case the
		 * pool is closed.
		 * 
		 */
		if (sendNode == recNode) {
			String poolName = (String) sendNode
					.getAttributeValue(GrappaConstants.LABEL_ATTR);
			if (poolVisibleTable.containsKey(poolName)) {
				if (!((Boolean) poolVisibleTable.get(poolName)).booleanValue())
					;
				edge.setAttribute(GrappaConstants.STYLE_ATTR, "invis");
			}
		}

	}

	private void checkNodeScope(Node graphNode, SimpleNode n, Hashtable scope,
			Hashtable resultingScopes) {
		Set entrySet = scope.entrySet();
		Iterator iter = entrySet.iterator();
		while (iter.hasNext()) {
			Map.Entry e = (Map.Entry) iter.next();
			if (e != null) {
				String channel = (String) e.getKey();
				ArrayList pids = (ArrayList) e.getValue();
				if (!(channel == null || channel == "" || pids == null)) {
					if (pids.contains(n.getPid())) {
						if (resultingScopes.containsKey(channel)) {
							ArrayList a = (ArrayList) resultingScopes
									.get(channel);
							a.add(graphNode);
						} else {
							ArrayList a = new ArrayList();
							a.add(graphNode);
							resultingScopes.put(channel, a);
						}
					}
				}
			}
		}
	}

	/**
	 * For each entry in the hashtable make a node with the name of the channel
	 * and edges between this node and all the nodes contained in the
	 * corresponding entry.
	 * 
	 * @param resultingScopes
	 */
	private void makeScopes(Hashtable resultingScopes) {
		Set entrySet = resultingScopes.entrySet();
		Iterator iter = entrySet.iterator();
		while (iter.hasNext()) {
			Map.Entry e = (Map.Entry) iter.next();
			if (e != null) {
				String channel = (String) e.getKey();
				ArrayList nodes = (ArrayList) e.getValue();
				if (!(channel == "" || channel == null || nodes == null)) {
					// make node for the channel
					Node channelNode = new Node(graph, channel);
					channelNode.setAttribute(GrappaConstants.SHAPE_ATTR,
							"ellipse");
					channelNode.setAttribute(GrappaConstants.COLOR_ATTR,
							"white");

					for (int i = 0; i < nodes.size(); i++) {
						Node n = (Node) nodes.get(i);
						if (n != null) {
							String edgeName = channelNode.getName() + "->"
									+ n.getName();
							Edge edge = graph.findEdgeByName(edgeName);
							if (edge == null) {
								edge = new Edge(graph, channelNode, n, edgeName);
								edge.setAttribute(GrappaConstants.STYLE_ATTR,
										"dashed");
								edge.setAttribute("arrowhead", "none");
							}
						}
					}
				}
			}
		}
	}

	/** ****************ActionListener Methoden********************************* */

	public void addActionListener(ActionListener listener) {
		listenerList.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		listenerList.remove(ActionListener.class, listener);
	}

	protected void fireActionPerformed(ActionEvent e) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActionListener.class) {
				((ActionListener) listeners[i + 1]).actionPerformed(e);
			}
		}
	}
}
