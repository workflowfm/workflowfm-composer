package com.workflowfm.composer.prover.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Vector;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.LogExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ComposeAction;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.command.Compose1Command;
import com.workflowfm.composer.prover.response.ComposeResponse;
import com.workflowfm.composer.prover.response.ProverResponse;

public class Compose1CommandTest {
	private static HolLight hol;
	private static ExceptionHandler handler;
	
	@BeforeClass
	public static void setUp() throws Exception {
		handler = new LogExceptionHandler();
		hol = new HolLight();
		hol.start();
	}

	@AfterClass
	public static void close() {
		hol.stop();
	}
	
	@Test
	public void testResponseSize() {
		Vector<ProcessPort> inputsp = new Vector<ProcessPort>();
		inputsp.add(new ProcessPort("ip", new CllTerm("X")));
		ProcessPort outputp = new ProcessPort("op",new CllTerm("Y"));
		CProcess p = new CProcess("P", inputsp, outputp);
		p.setProc("P");
		
		Vector<ProcessPort> inputsq = new Vector<ProcessPort>();
		inputsq.add(new ProcessPort("iq", new CllTerm("Y")));
		ProcessPort outputq = new ProcessPort("oq",new CllTerm("Z"));
		CProcess q = new CProcess("Q", inputsq, outputq);
		q.setProc("Q");
		
		ComposeAction action = new ComposeAction("JOIN", "P", "", "Q", "", "R");
		Compose1Command command = new Compose1Command(action, p, q, "R", handler);
		
		hol.execute(command);
		if (command.succeeded()) {
			assertEquals(1,command.getResponses().size());
		} else {
			fail("Command failed.");
		}
	}
	
	@Test
	public void testProcess1() {
		Vector<ProcessPort> inputsp = new Vector<ProcessPort>();
		inputsp.add(new ProcessPort("ip", new CllTerm("X")));
		ProcessPort outputp = new ProcessPort("op",new CllTerm("Y"));
		CProcess p = new CProcess("P", inputsp, outputp);
		p.setProc("P(ip,op) = Zero");
		
		Vector<ProcessPort> inputsq = new Vector<ProcessPort>();
		inputsq.add(new ProcessPort("iq", new CllTerm("Y")));
		ProcessPort outputq = new ProcessPort("oq",new CllTerm("Z"));
		CProcess q = new CProcess("Q", inputsq, outputq);
		q.setProc("Q(iq,oq) = Zero");
		
		ComposeAction action = new ComposeAction("JOIN", "P", "", "Q", "", "R");
		Compose1Command command = new Compose1Command(action, p, q, "R", handler);
		
		hol.execute(command);
		if (command.succeeded()) {
			
			for (ProverResponse r : command.getResponses()) {
				System.out.println(r.debugString());
				if (r instanceof ComposeResponse) {
					CProcess process = ((ComposeResponse)r).getProcess();
					assertEquals(1, process.getInputCll().size());
					assertEquals(new CllTerm("X"), process.getInputCll().iterator().next());
					assertEquals(new CllTerm("Z"), process.getOutputCll());
					assertEquals("R", process.getName());
					assertEquals(false, process.isAtomic());
					assertEquals(1, process.getActions().size());
					assertEquals(false, process.isCopier());
					assertEquals(true, process.isValid());
					assertEquals(true, process.isChecked());
					assertEquals(true, process.isIntermediate());
					assertEquals(action,process.getActions().elementAt(0));
					assertEquals(action,((ComposeResponse)r).getAction());
				}
				else {
					fail("Unexpected response received!");
				}
			}
			
		} else {
			fail("Command was not executed even though it was sent.");
		}
	}
//	
//	@Test
//	public void testProcess2() {
//		String name = "P";
//		Vector<CllTerm> inputs = new Vector<CllTerm>();
//		inputs.add(new CllTerm("X"));
//		inputs.add(new CllTerm("Y").times(new CllTerm("Z")));
//		CllTerm output = new CllTerm("Y").plus(new CllTerm("W").times(new CllTerm("R")));
//		
//		CreateProcessCommand command = new CreateProcessCommand(name,inputs,output);
//		
//		hol.sendCommand(command);
//		if (command.isExecuted()) {
//			
//			for (ProverResponse r : command.getResponses()) {
//				System.out.println(r.debugString());
//				if (r instanceof CreateProcessResponse) {
//					CProcess process = ((CreateProcessResponse)r).getProcess();
//					assertEquals(inputs, process.getInputCll());
//					assertEquals(output, process.getOutputCll());
//					assertEquals(name, process.getName());
//					assertEquals(true, process.isAtomic());
//					assertEquals(0, process.getActions().size());
//					assertEquals(false, process.isCopier());
//					assertEquals(true, process.isValid());
//					assertEquals(true, process.isChecked());
//				}
//				else {
//					fail("Unexpected response received!");
//				}
//			}
//			
//		} else {
//			fail("Command was not executed even though it was sent.");
//		}
//	}
//	
//	@Test
//	public void testProcess3() {
//		String name = "P";
//		Vector<ProcessPort> inputs = new Vector<ProcessPort>();
//		inputs.add(new ProcessPort("c0",new CllTerm("X")));
//		inputs.add(new ProcessPort("c1",new CllTerm("Y").times(new CllTerm("Z"))));
//		ProcessPort output = new ProcessPort("co",new CllTerm("Y").plus(new CllTerm("W").times(new CllTerm("R"))));
//		CProcess p = new CProcess(name,inputs,output);
//		
//		CreateProcessCommand command = new CreateProcessCommand(p);
//		
//		hol.sendCommand(command);
//		if (command.isExecuted()) {
//			
//			for (ProverResponse r : command.getResponses()) {
//				System.out.println(r.debugString());
//				if (r instanceof CreateProcessResponse) {
//					CProcess process = ((CreateProcessResponse)r).getProcess();
//					assertEquals(p.getInputCll(), process.getInputCll());
//					assertEquals(p.getOutputCll(), process.getOutputCll());
//					assertEquals(name, process.getName());
//					assertEquals(true, process.isAtomic());
//					assertEquals(0, process.getActions().size());
//					assertEquals(false, process.isCopier());
//					assertEquals(true, process.isValid());
//					assertEquals(true, process.isChecked());
//				}
//				else {
//					fail("Unexpected response received!");
//				}
//			}
//			
//		} else {
//			fail("Command was not executed even though it was sent.");
//		}
//	}
//	
//	@Test
//	public void testCopierProcess() {
//		String name = "P";
//		Vector<ProcessPort> inputs = new Vector<ProcessPort>();
//		inputs.add(new ProcessPort("c0",new CllTerm("X")));
//		ProcessPort output = new ProcessPort("co",new CllTerm("X").times(new CllTerm("X")).times(new CllTerm("X")));
//		CProcess p = new CProcess(name,inputs,output);
//		
//		CreateProcessCommand command = new CreateProcessCommand(p);
//		
//		hol.sendCommand(command);
//		if (command.isExecuted()) {
//			
//			for (ProverResponse r : command.getResponses()) {
//				System.out.println(r.debugString());
//				if (r instanceof CreateProcessResponse) {
//					CProcess process = ((CreateProcessResponse)r).getProcess();
//					assertEquals(p.getInputCll(), process.getInputCll());
//					assertEquals(p.getOutputCll(), process.getOutputCll());
//					assertEquals(name, process.getName());
//					assertEquals(true, process.isAtomic());
//					assertEquals(0, process.getActions().size());
//					assertEquals(true, process.isCopier());
//					assertEquals(true, process.isValid());
//					assertEquals(true, process.isChecked());
//				}
//				else {
//					fail("Unexpected response received!");
//				}
//			}
//			
//		} else {
//			fail("Command was not executed even though it was sent.");
//		}
//	}
//	
//	@Test
//	public void testError() {
//		String name = "P";
//		Vector<CllTerm> inputs = new Vector<CllTerm>();
//		inputs.add(new CllTerm("X"));
//		CllTerm output = new CllTerm("-","",new Vector<CllTerm>());
//		
//		CreateProcessCommand command = new CreateProcessCommand(name,inputs,output);
//		
//		hol.sendCommand(command);
//		if (command.isExecuted()) {
//			
//			for (ProverResponse r : command.getResponses()) {
//				System.out.println(r.debugString());
//				if (r instanceof CommandFailedResponse) { 
//					// JSON reply:  {   "response": "CommandFailed",   "content": "Cll_json.to_linprop: Unexpected operator - -" } 
//					assertEquals("Cll_json.to_linprop: Unexpected operator - -",((CommandFailedResponse)r).getContent()); 
//				}
//				else {
//					fail("Unexpected response received!");
//				}
//			}
//			
//		} else {
//			fail("Command was not executed even though it was sent.");
//		}
//	}
	
}
