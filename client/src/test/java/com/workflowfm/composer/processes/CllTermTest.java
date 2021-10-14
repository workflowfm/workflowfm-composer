package com.workflowfm.composer.processes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

import org.junit.Test;

import com.google.gson.JsonSyntaxException;
import com.workflowfm.composer.exceptions.InvalidCllPathException;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.processes.CllTerm;
import com.workflowfm.composer.processes.CllTermPath;
import com.workflowfm.composer.utils.CustomGson;

public class CllTermTest {
	
	//private static Prover prover = new HolLight();

	@Test
	public void testArgs1() {
		// A ** (B ** C) ** ((D ** E) ** F)
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").times(new CllTerm("C")))
				.times(new CllTerm("D").times(new CllTerm("E")).times(new CllTerm("F")));
		Vector<String> res = new Vector<String>();
		for (CllTerm a : tm.getArgs()) {
			res.add(a.getName());
		}
		
		Vector<String> expected = new Vector<String>();
		expected.add("A");
		expected.add("B");
		expected.add("C");
		expected.add("D");
		expected.add("E");
		expected.add("F");
		
		assertEquals(expected,res);
	}
	
	@Test
	public void testGetVars1() {
		/* Q:LinProp */
		//JsonObject json = getJsonObj("{ \"type\": \"var\", \"name\": \"Q\" }");
		
		Set<String> res = new CllTerm("Q").getVars();
		
		Set<String> expected = new HashSet<String>();
		expected.add("Q");
		
		assertEquals(expected,res);
	}

	@Test
	public void testGetVars2() {
		/* A ** B ** C */
		//JsonObject json = getJsonObj("{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    { \"type\": \"var\", \"name\": \"B\" },\n    { \"type\": \"var\", \"name\": \"C\" }\n  ]\n}");
		
		Set<String> res = new CllTerm("A").tensor(new CllTerm("B")).tensor(new CllTerm("C")).getVars();
		
		Set<String> expected = new HashSet<String>();
		expected.add("A");
		expected.add("B");
		expected.add("C");
		
		assertEquals(expected,res);
	}

	@Test
	public void testGetVars3() {
		/* A ++ NEG B ++ C */
		//JsonObject json = getJsonObj("{\n  \"type\": \"plus\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    { \"type\": \"neg\", \"args\": [ { \"type\": \"var\", \"name\": \"B\" } ] },\n    { \"type\": \"var\", \"name\": \"C\" }\n  ]\n}");
		
		Set<String> res = new CllTerm("A").plus(new CllTerm("B").neg()).plus(new CllTerm("C")).getVars();
		
		Set<String> expected = new HashSet<String>();
		expected.add("B");
		expected.add("A");
		expected.add("C");
		
		assertEquals(expected,res);
	}
	
	@Test
	public void testGetVars4() {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		Set<String> res = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H")))).getVars();
		
		Set<String> expected = new HashSet<String>();
		expected.add("A");
		expected.add("B");
		expected.add("C");
		expected.add("D");
		expected.add("E");
		expected.add("G");
		expected.add("H");
		
		assertEquals(expected,res);
	}
	
	@Test
	public void testCustomGsonGetVars1() {
		/* Q:LinProp */
		String json = "{ \"type\": \"var\", \"name\": \"Q\" }";
		
		CllTerm tm;
		Set<String> res = new HashSet<String>();
		
		String classPath = "com.workflowfm.composer.processes.CllTerm";

		try {
			tm = (CllTerm) CustomGson.getGson().fromJson(json,
					(Class<?>) Class.forName(classPath));
			res = tm.getVars();
		} catch (JsonSyntaxException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Set<String> expected = new HashSet<String>();
		expected.add("Q");
		
		assertEquals(expected,res);
	}
	
	@Test
	public void testCustomGsonGetVars2() {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		String json = "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}";
		
		CllTerm tm;
		Set<String> res = new HashSet<String>();
		
		String classPath = "com.workflowfm.composer.processes.CllTerm";

		try {
			tm = (CllTerm) CustomGson.getGson().fromJson(json,
					(Class<?>) Class.forName(classPath));
			res = tm.getVars();
		} catch (JsonSyntaxException | ClassNotFoundException e) {
			fail(e.getMessage());
		}
		
		Set<String> expected = new HashSet<String>();
		expected.add("A");
		expected.add("B");
		expected.add("C");
		expected.add("D");
		expected.add("E");
		expected.add("G");
		expected.add("H");
		
		assertEquals(expected,res);
	}
	
	@Test
	public void testCustomGsonEquals() {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		String json = "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}";
		
		CllTerm tm = new CllTerm("X");
		
		String classPath = "com.workflowfm.composer.processes.CllTerm";

		try {
			tm = (CllTerm) CustomGson.getGson().fromJson(json,
					(Class<?>) Class.forName(classPath));
		} catch (JsonSyntaxException | ClassNotFoundException e) {
			fail(e.getMessage());
		}
		
		CllTerm expected = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		
		assertEquals(expected,tm);
	}
	
	@Test
	public void testFlatten() {
		
		// A ** (B ** C) ** ((D ** E) ** F)
		
		CllTerm bc = new CllTerm("B").times(new CllTerm("C"));
		CllTerm de = new CllTerm("D").times(new CllTerm("E"));
		
		Vector<CllTerm> abcargs = new Vector<CllTerm>();
		abcargs.add(new CllTerm("A"));
		abcargs.add(bc);
		CllTerm abc = new CllTerm("times","",abcargs);
		
		Vector<CllTerm> defargs = new Vector<CllTerm>();
		defargs.add(de);
		defargs.add(new CllTerm("F"));
		CllTerm def = new CllTerm("times","",defargs);
		
		Vector<CllTerm> fullargs = new Vector<CllTerm>();
		fullargs.add(abc);
		fullargs.add(def);
		CllTerm res = new CllTerm("times","",fullargs);
		
		res.flatten();
		
		CllTerm expected = new CllTerm("A").times(new CllTerm("B").times(new CllTerm("C")))
				.times(new CllTerm("D").times(new CllTerm("E")).times(new CllTerm("F")));
		
		assertEquals(expected,res);
	}
	
	@Test
	public void testDeleteBranch() throws NumberFormatException, InvalidCllPathException, NotFoundException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		//JsonObject json = getJsonObj( "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}");
		
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		tm.deletePath(new CllTermPath("1"));
		
		Set<String> expected = new HashSet<String>();
		expected.add("A");
		expected.add("E");
		expected.add("G");
		expected.add("H");
		
		assertEquals(expected,tm.getVars());
	}
	
	@Test
	public void testSetName() throws NumberFormatException, InvalidCllPathException { // Tests 1 and 2 check if vectors were cloned. This one checks if the elements were also cloned.
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		CllTerm expected = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("RRR")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("SSS").times(new CllTerm("H"))));
		
		new CllTermPath("1.1").follow(tm).setName("RRR");
		new CllTermPath("2.1.0").follow(tm).setName("SSS");
		
		assertEquals(expected,tm);
	}
	
	@Test
	public void testClone1() throws NumberFormatException, InvalidCllPathException, NotFoundException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		CllTerm clone = new CllTerm(tm);
		
		tm.deletePath(new CllTermPath("1"));
		CllTermPath path = new CllTermPath("1.1");
		String res = path.follow(clone).getName();
		
		assertEquals("C",res);
	}
	
	@Test
	public void testClone2() throws NumberFormatException, InvalidCllPathException, NotFoundException {
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		CllTerm expected = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		CllTerm clone = new CllTerm(tm);
		
		tm.deletePath(new CllTermPath("1"));
		
		assertEquals(expected,clone);
	}
	
	@Test
	public void testClone3() throws NumberFormatException, InvalidCllPathException { // Tests 1 and 2 check if vectors were cloned. This one checks if the elements were also cloned.
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		CllTerm expected = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		CllTerm clone = new CllTerm(tm);
		
		new CllTermPath("1.1").follow(tm).setName("RRR");
		new CllTermPath("2.1.0").follow(tm).setName("SSS");
		
		assertEquals(expected,clone);
	}
	
	@Test
	public void testHasSubterm1() { 
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		Optional<CllTermPath> result = tm.hasSubterm(new CllTerm("D"));
		
		assertEquals(true,result.isPresent());
	}
	
	@Test
	public void testHasSubterm2() { 
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		Optional<CllTermPath> result = tm.hasSubterm(new CllTerm("C").plus(new CllTerm("D")));
		
		assertEquals(true,result.isPresent());
	}	
	
	@Test
	public void testHasSubterm3() { 
		CllTerm tm = new CllTerm("A");
		Optional<CllTermPath> result = tm.hasSubterm(new CllTerm("A"));
		
		assertEquals(true,result.isPresent());
	}
	
	@Test
	public void testHasSubterm4() { 
		CllTerm tm = new CllTerm("B");
		Optional<CllTermPath> result = tm.hasSubterm(new CllTerm("A"));
		
		assertEquals(false,result.isPresent());
	}
	
	
	@Test
	public void testHasSubterm5() { 
		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
		Optional<CllTermPath> result = tm.hasSubterm(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")));
		
		assertEquals(true,result.isPresent());
	}
	
//	@Test
//	public void testRemoveSubterm1() { 
//		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
//		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
//				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
//		CllTerm expected = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")))
//				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
//		CllTerm result = tm.removeSubterm(new CllTerm("D"));
//		
//		System.out.println(prover.cllResourceString(result));
//		assertEquals(expected,result);
//	}
//	
//	@Test
//	public void testRemoveSubterm2() { 
//		/* A ** (B ++ C ++ D) ** (E ++ (G ** H)) */
//		CllTerm tm = new CllTerm("A").times(new CllTerm("B").plus(new CllTerm("C")).plus(new CllTerm("D")))
//				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
//		CllTerm expected = new CllTerm("A").times(new CllTerm("B"))
//				.times(new CllTerm("E").plus(new CllTerm("G").times(new CllTerm("H"))));
//		CllTerm result = tm.removeSubterm(new CllTerm("C").plus(new CllTerm("D")));
//		
//		System.out.println(prover.cllResourceString(result));
//		assertEquals(expected,result);
//	}
	
	@Test
	public void testEquals1() {
		boolean result = new CllTerm("C").equals(new CllTerm("C"));
		assertEquals(true,result);
	}
	
	@Test
	public void testEquals2() {
		assertEquals(new CllTerm("C"),new CllTerm("C"));
	}
	
//	@Test
//	public void testEquals3() {
//		Set<CllTerm> set = new HashSet<CllTerm>();
//		set.add(new CllTerm("C"));
//		
//		assertEquals(false, set.contains(new CllTerm("C"))); // HashSet only checks equality with rest of bucket, so hashcode comes first!
//	}
}
