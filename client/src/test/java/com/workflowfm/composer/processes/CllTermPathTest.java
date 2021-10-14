package com.workflowfm.composer.processes;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;

public class CllTermPathTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFollowPath1() throws InvalidCllPathException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) ** L */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		
		CllTermPath path = new CllTermPath("0");
		CllTerm res = path.follow(tm);
		
		Set<String> expected = new HashSet<String>();
		expected.add("A");
		
		assertEquals(expected,res.getVars());
	}
	
	@Test
	public void testFollowPath2() throws InvalidCllPathException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) ** L */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		
		CllTermPath path = new CllTermPath("1.0");
		CllTerm res = path.follow(tm);
		
		Set<String> expected = new HashSet<String>();
		expected.add("B");
		
		assertEquals(expected,res.getVars());
	}
	
	@Test
	public void testFollowPath3() throws InvalidCllPathException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) ** L */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		
		CllTermPath path = new CllTermPath("1.1");
		CllTerm res = path.follow(tm);
		
		Set<String> expected = new HashSet<String>();
		expected.add("C");
		
		assertEquals(expected,res.getVars());
	}
	
	@Test
	public void testFollowPath4() throws InvalidCllPathException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) ** L */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		
		CllTermPath path = new CllTermPath("1.2");
		CllTerm res = path.follow(tm);
		
		Set<String> expected = new HashSet<String>();
		expected.add("D");
		
		assertEquals(expected,res.getVars());
	}
	
	@Test
	public void testFollowPath5() throws InvalidCllPathException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) ** L */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		
		CllTermPath path = new CllTermPath("2.0");
		CllTerm res = path.follow(tm);
		
		Set<String> expected = new HashSet<String>();
		expected.add("E");
		
		assertEquals(expected,res.getVars());
	}
	
	@Test
	public void testFollowPath6() throws InvalidCllPathException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) ** L */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		
		CllTermPath path = new CllTermPath("2.1.0");
		CllTerm res = path.follow(tm);
		
		Set<String> expected = new HashSet<String>();
		expected.add("G");
		
		assertEquals(expected,res.getVars());
	}
	
	@Test
	public void testFollowPath7() throws InvalidCllPathException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) ** L */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));

		CllTermPath path = new CllTermPath("2.1.1");
		CllTerm res = path.follow(tm);
		
		Set<String> expected = new HashSet<String>();
		expected.add("H");
		
		assertEquals(expected,res.getVars());
	}
	
	@Test
	public void testFollowPath8() throws InvalidCllPathException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) ** L */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));
		
		CllTermPath path = new CllTermPath("3");
		CllTerm res = path.follow(tm);
		
		Set<String> expected = new HashSet<String>();
		expected.add("L");
		
		assertEquals(expected,res.getVars());
	}
	
	@Test
	public void testFollowParent1() throws InvalidCllPathException, NotFoundException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) ** L */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));

		CllTermPath path = new CllTermPath("1.0");
		CllTerm res = path.followParent(tm);
		
		Set<String> expected = new HashSet<String>();
		expected.add("B");
		expected.add("C");
		expected.add("D");
		
		assertEquals(expected,res.getVars());
	}
	
	@Test
	public void testFollowParent2() throws InvalidCllPathException, NotFoundException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) ** L */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).times(new CllTerm("L"));

		CllTermPath path = new CllTermPath("2.1.0");
		CllTerm res = path.followParent(tm);
		
		Set<String> expected = new HashSet<String>();
		expected.add("G");
		expected.add("H");
		
		assertEquals(expected,res.getVars());
	}
}
