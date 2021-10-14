package com.workflowfm.composer.workspace;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.workspace.Workspace;


public class WorkspaceTest {
	
	private Workspace workspace;
	
	@Before
	public void setUp() {
		this.workspace = new CompositionSession(new HolLight()).createWorkspace();
	}
	
	public void case1() {
		CProcess pa = new CProcess("Pa",null,null,false,false,true,true,new Vector<ComposeAction>());
		CProcess pb = new CProcess("Pb",null,null,false,false,true,true,new Vector<ComposeAction>());
		CProcess pc = new CProcess("Pc",null,null,false,false,true,true,new Vector<ComposeAction>());
		CProcess pd = new CProcess("Pd",null,null,false,false,true,true,new Vector<ComposeAction>());
		CProcess pe = new CProcess("Pe",null,null,false,false,true,true,new Vector<ComposeAction>());
		workspace.addProcess(pa);
		workspace.addProcess(pb);
		workspace.addProcess(pc);
		workspace.addProcess(pd);
		workspace.addProcess(pe);
		
		ComposeAction qact = new ComposeAction("JOIN", "Pa", "", "Pb", "", "Q");
		
		Vector<ComposeAction> qacts = new Vector<ComposeAction>();
		qacts.add(qact);
		CProcess q = new CProcess("Q",null,null,false,false,true,true,qacts);
		workspace.addProcess(q);
		
		ComposeAction s1act = new ComposeAction("JOIN", "Q", "", "Pc", "", "Step1");
		
		Vector<ComposeAction> s1acts = new Vector<ComposeAction>();
		s1acts.add(s1act);
		CProcess s1 = new CProcess("Step1",null,null,true,false,true,true,s1acts);
		workspace.addProcess(s1);
		
		ComposeAction s2act1 = new ComposeAction("JOIN", "Step1", "", "Pd", "", "Step2a");
		ComposeAction s2act2 = new ComposeAction("JOIN", "Step2a", "", "Step1", "", "Step2");
		
		Vector<ComposeAction> s2acts = new Vector<ComposeAction>();
		s2acts.add(s2act1);
		s2acts.add(s2act2);
		CProcess s2 = new CProcess("Step2",null,null,true,false,true,true,s2acts);
		workspace.addProcess(s2);
		
		ComposeAction s3act = new ComposeAction("JOIN", "Pe", "", "Step2", "", "Step3");

		Vector<ComposeAction> s3acts = new Vector<ComposeAction>();
		s3acts.add(s3act);
		CProcess s3 = new CProcess("Step3",null,null,true,false,true,true,s3acts);
		workspace.addProcess(s3);
		
		ComposeAction ract = new ComposeAction("JOIN", "Pe", "", "Step2", "", "R");
		
		Vector<ComposeAction> racts = new Vector<ComposeAction>();
		racts.add(s1act);
		racts.add(s2act1);
		racts.add(s2act2);
		racts.add(ract);
		CProcess r = new CProcess("R",null,null,true,false,true,true,racts);
		workspace.addProcess(r);
	}
	
	@Test
	public void testRootDependencies1() throws NotFoundException {
		case1();
		
		System.out.println("R root dependencies:");
		Collection<String> res = ComposeAction.getRootDependencies(workspace.getComposition("R").getActions());
		for (String proc : res)
			System.out.println(proc);
		
		assertEquals(4,res.size());
	}
	
	@Test
	public void testGetComponents1() throws NotFoundException {
		case1();
		
		System.out.println("Step3 components:");
		Collection<CProcess> res = workspace.getComponents("Step3");
		for (CProcess proc : res)
			System.out.println(proc.debugString());
		
		assertEquals(2,res.size());
	}
	
	@Test
	public void testGetComponents2() throws NotFoundException {
		case1();
		
		System.out.println("R components:");
		Collection<CProcess> res = workspace.getComponents("R");
		for (CProcess proc : res)
			System.out.println(proc.debugString());
		
		assertEquals(4,res.size());
	}
	
	@Test
	public void testGetAllIntermediateComponents() throws NotFoundException {
		case1();
		
		System.out.println("Step3 intermediate components:");
		Collection<CProcess> res = workspace.getAllIntermediateComponents("Step3");
		for (CProcess proc : res)
			System.out.println(proc.debugString());
		
		assertEquals(2,res.size());
	}
	
	@Test
	public void testGetActionsForProcess() throws NotFoundException {
		case1();
	
		System.out.println("Step3 actions for process:");
		Collection<ComposeAction> res = workspace.getActionsForProcess(workspace.getComposition("Step3"), "Result");
		for (ComposeAction action : res)
			System.out.println(action.debugString());
		
		assertEquals(4,res.size());
	}
}
