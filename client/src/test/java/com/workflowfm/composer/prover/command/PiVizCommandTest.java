package com.workflowfm.composer.prover.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Vector;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.LogExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.processes.deploy.DeploymentFile;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.command.PiVizCommand;
import com.workflowfm.composer.prover.response.DeployResponse;
import com.workflowfm.composer.prover.response.ProverResponse;

public class PiVizCommandTest {
	private static HolLight hol;
	private static ExceptionHandler handler;
	private CProcess process;
	
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
	
	@Before
	public void setupProcess() {
		Vector<ProcessPort> inputsp = new Vector<ProcessPort>();
		inputsp.add(new ProcessPort("cPb_C_1", new CllTerm("C")));
		inputsp.add(new ProcessPort("cPb_E_2", new CllTerm("E")));
		ProcessPort outputp = new ProcessPort("oPb_G_",new CllTerm("G"));
		process = new CProcess("Pb", inputsp, outputp);
		process.setProc("Pb (cPb_C_1,cPb_E_2,oPb_G_) =\nComp (In cPb_C_1 [cPb_C_1__a_C] Zero)\n(Comp (In cPb_E_2 [cPb_E_2__a_E] Zero)\n(Res [oPb_G___a_G] (Out oPb_G_ [oPb_G___a_G] Zero)))");
	}
	
	@Test
	public void testResponse() {
		PiVizCommand command = new PiVizCommand(process, new Vector<CProcess>(),handler);
		hol.execute(command);
		if (command.succeeded()) {
			assertEquals(1,command.getResponses().size());
			for (ProverResponse r : command.getResponses()) {
				System.out.println(r.debugString());
				if (r instanceof DeployResponse) {
					Collection<DeploymentFile> files = ((DeployResponse)r).getFiles();
					assertEquals(1,files.size());
					for (DeploymentFile f : files)
						System.out.println(f.getContent());
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
