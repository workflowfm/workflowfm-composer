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
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.command.CreateProcessCommand;
import com.workflowfm.composer.prover.response.CommandFailedResponse;
import com.workflowfm.composer.prover.response.CreateProcessResponse;
import com.workflowfm.composer.prover.response.ProverResponse;

public class CreateProcessCommandTest {
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
		Vector<CllTerm> inputs = new Vector<CllTerm>();
		inputs.add(new CllTerm("X"));
		CllTerm output = new CllTerm("Y");
		
		CreateProcessCommand command = new CreateProcessCommand("P",inputs,output,handler);
		
		hol.execute(command);
		if (command.succeeded()) {
			assertEquals(1,command.getResponses().size());
		} else {
			fail("Command was not executed even though it was sent.");
		}
	}
	
	@Test
	public void testProcess1() {
		String name = "P";
		Vector<CllTerm> inputs = new Vector<CllTerm>();
		inputs.add(new CllTerm("X"));
		CllTerm output = new CllTerm("Y");
		
		CreateProcessCommand command = new CreateProcessCommand(name,inputs,output,handler);
		
		hol.execute(command);
		if (command.succeeded()) {
			
			for (ProverResponse r : command.getResponses()) {
				System.out.println(r.debugString());
				if (r instanceof CreateProcessResponse) {
					CProcess process = ((CreateProcessResponse)r).getProcess();
					assertEquals(inputs, process.getInputCll());
					assertEquals(output, process.getOutputCll());
					assertEquals(name, process.getName());
					assertEquals(true, process.isAtomic());
					assertEquals(0, process.getActions().size());
					assertEquals(false, process.isCopier());
					assertEquals(true, process.isValid());
					assertEquals(true, process.isChecked());
				}
				else {
					fail("Unexpected response received!");
				}
			}
			
		} else {
			fail("Command was not executed even though it was sent.");
		}
	}
	
	@Test
	public void testProcess2() {
		String name = "P";
		Vector<CllTerm> inputs = new Vector<CllTerm>();
		inputs.add(new CllTerm("X"));
		inputs.add(new CllTerm("Y").times(new CllTerm("Z")));
		CllTerm output = new CllTerm("Y").plus(new CllTerm("W").times(new CllTerm("R")));
		
		CreateProcessCommand command = new CreateProcessCommand(name,inputs,output,handler);
		
		hol.execute(command);
		if (command.succeeded()) {
			
			for (ProverResponse r : command.getResponses()) {
				System.out.println(r.debugString());
				if (r instanceof CreateProcessResponse) {
					CProcess process = ((CreateProcessResponse)r).getProcess();
					assertEquals(inputs, process.getInputCll());
					assertEquals(output, process.getOutputCll());
					assertEquals(name, process.getName());
					assertEquals(true, process.isAtomic());
					assertEquals(0, process.getActions().size());
					assertEquals(false, process.isCopier());
					assertEquals(true, process.isValid());
					assertEquals(true, process.isChecked());
				}
				else {
					fail("Unexpected response received!");
				}
			}
			
		} else {
			fail("Command was not executed even though it was sent.");
		}
	}
	
	@Test
	public void testProcess3() {
		String name = "P";
		Vector<ProcessPort> inputs = new Vector<ProcessPort>();
		inputs.add(new ProcessPort("c0",new CllTerm("X")));
		inputs.add(new ProcessPort("c1",new CllTerm("Y").times(new CllTerm("Z"))));
		ProcessPort output = new ProcessPort("co",new CllTerm("Y").plus(new CllTerm("W").times(new CllTerm("R"))));
		CProcess p = new CProcess(name,inputs,output);
		
		CreateProcessCommand command = new CreateProcessCommand(p,handler);
		
		hol.execute(command);
		if (command.succeeded()) {
			
			for (ProverResponse r : command.getResponses()) {
				System.out.println(r.debugString());
				if (r instanceof CreateProcessResponse) {
					CProcess process = ((CreateProcessResponse)r).getProcess();
					assertEquals(p.getInputCll(), process.getInputCll());
					assertEquals(p.getOutputCll(), process.getOutputCll());
					assertEquals(name, process.getName());
					assertEquals(true, process.isAtomic());
					assertEquals(0, process.getActions().size());
					assertEquals(false, process.isCopier());
					assertEquals(true, process.isValid());
					assertEquals(true, process.isChecked());
				}
				else {
					fail("Unexpected response received!");
				}
			}
			
		} else {
			fail("Command was not executed even though it was sent.");
		}
	}
	
	@Test
	public void testCopierProcess() {
		String name = "P";
		Vector<ProcessPort> inputs = new Vector<ProcessPort>();
		inputs.add(new ProcessPort("c0",new CllTerm("X")));
		ProcessPort output = new ProcessPort("co",new CllTerm("X").times(new CllTerm("X")).times(new CllTerm("X")));
		CProcess p = new CProcess(name,inputs,output);
		
		CreateProcessCommand command = new CreateProcessCommand(p,handler);
		
		hol.execute(command);
		if (command.succeeded()) {
			
			for (ProverResponse r : command.getResponses()) {
				System.out.println(r.debugString());
				if (r instanceof CreateProcessResponse) {
					CProcess process = ((CreateProcessResponse)r).getProcess();
					assertEquals(p.getInputCll(), process.getInputCll());
					assertEquals(p.getOutputCll(), process.getOutputCll());
					assertEquals(name, process.getName());
					assertEquals(true, process.isAtomic());
					assertEquals(0, process.getActions().size());
					assertEquals(true, process.isCopier());
					assertEquals(true, process.isValid());
					assertEquals(true, process.isChecked());
				}
				else {
					fail("Unexpected response received!");
				}
			}
			
		} else {
			fail("Command was not executed even though it was sent.");
		}
	}
	
	@Test
	public void testError() {
		String name = "P";
		Vector<CllTerm> inputs = new Vector<CllTerm>();
		inputs.add(new CllTerm("X"));
		CllTerm output = new CllTerm("-","",new Vector<CllTerm>());
		
		CreateProcessCommand command = new CreateProcessCommand(name,inputs,output,handler);
		
		hol.execute(command);
		if (command.succeeded()) {
			
			for (ProverResponse r : command.getResponses()) {
				System.out.println(r.debugString());
				if (r instanceof CommandFailedResponse) { 
					// JSON reply:  {   "response": "CommandFailed",   "content": "Cll_json.to_linprop: Unexpected operator - -" } 
					assertEquals("Cll_json.to_linprop: Unexpected operator - -",((CommandFailedResponse)r).getContent()); 
				}
				else {
					fail("Unexpected response received!");
				}
			}
			
		} else {
			fail("Command was not executed even though it was sent.");
		}
	}
	
}
