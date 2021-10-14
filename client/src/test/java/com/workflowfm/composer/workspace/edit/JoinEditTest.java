package com.workflowfm.composer.workspace.edit;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JFrame;

import com.workflowfm.composer.edit.graph.AddProcessGraphEdit;
import com.workflowfm.composer.exceptions.ComponentExceptionHandler;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.InvalidProvenancePathException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;
import com.workflowfm.composer.processes.CllTermVisitor;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ComposeActionState;
import com.workflowfm.composer.processes.ComposeProvenance;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.CompositionSessionUI;
import com.workflowfm.composer.ui.WorkspaceUI;
import com.workflowfm.composer.workspace.Workspace;
import com.workflowfm.composer.workspace.edit.JoinEdit;


public class JoinEditTest {
	private Workspace workspace;
	private Prover prover;
	private ExceptionHandler exceptionHandler;

	public JoinEditTest()
	{
		prover = new HolLight();
		CompositionSession session = new CompositionSession(prover);
		workspace = session.createWorkspace();
	}
/*
	public void addStuff() {
		int i = 1;
		{	
			String name1 = "P" + i;
			ProcessPort output1 = new ProcessPort(name1 + "co", new CllTerm("X").times(new CllTerm("Y")));
			Vector<ProcessPort> inputs1 = new Vector<ProcessPort>();
			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("W")));
			CProcess p1 = new CProcess(name1,inputs1,output1);
			workspace.addProcess(p1);
			
			String name2 = "Q" + i;
			ProcessPort output2 = new ProcessPort(name2 + "co", new CllTerm("Z"));
			Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
			inputs2.add(new ProcessPort(name2 + "c0", new CllTerm("X")));
			CProcess p2 = new CProcess(name2,inputs2,output2);
			workspace.addProcess(p2);
			
			String namer = "R" + i;
			
			ComposeAction action = new ComposeAction("JOIN",name1,"l",name2,"",namer);
			
			Vector<ComposeAction> actions = new Vector<ComposeAction>();
			actions.add(action);
			
			ProcessPort outputr = new ProcessPort(namer + "co", new CllTerm("Z").times(new CllTerm("Y")));
			Vector<ProcessPort> inputsr = new Vector<ProcessPort>();
			inputsr.add(new ProcessPort(namer + "c0", new CllTerm("W")));
			CProcess pr = new CProcess(namer,inputsr,outputr,true,false,true,true,actions);
			
			Vector<CllTerm> buffered = new Vector<CllTerm>();
			Vector<ProcessPort> connected = new Vector<ProcessPort>();
			buffered.add(new CllTerm("Y"));
			connected.add(new ProcessPort(name2 + "c0", new CllTerm("X").neg()));
			
			ComposeProvenance prov = new ComposeProvenance(name2 + "c0").times(new ComposeProvenance(ComposeProvenance.BUFFER_SOURCE));
			
			ComposeActionState state = new ComposeActionState(namer,p1,p2,buffered,connected,output1.getCllTerm(),prov);

			new JoinEdit(workspace, exceptionHandler, pr, action, state, true).apply();
			i++;
		}
		{	// Buffer trimming
			String name1 = "P" + i;
			ProcessPort output1 = new ProcessPort(name1 + "co", new CllTerm("C").plus(new CllTerm("D")));
			Vector<ProcessPort> inputs1 = new Vector<ProcessPort>();
			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("A")));
			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("B")));
			CProcess p1 = new CProcess(name1,inputs1,output1);
			workspace.addProcess(p1);
			
			String name2 = "Q" + i;
			ProcessPort output2 = new ProcessPort(name2 + "co", new CllTerm("G"));
			Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
			inputs2.add(new ProcessPort(name2 + "c0", new CllTerm("C")));
			inputs2.add(new ProcessPort(name2 + "c1", new CllTerm("E")));
			CProcess p2 = new CProcess(name2,inputs2,output2);
			workspace.addProcess(p2);
			
			String namer = "R" + i;
			
			ComposeAction action = new ComposeAction("JOIN",name1,"l",name2,"",namer);
			
			Vector<ComposeAction> actions = new Vector<ComposeAction>();
			actions.add(action);
			
			ProcessPort outputr = new ProcessPort(namer + "co", new CllTerm("G").plus(new CllTerm("E").times(new CllTerm("D"))));
			Vector<ProcessPort> inputsr = new Vector<ProcessPort>();
			inputsr.add(new ProcessPort(namer + "c0", new CllTerm("A")));
			inputsr.add(new ProcessPort(namer + "c1", new CllTerm("B")));
			inputsr.add(new ProcessPort(namer + "c2", new CllTerm("E")));
			CProcess pr = new CProcess(namer,inputsr,outputr,true,false,true,true,actions);
			
			Vector<CllTerm> buffered = new Vector<CllTerm>();
			Vector<ProcessPort> connected = new Vector<ProcessPort>();
			buffered.add(new CllTerm("D"));
			buffered.add(new CllTerm("E"));
			buffered.add(new CllTerm("E"));
			connected.add(new ProcessPort(name2 + "c0", new CllTerm("C").neg()));
			
			ComposeProvenance prov = new ComposeProvenance(name2 + "c0").plus(new ComposeProvenance(ComposeProvenance.BUFFER_SOURCE));
			
			ComposeActionState state = new ComposeActionState(namer,p1,p2,buffered,connected,output1.getCllTerm(),prov);
						
			new JoinEdit(workspace, exceptionHandler, pr, action, state, true).apply();	
			i++;
		}
		{	// optimised buffer edge routing 1
			String name1 = "P" + i;
			ProcessPort output1 = new ProcessPort(name1 + "co", new CllTerm("C").times(new CllTerm("D")));
			Vector<ProcessPort> inputs1 = new Vector<ProcessPort>();
			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("A")));
			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("B")));
			CProcess p1 = new CProcess(name1,inputs1,output1);
			workspace.addProcess(p1);
			
			String name2 = "Q" + i;
			ProcessPort output2 = new ProcessPort(name2 + "co", new CllTerm("G"));
			Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
			inputs2.add(new ProcessPort(name2 + "c0", new CllTerm("C")));
			inputs2.add(new ProcessPort(name2 + "c1", new CllTerm("E")));
			CProcess p2 = new CProcess(name2,inputs2,output2);
			workspace.addProcess(p2);

			String name3 = "S" + i;
			ProcessPort output3 = new ProcessPort(name3 + "co", new CllTerm("H"));
			Vector<ProcessPort> inputs3 = new Vector<ProcessPort>();
			inputs3.add(new ProcessPort(name3 + "c0", new CllTerm("D")));
			inputs3.add(new ProcessPort(name3 + "c1", new CllTerm("G")));
			CProcess p3 = new CProcess(name3,inputs3,output3);
			workspace.addProcess(p3);
			
			String namer1 = "R" + i;
			String namer2 = "Res" + i;
			
			ComposeAction action1 = new ComposeAction("JOIN",name1,"l",name2,"",namer1);			
			
			Vector<ComposeAction> actions1 = new Vector<ComposeAction>();
			actions1.add(action1);
			
			ProcessPort outputr1 = new ProcessPort(namer1 + "co", new CllTerm("G").times(new CllTerm("D")));
			Vector<ProcessPort> inputsr1 = new Vector<ProcessPort>();
			inputsr1.add(new ProcessPort(namer1 + "c0", new CllTerm("A")));
			inputsr1.add(new ProcessPort(namer1 + "c1", new CllTerm("B")));
			inputsr1.add(new ProcessPort(namer1 + "c2", new CllTerm("E")));
			CProcess pr1 = new CProcess(namer1,inputsr1,outputr1,true,false,true,true,actions1);

			Vector<CllTerm> buffered1 = new Vector<CllTerm>();
			Vector<ProcessPort> connected1 = new Vector<ProcessPort>();			
			buffered1.add(new CllTerm("D"));
			connected1.add(new ProcessPort(name2 + "c0", new CllTerm("C").neg()));
			
			ComposeProvenance prov1 = new ComposeProvenance(name2 + "c0").times(new ComposeProvenance(ComposeProvenance.BUFFER_SOURCE));
			
			ComposeActionState state1 = new ComposeActionState(namer1,p1,p2,buffered1,connected1,p1.getOutputCll(),prov1);

			new JoinEdit(workspace, exceptionHandler, pr1, action1, state1, true).apply();	
			
			pr1.setProvenance(new ComposeProvenance(name2).times(new ComposeProvenance(name1)));
			
			
			ComposeAction action2 = new ComposeAction("JOIN",namer1,"r",name3,"",namer2);
			
			Vector<ComposeAction> actions2 = new Vector<ComposeAction>();
			actions2.add(action2);
			
			ProcessPort outputr2 = new ProcessPort(namer2 + "co", new CllTerm("H"));
			Vector<ProcessPort> inputsr2 = new Vector<ProcessPort>();
			inputsr1.add(new ProcessPort(namer2 + "c0", new CllTerm("A")));
			inputsr1.add(new ProcessPort(namer2 + "c1", new CllTerm("B")));
			inputsr1.add(new ProcessPort(namer2 + "c2", new CllTerm("E")));
			CProcess pr2 = new CProcess(namer2,inputsr2,outputr2,true,false,true,true,actions2);
			
			Vector<CllTerm> buffered2 = new Vector<CllTerm>();
			Vector<ProcessPort> connected2 = new Vector<ProcessPort>();		
			connected2.add(new ProcessPort(name3 + "c1", new CllTerm("G").neg()));
			connected2.add(new ProcessPort(name3 + "c0", new CllTerm("D").neg()));
			
			ComposeProvenance prov2 = new ComposeProvenance(name3 + "c1").times(new ComposeProvenance(name3 + "c0"));
			
			ComposeActionState state2 = new ComposeActionState(namer2,pr1,p3,buffered2,connected2,pr1.getOutputCll(),prov2);
	
			new AddProcessGraphEdit(pr1, workspace.getSession(), exceptionHandler, workspace.getGraph()).apply();
			new JoinEdit(workspace, exceptionHandler, pr2, action2, state2, true).apply();
			i++;
		}
		{	// optimised buffer edge routing 2
			String name1 = "P" + i;
			ProcessPort output1 = new ProcessPort(name1 + "co", new CllTerm("C").times(new CllTerm("D")));
			Vector<ProcessPort> inputs1 = new Vector<ProcessPort>();
			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("A")));
			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("B")));
			CProcess p1 = new CProcess(name1,inputs1,output1);
			workspace.addProcess(p1);
			
			String name2 = "Q" + i;
			ProcessPort output2 = new ProcessPort(name2 + "co", new CllTerm("G"));
			Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
			inputs2.add(new ProcessPort(name2 + "c0", new CllTerm("C")));
			inputs2.add(new ProcessPort(name2 + "c1", new CllTerm("E")));
			CProcess p2 = new CProcess(name2,inputs2,output2);
			workspace.addProcess(p2);

			String name3 = "S" + i;
			ProcessPort output3 = new ProcessPort(name3 + "co", new CllTerm("H"));
			Vector<ProcessPort> inputs3 = new Vector<ProcessPort>();
			inputs3.add(new ProcessPort(name3 + "c1", new CllTerm("G")));
			CProcess p3 = new CProcess(name3,inputs3,output3);
			workspace.addProcess(p3);
			
			String namer1 = "R" + i;
			String namer2 = "Res" + i;
			
			ComposeAction action1 = new ComposeAction("JOIN",name1,"l",name2,"",namer1);			
			
			Vector<ComposeAction> actions1 = new Vector<ComposeAction>();
			actions1.add(action1);
			
			ProcessPort outputr1 = new ProcessPort(namer1 + "co", new CllTerm("G").times(new CllTerm("D")));
			Vector<ProcessPort> inputsr1 = new Vector<ProcessPort>();
			inputsr1.add(new ProcessPort(namer1 + "c0", new CllTerm("A")));
			inputsr1.add(new ProcessPort(namer1 + "c1", new CllTerm("B")));
			inputsr1.add(new ProcessPort(namer1 + "c2", new CllTerm("E")));
			CProcess pr1 = new CProcess(namer1,inputsr1,outputr1,true,false,true,true,actions1);
			
			Vector<CllTerm> buffered1 = new Vector<CllTerm>();
			Vector<ProcessPort> connected1 = new Vector<ProcessPort>();			
			buffered1.add(new CllTerm("D"));
			connected1.add(new ProcessPort(name2 + "c0", new CllTerm("C").neg()));
			
			ComposeProvenance prov1 = new ComposeProvenance(name2 + "c0").times(new ComposeProvenance(ComposeProvenance.BUFFER_SOURCE));
			
			ComposeActionState state1 = new ComposeActionState(namer1,p1,p2,buffered1,connected1,p1.getOutputCll(),prov1);
			
			new JoinEdit(workspace, exceptionHandler, pr1, action1, state1, true).apply();	
			pr1.setProvenance(new ComposeProvenance(name2).times(new ComposeProvenance(name1)));
			
			ComposeAction action2 = new ComposeAction("JOIN",namer1,"r",name3,"",namer2);
			
			Vector<ComposeAction> actions2 = new Vector<ComposeAction>();
			actions2.add(action2);
			
			ProcessPort outputr2 = new ProcessPort(namer2 + "co", new CllTerm("H").times(new CllTerm("D")));
			Vector<ProcessPort> inputsr2 = new Vector<ProcessPort>();
			inputsr1.add(new ProcessPort(namer2 + "c0", new CllTerm("A")));
			inputsr1.add(new ProcessPort(namer2 + "c1", new CllTerm("B")));
			inputsr1.add(new ProcessPort(namer2 + "c2", new CllTerm("E")));
			CProcess pr2 = new CProcess(namer2,inputsr2,outputr2,true,false,true,true,actions2);
			
			Vector<CllTerm> buffered2 = new Vector<CllTerm>();
			Vector<ProcessPort> connected2 = new Vector<ProcessPort>();	
			buffered2.add(new CllTerm("D"));
			connected2.add(new ProcessPort(name3 + "c1", new CllTerm("G").neg()));
			
			ComposeProvenance prov2 = new ComposeProvenance(name3 + "c1").times(new ComposeProvenance(ComposeProvenance.BUFFER_SOURCE));
			
			ComposeActionState state2 = new ComposeActionState(namer2,pr1,p3,buffered2,connected2,pr1.getOutputCll(),prov2);
			
			new JoinEdit(workspace, exceptionHandler, pr2, action2, state2, true).apply();
			i++;
		}
////		{	// composite buffer - THIS NEVER HAPPENS THOUGH!
////			String name1 = "P" + i;
////			ProcessPort output1 = new ProcessPort(name1 + "co", new CllTerm("C").times(new CllTerm("D")).times(new CllTerm("X")));
////			Vector<ProcessPort> inputs1 = new Vector<ProcessPort>();
////			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("A")));
////			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("B")));
////			CProcess p1 = new CProcess(name1,inputs1,output1);
////			workspace.addProcess(p1);
////			
////			String name2 = "Q" + i;
////			ProcessPort output2 = new ProcessPort(name2 + "co", new CllTerm("G"));
////			Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
////			inputs2.add(new ProcessPort(name2 + "c0", new CllTerm("C")));
////			inputs2.add(new ProcessPort(name2 + "c1", new CllTerm("E")));
////			CProcess p2 = new CProcess(name2,inputs2,output2);
////			workspace.addProcess(p2);
////
////			String name3 = "S" + i;
////			ProcessPort output3 = new ProcessPort(name3 + "co", new CllTerm("H"));
////			Vector<ProcessPort> inputs3 = new Vector<ProcessPort>();
////			inputs3.add(new ProcessPort(name3 + "c1", new CllTerm("G")));
////			CProcess p3 = new CProcess(name3,inputs3,output3);
////			workspace.addProcess(p3);
////			
////			String namer1 = "R" + i;
////			String namer2 = "Res" + i;
////			
////			ComposeAction action1 = new ComposeAction("JOIN",name1,"l",name2,"",namer1);			
////			
////			Vector<ComposeAction> actions1 = new Vector<ComposeAction>();
////			actions1.add(action1);
////			
////			ProcessPort outputr1 = new ProcessPort(namer1 + "co", new CllTerm("G").times(new CllTerm("D").times(new CllTerm("X"))));
////			Vector<ProcessPort> inputsr1 = new Vector<ProcessPort>();
////			inputsr1.add(new ProcessPort(namer1 + "c0", new CllTerm("A")));
////			inputsr1.add(new ProcessPort(namer1 + "c1", new CllTerm("B")));
////			inputsr1.add(new ProcessPort(namer1 + "c2", new CllTerm("E")));
////			CProcess pr1 = new CProcess(namer1,inputsr1,outputr1,true,false,true,true,actions1);
////			
////			CllTerm[] buffered1 = { new CllTerm("D").times(new CllTerm("X")) };
////			ProcessPort[] connected1 = { new ProcessPort(name2 + "c0", new CllTerm("C")) };
////			
////			ComposeActionState state1 = new ComposeActionState(4,buffered1,connected1);
////			
////			new JoinEdit(workspace, pr1, action1, state1, true).apply();	
////			
////			i++;
////		}
////		{	// optimised routing for composite buffer - THIS NEVER HAPPENS THOUGH!
////			String name1 = "P" + i;
////			ProcessPort output1 = new ProcessPort(name1 + "co", new CllTerm("C").times(new CllTerm("D")).times(new CllTerm("X")));
////			Vector<ProcessPort> inputs1 = new Vector<ProcessPort>();
////			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("A")));
////			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("B")));
////			CProcess p1 = new CProcess(name1,inputs1,output1);
////			workspace.addProcess(p1);
////			
////			String name2 = "Q" + i;
////			ProcessPort output2 = new ProcessPort(name2 + "co", new CllTerm("G"));
////			Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
////			inputs2.add(new ProcessPort(name2 + "c0", new CllTerm("C")));
////			inputs2.add(new ProcessPort(name2 + "c1", new CllTerm("E")));
////			CProcess p2 = new CProcess(name2,inputs2,output2);
////			workspace.addProcess(p2);
////
////			String name3 = "S" + i;
////			ProcessPort output3 = new ProcessPort(name3 + "co", new CllTerm("H"));
////			Vector<ProcessPort> inputs3 = new Vector<ProcessPort>();
////			inputs3.add(new ProcessPort(name3 + "c1", new CllTerm("G")));
////			CProcess p3 = new CProcess(name3,inputs3,output3);
////			workspace.addProcess(p3);
////			
////			String namer1 = "R" + i;
////			String namer2 = "Res" + i;
////			
////			ComposeAction action1 = new ComposeAction("JOIN",name1,"l",name2,"",namer1);			
////			
////			Vector<ComposeAction> actions1 = new Vector<ComposeAction>();
////			actions1.add(action1);
////			
////			ProcessPort outputr1 = new ProcessPort(namer1 + "co", new CllTerm("G").times(new CllTerm("D")).times(new CllTerm("X")));
////			Vector<ProcessPort> inputsr1 = new Vector<ProcessPort>();
////			inputsr1.add(new ProcessPort(namer1 + "c0", new CllTerm("A")));
////			inputsr1.add(new ProcessPort(namer1 + "c1", new CllTerm("B")));
////			inputsr1.add(new ProcessPort(namer1 + "c2", new CllTerm("E")));
////			CProcess pr1 = new CProcess(namer1,inputsr1,outputr1,true,false,true,true,actions1);
////			
////			CllTerm[] buffered1 = { new CllTerm("D").times(new CllTerm("X")) };
////			ProcessPort[] connected1 = { new ProcessPort(name2 + "c0", new CllTerm("C")) };
////			
////			ComposeActionState state1 = new ComposeActionState(4,buffered1,connected1);
////			
////			new JoinEdit(workspace, pr1, action1, state1, true).apply();	
////			
////			
////			ComposeAction action2 = new ComposeAction("JOIN",namer1,"r",name3,"",namer2);
////			
////			Vector<ComposeAction> actions2 = new Vector<ComposeAction>();
////			actions2.add(action2);
////			
////			ProcessPort outputr2 = new ProcessPort(namer2 + "co", new CllTerm("H").times(new CllTerm("D")).times(new CllTerm("X")));
////			Vector<ProcessPort> inputsr2 = new Vector<ProcessPort>();
////			inputsr1.add(new ProcessPort(namer2 + "c0", new CllTerm("A")));
////			inputsr1.add(new ProcessPort(namer2 + "c1", new CllTerm("B")));
////			inputsr1.add(new ProcessPort(namer2 + "c2", new CllTerm("E")));
////			CProcess pr2 = new CProcess(namer2,inputsr2,outputr2,true,false,true,true,actions2);
////			
////			CllTerm[] buffered2 = { new CllTerm("D").times(new CllTerm("X")) };
////			ProcessPort[] connected2 = { new ProcessPort(name3 + "c1", new CllTerm("G")) };
////			
////			ComposeActionState state2 = new ComposeActionState(8,buffered2,connected2);
////			
////			new JoinEdit(workspace, pr2, action2, state2, true).apply();
////			i++;
////		}
//		{	// composite conditional buffering
//			String name1 = "P" + i;
//			ProcessPort output1 = new ProcessPort(name1 + "co", new CllTerm("C").plus(new CllTerm("D")));
//			Vector<ProcessPort> inputs1 = new Vector<ProcessPort>();
//			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("A")));
//			inputs1.add(new ProcessPort(name1 + "c1", new CllTerm("B")));
//			CProcess p1 = new CProcess(name1,inputs1,output1);
//			workspace.addProcess(p1);
//			
//			String name2 = "Q" + i;
//			ProcessPort output2 = new ProcessPort(name2 + "co", new CllTerm("G"));
//			Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
//			inputs2.add(new ProcessPort(name2 + "c0", new CllTerm("C")));
//			inputs2.add(new ProcessPort(name2 + "c1", new CllTerm("E")));
//			inputs2.add(new ProcessPort(name2 + "c2", new CllTerm("F")));
//			CProcess p2 = new CProcess(name2,inputs2,output2);
//			workspace.addProcess(p2);
//
//			String name3 = "S" + i;
//			ProcessPort output3 = new ProcessPort(name3 + "co", new CllTerm("E"));
//			Vector<ProcessPort> inputs3 = new Vector<ProcessPort>();
//			inputs3.add(new ProcessPort(name3 + "c0", new CllTerm("J")));
//			inputs3.add(new ProcessPort(name3 + "c1", new CllTerm("K")));
//			CProcess p3 = new CProcess(name3,inputs3,output3);
//			workspace.addProcess(p3);
//			
//			String namer1 = "R" + i;
//			String namer2 = "Res" + i;
//			
//			ComposeAction action1 = new ComposeAction("JOIN",name3,"l",name2,"",namer1);			
//			
//			Vector<ComposeAction> actions1 = new Vector<ComposeAction>();
//			actions1.add(action1);
//			
//			ProcessPort outputr1 = new ProcessPort(namer1 + "co", new CllTerm("G").times(new CllTerm("L")));
//			Vector<ProcessPort> inputsr1 = new Vector<ProcessPort>();
//			inputsr1.add(new ProcessPort(namer1 + "c0", new CllTerm("A")));
//			inputsr1.add(new ProcessPort(namer1 + "c1", new CllTerm("J")));
//			inputsr1.add(new ProcessPort(namer1 + "c2", new CllTerm("K")));
//			inputsr1.add(new ProcessPort(namer1 + "c3", new CllTerm("F")));
//			CProcess pr1 = new CProcess(namer1,inputsr1,outputr1,true,false,true,true,actions1);
//			
//			CllTerm[] buffered1 = { new CllTerm("L") };
//			ProcessPort[] connected1 = { new ProcessPort(name2 + "c1", new CllTerm("E")) };
//			
//			ComposeActionState state1 = new ComposeActionState(4,buffered1,connected1);
//			
//			new JoinEdit(workspace, exceptionHandler, pr1, action1, state1, true).apply();	
//			
//			
//			ComposeAction action2 = new ComposeAction("JOIN",name1,"l",namer1,"",namer2);
//			
//			Vector<ComposeAction> actions2 = new Vector<ComposeAction>();
//			actions2.add(action2);
//			
//			ProcessPort outputr2 = new ProcessPort(namer2 + "co", (new CllTerm("G").times(new CllTerm("L"))).plus(new CllTerm("J").times(new CllTerm("K")).times(new CllTerm("F")).times(new CllTerm("D"))));
//			Vector<ProcessPort> inputsr2 = new Vector<ProcessPort>();
//			inputsr1.add(new ProcessPort(namer2 + "c0", new CllTerm("A")));
//			inputsr1.add(new ProcessPort(namer2 + "c1", new CllTerm("B")));
//			inputsr1.add(new ProcessPort(namer2 + "c2", new CllTerm("E")));
//			CProcess pr2 = new CProcess(namer2,inputsr2,outputr2,true,false,true,true,actions2);
//			
//			CllTerm[] buffered2 = { new CllTerm("D"), new CllTerm("F"), new CllTerm("K"), new CllTerm("J") };
//			ProcessPort[] connected2 = { new ProcessPort(name2 + "c0", new CllTerm("C")) };
//			
//			ComposeActionState state2 = new ComposeActionState(13,buffered2,connected2);
//			
//			new JoinEdit(workspace, exceptionHandler, pr2, action2, state2, true).apply();
//			i++;
//		}
		{	// handling both branches of an optional
			String name1 = "P" + i;
			ProcessPort output1 = new ProcessPort(name1 + "co", new CllTerm("C").plus(new CllTerm("D")));
			Vector<ProcessPort> inputs1 = new Vector<ProcessPort>();
			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("A")));
			CProcess p1 = new CProcess(name1,inputs1,output1);
			workspace.addProcess(p1);
			
			String name2 = "Q" + i;
			ProcessPort output2 = new ProcessPort(name2 + "co", new CllTerm("G"));
			Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
			inputs2.add(new ProcessPort(name2 + "c0", new CllTerm("C")));
			CProcess p2 = new CProcess(name2,inputs2,output2);
			workspace.addProcess(p2);

			String name3 = "S" + i;
			ProcessPort output3 = new ProcessPort(name3 + "co", new CllTerm("H"));
			Vector<ProcessPort> inputs3 = new Vector<ProcessPort>();
			inputs3.add(new ProcessPort(name3 + "c0", new CllTerm("D")));
			CProcess p3 = new CProcess(name3,inputs3,output3);
			workspace.addProcess(p3);
			
			String namer1 = "R" + i;
			String namer2 = "Res" + i;
			
			ComposeAction action1 = new ComposeAction("JOIN",name1,"l",name2,"",namer1);			
			
			Vector<ComposeAction> actions1 = new Vector<ComposeAction>();
			actions1.add(action1);
			
			ProcessPort outputr1 = new ProcessPort(namer1 + "co", new CllTerm("G").plus(new CllTerm("D")));
			Vector<ProcessPort> inputsr1 = new Vector<ProcessPort>();
			inputsr1.add(new ProcessPort(namer1 + "c0", new CllTerm("A")));
			CProcess pr1 = new CProcess(namer1,inputsr1,outputr1,true,false,true,true,actions1);
			
			Vector<CllTerm> buffered1 = new Vector<CllTerm>();
			Vector<ProcessPort> connected1 = new Vector<ProcessPort>();			
			buffered1.add(new CllTerm("D"));
			connected1.add(new ProcessPort(name2 + "c0", new CllTerm("C").neg()));
			
			ComposeProvenance prov1 = new ComposeProvenance(name2 + "c0").plus(new ComposeProvenance(ComposeProvenance.BUFFER_SOURCE));
			
			ComposeActionState state1 = new ComposeActionState(namer1,p1,p2,buffered1,connected1,p1.getOutputCll(),prov1);
			
			new JoinEdit(workspace, exceptionHandler, pr1, action1, state1, true).apply();	
			pr1.setProvenance(new ComposeProvenance(name2).times(new ComposeProvenance(name1)));
			
			
			ComposeAction action2 = new ComposeAction("JOIN",namer1,"r",name3,"",namer2);
			
			Vector<ComposeAction> actions2 = new Vector<ComposeAction>();
			actions2.add(action2);
			
			ProcessPort outputr2 = new ProcessPort(namer2 + "co", new CllTerm("G").plus(new CllTerm("H")));
			Vector<ProcessPort> inputsr2 = new Vector<ProcessPort>();
			inputsr1.add(new ProcessPort(namer2 + "c0", new CllTerm("A")));
			CProcess pr2 = new CProcess(namer2,inputsr2,outputr2,true,false,true,true,actions2);
		
			Vector<CllTerm> buffered2 = new Vector<CllTerm>();
			Vector<ProcessPort> connected2 = new Vector<ProcessPort>();			
			buffered2.add(new CllTerm("G"));
			connected2.add(new ProcessPort(name3 + "c0", new CllTerm("D").neg()));
			
			ComposeProvenance prov2 = new ComposeProvenance(ComposeProvenance.BUFFER_SOURCE).plus(new ComposeProvenance(name3 + "c0"));	
			ComposeActionState state2 = new ComposeActionState(namer2,pr1,p3,buffered2,connected2,pr1.getOutputCll(),prov2);
			
			new AddProcessGraphEdit(pr1, workspace.getSession(), exceptionHandler, workspace.getGraph()).apply();
			new JoinEdit(workspace, exceptionHandler, pr2, action2, state2, true).apply();
			pr2.setProvenance(new ComposeProvenance(name2).times(new ComposeProvenance(name3)));
			
			
			String name4 = "T" + i;
			ProcessPort output4 = new ProcessPort(name4 + "co", new CllTerm("R"));
			Vector<ProcessPort> inputs4 = new Vector<ProcessPort>();
			inputs4.add(new ProcessPort(name4 + "c0", new CllTerm("G").plus(new CllTerm("H"))));
			CProcess p4 = new CProcess(name4,inputs4,output4);
			workspace.addProcess(p4);
			
			String namer3 = "Rez" + i;
			ComposeAction action3 = new ComposeAction("JOIN",namer2,"r",name4,"",namer3);
			
			Vector<ComposeAction> actions3 = new Vector<ComposeAction>();
			actions3.add(action3);
			
			ProcessPort outputr3 = new ProcessPort(namer3 + "co", new CllTerm("R"));
			Vector<ProcessPort> inputsr3 = new Vector<ProcessPort>();
			inputsr3.add(new ProcessPort(namer3 + "c0", new CllTerm("A")));
			CProcess pr3 = new CProcess(namer3,inputsr3,outputr3,true,false,true,true,actions3);
		
			Vector<CllTerm> buffered3 = new Vector<CllTerm>();
			Vector<ProcessPort> connected3 = new Vector<ProcessPort>();			
			connected3.add(new ProcessPort(name4 + "c0", new CllTerm("G").plus(new CllTerm("H")).neg()));
			
			ComposeProvenance prov3 = new ComposeProvenance(name4 + "c0").plus(new ComposeProvenance(name4 + "c0"));	
			ComposeActionState state3 = new ComposeActionState(namer3,pr2,p4,buffered3,connected3,pr2.getOutputCll(),prov3);
			
			new AddProcessGraphEdit(pr2, workspace.getSession(), exceptionHandler, workspace.getGraph()).apply();
			new JoinEdit(workspace, exceptionHandler, pr3, action3, state3, true).apply();
			//pr2.setProvenance(new ComposeProvenance(name2).times(new ComposeProvenance(name3)));
			i++;
		}
//		{	// compound optional
//			String name1 = "P" + i;
//			ProcessPort output1 = new ProcessPort(name1 + "co", (new CllTerm("A").times(new CllTerm("B"))).plus(new CllTerm("C").times(new CllTerm("D"))));
//			Vector<ProcessPort> inputs1 = new Vector<ProcessPort>();
//			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("W")));
//			CProcess p1 = new CProcess(name1,inputs1,output1);
//			workspace.addProcess(p1);
//			
//			String name2 = "Q" + i;
//			ProcessPort output2 = new ProcessPort(name2 + "co", new CllTerm("Z"));
//			Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
//			inputs2.add(new ProcessPort(name2 + "c0", new CllTerm("A")));
//			inputs2.add(new ProcessPort(name2 + "c1", new CllTerm("B")));
//			inputs2.add(new ProcessPort(name2 + "c1", new CllTerm("C")));
//			CProcess p2 = new CProcess(name2,inputs2,output2);
//			workspace.addProcess(p2);
//			
//			String namer = "R" + i;
//			
//			ComposeAction action = new ComposeAction("JOIN",name1,"l",name2,"",namer);
//			
//			Vector<ComposeAction> actions = new Vector<ComposeAction>();
//			actions.add(action);
//			
//			ProcessPort outputr = new ProcessPort(namer + "co", new CllTerm("Z").plus(new CllTerm("C").times(new CllTerm("C")).times(new CllTerm("D"))));
//			Vector<ProcessPort> inputsr = new Vector<ProcessPort>();
//			inputsr.add(new ProcessPort(namer + "c0", new CllTerm("W")));
//			CProcess pr = new CProcess(namer,inputsr,outputr,true,false,true,true,actions);
//			
//			CllTerm[] buffered = { new CllTerm("D"), new CllTerm("C"), new CllTerm("C") };
//			ProcessPort[] connected = { new ProcessPort(name2 + "c0", new CllTerm("A")), new ProcessPort(name2 + "c1", new CllTerm("B")) };
//			
//			ComposeActionState state = new ComposeActionState(i,buffered,connected);
//			
//			
//			new JoinEdit(workspace, exceptionHandler, pr, action, state, true).apply();		
//			i++;
//		}
////		{	
////			String name1 = "ReviewPatient";
////			ProcessPort output1 = new ProcessPort(name1 + "co", new CllTerm("Patient_fit_for_procedure").plus(new CllTerm("Patient_UNFIT_for_procedure")));
////			Vector<ProcessPort> inputs1 = new Vector<ProcessPort>();
////			inputs1.add(new ProcessPort(name1 + "c0", new CllTerm("Referral")));
////			CProcess p1 = new CProcess(name1,inputs1,output1);
////			workspace.addProcess(p1);
////			
////			String name2 = "InformTheatre";
////			ProcessPort output2 = new ProcessPort(name2 + "co", new CllTerm("Z"));
////			Vector<ProcessPort> inputs2 = new Vector<ProcessPort>();
////			inputs2.add(new ProcessPort(name2 + "c0", new CllTerm("X")));
////			CProcess p2 = new CProcess(name2,inputs2,output2);
////			workspace.addProcess(p2);
////			
////			String namer = "R" + i;
////			
////			ComposeAction action = new ComposeAction("JOIN",name1,"l",name2,"",namer);
////			
////			Vector<ComposeAction> actions = new Vector<ComposeAction>();
////			actions.add(action);
////			
////			ProcessPort outputr = new ProcessPort(namer + "co", new CllTerm("Z").times(new CllTerm("Y")));
////			Vector<ProcessPort> inputsr = new Vector<ProcessPort>();
////			inputsr.add(new ProcessPort(namer + "c0", new CllTerm("W")));
////			CProcess pr = new CProcess(namer,inputsr,outputr,true,false,true,true,actions);
////			
////			CllTerm[] buffered = { new CllTerm("Y") };
////			ProcessPort[] connected = { new ProcessPort(name2 + "c0", new CllTerm("X")) };
////			
////			ComposeActionState state = new ComposeActionState(i,buffered,connected);
////			
////			
////			new JoinEdit(workspace, pr, action, state, true).apply();		
////			i++;
////		}
	}
	
	public void analyseConnection() {
		Vector<ComposeProvenance> iprovs = new Vector<ComposeProvenance>();
		iprovs.add(new ComposeProvenance("#").times(new ComposeProvenance("#").times(new ComposeProvenance("#"))));
		iprovs.add(new ComposeProvenance("S4").plus(new ComposeProvenance("S4")));
		final ComposeProvenance iprov = new ComposeProvenance("times","",iprovs);
		Vector<ComposeProvenance> oprovs = new Vector<ComposeProvenance>();
		oprovs.add(new ComposeProvenance("P3").times(new ComposeProvenance("P2").times(new ComposeProvenance("P4"))));
		oprovs.add(new ComposeProvenance("P1").plus(new ComposeProvenance("P1")));
		final ComposeProvenance oprov = new ComposeProvenance("times","",oprovs);
		Vector<CllTerm> terms = new Vector<CllTerm>();
		terms.add(new CllTerm("T").times(new CllTerm("Z").times(new CllTerm("XX"))));
		terms.add(new CllTerm("X").plus(new CllTerm("Y")));
		final CllTerm output = new CllTerm("times","",terms);
		
		CllTermVisitor visitor = new CllTermVisitor() {	
			@Override
			public boolean visit(CllTerm term, CllTermPath path) {
				try {
					System.out.println("Visit: [" + prover.cllResourceString(path.follow(output)) + "] - [" + path + "]");
				} catch (InvalidCllPathException e1) {
					e1.printStackTrace();
				}
//				try {
				Optional<String> source = path.follow(oprov).getSingleSource();
				if (!source.isPresent()) {
					System.out.println("Failed Sources: " + path.follow(oprov).getSources());
					return false;
				}
				else {
					Optional<String> target = path.follow(iprov).getSingleSource();
					if (!target.isPresent()) {
						System.out.println("Failed Targets: " + path.follow(iprov).getSources());
						return false;
					}
					else {
						try {
							System.out.println(" *** Connection: [" + prover.cllResourceString(path.follow(output)) + "] Buffered: [" + target.get().equals(ComposeProvenance.BUFFER_SOURCE) + "] Source: [" + source.get() + "].");
						} catch (InvalidCllPathException e) {
							e.printStackTrace();
						}
						return true;
					}
				}
//				} catch (InvalidProvenancePathException e) {
//					return false;
//				}
			}
		};
		
		output.bfsAll(visitor, true);
	}
	
	public void setup(String name)
	{
		System.err.println("Setting up GUI.");

		final JFrame frame = new JFrame();
		frame.setTitle(name);
		
		CompositionSessionUI sUI = new CompositionSessionUI(workspace.getSession());
		sUI.setup();
		WorkspaceUI wUI = new WorkspaceUI(workspace,sUI);
		wUI.show();
		wUI.getPanel().setPreferredSize(new Dimension(800, 800));
		exceptionHandler = new ComponentExceptionHandler(wUI.getPanel());
				
		frame.getContentPane().add(wUI.getPanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String args[])
	{
		JoinEditTest gui2 = new JoinEditTest();
		gui2.setup("New");
		gui2.addStuff();
	}*/
}
