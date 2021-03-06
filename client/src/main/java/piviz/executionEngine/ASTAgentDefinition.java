/* Generated By:JJTree: Do not edit this line. ASTAgentDefinition.java */

package piviz.executionEngine;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Tree node representing the definition of an agent. It holds the name of the
 * agent and its parameters.
 * 
 * @author Anja
 * 
 */
public class ASTAgentDefinition extends SimpleNode {
	private String agentName;

	private ArrayList parameters;
	
	private boolean executeAgent;

	public ASTAgentDefinition(int id) {
		super(id);
		parameters = new ArrayList();
		executeAgent = false;
	}

	public ASTAgentDefinition(PiParser p, int id) {
		super(p, id);
		parameters = new ArrayList();
		executeAgent = false;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public ArrayList getParameters() {
		return parameters;
	}
	
	public void addParameter(String param){
		parameters.add(param);
	}
	
	/**
	 * Dump this tree node in dot format. A dot node 'AgentDefinition' is created with
	 * the attributes agentName and its parameters representing all of its free names.
	 */
	public int dumpDot(String prefix, int nodeCount, FileWriter fw) {
		//System.out.println(toString(prefix)+ nodeCount + ";");
		try {
			// "parentNodeName -> nodeName_Id;"
			fw.write(toString(prefix)+ "_" + nodeCount + ";\n");
					// nodeName_Id [label = "*ch: channelName, *params: [a,b,c]"];
			fw.write( toString() + "_" + nodeCount 
					+ " [label = " + "\"" + toString()
					+ "\\n*name: " + getAgentName()
					+ "\\n*params: " + getParameters().toString() 
					+ "\\n*exec: " + isExecuteAgent() + "\"];\n "
				);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int newCount = nodeCount;
		
		if (children != null) {
	    	for (int i = 0; i < children.length; i++) {
			SimpleNode n = (SimpleNode)children[i];
			if (n != null) {
				newCount = n.dumpDot(toString() + "_" + nodeCount + " -> ", newCount+1, fw);
			}
	      }
	    }
	    return newCount;
	  }

	public boolean isExecuteAgent() {
		return executeAgent;
	}

	public void setExecuteAgent(boolean executeAgent) {
		this.executeAgent = executeAgent;
	}

}
