package com.workflowfm.composer.prover;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;
import com.workflowfm.composer.processes.ui.PortEdge;
import com.workflowfm.composer.prover.HolLight;

public class HolLightTest {

	private HolLight hol;
	
	@Before
	public void setUp() throws Exception {
		hol = new HolLight();
	}

	@Test
	public void testCllPath1() throws InvalidCllPathException {
		System.out.println("Testing CLL path 1");
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		PortEdge e = new PortEdge(tm, tm, new CllTermPath("0"), false, false); 
		
		assertEquals("lr",hol.cllPath(e));
	}

	@Test
	public void testCllPath2() throws InvalidCllPathException {
		System.out.println("Testing CLL path 2");
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		PortEdge e = new PortEdge(tm, tm, new CllTermPath("1.0"), false, false);
		
		assertEquals("rlrlr",hol.cllPath(e));	
	}
	
	@Test
	public void testCllPath3() throws InvalidCllPathException {
		System.out.println("Testing CLL path 3");
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		PortEdge e = new PortEdge(tm, tm, new CllTermPath("1.1"), false, false);
		
		assertEquals("rlrrlr",hol.cllPath(e));	
	}
	
	@Test
	public void testCllPath4() throws InvalidCllPathException {
		System.out.println("Testing CLL path 4");
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		PortEdge e = new PortEdge(tm, tm, new CllTermPath("1.2"), false, false);
		
		assertEquals("rlrrr",hol.cllPath(e));	
	}
	
	@Test
	public void testCllPath5() throws InvalidCllPathException {
		System.out.println("Testing CLL path 5");
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		PortEdge e = new PortEdge(tm, tm, new CllTermPath("2.0"), false, false);
		
		assertEquals("rrlrlr",hol.cllPath(e));	
	}
	
	@Test
	public void testCllPath6() throws InvalidCllPathException {
		System.out.println("Testing CLL path 6");
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		PortEdge e = new PortEdge(tm, tm, new CllTermPath("2.1.0"), false, false);
		
		assertEquals("rrlrrlr",hol.cllPath(e));	
	}
	
	@Test
	public void testCllPath7() throws InvalidCllPathException {
		System.out.println("Testing CLL path 7");
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		PortEdge e = new PortEdge(tm, tm, new CllTermPath("2.1.1"), false, false);
		
		assertEquals("rrlrrr",hol.cllPath(e));	
	}
	
	@Test
	public void testCllPath8() throws InvalidCllPathException {
		System.out.println("Testing CLL path 8");
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		PortEdge e = new PortEdge(tm, tm, new CllTermPath("3"), false, false);
		
		assertEquals("rrr",hol.cllPath(e));	
	}
}
