package com.workflowfm.composer.prover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonSyntaxException;
import com.workflowfm.composer.processes.ProcessPort;
import com.workflowfm.composer.utils.CustomGson;

public class CustomGsonTest {
	
	@Before
	public void setUp() {
	}

	public Object testJson (String json, String classPath) {
		Object res = null;
		try {
			res = CustomGson.getGson().fromJson(json,
					(Class<?>) Class.forName(classPath));
		} catch (JsonSyntaxException | ClassNotFoundException e) {
			fail(e.getMessage());
		}
		assertEquals("class " + classPath,res.getClass().toString());
		return res;
	}

	@Test
	public void testCllTerm() {
		String json = "{\n  \"type\": \"times\",\n  \"args\": [\n    { \"type\": \"var\", \"name\": \"A\" },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"B\" },\n        { \"type\": \"var\", \"name\": \"C\" },\n        { \"type\": \"var\", \"name\": \"D\" }\n      ]\n    },\n    {\n      \"type\": \"plus\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"E\" },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"G\" },\n            { \"type\": \"var\", \"name\": \"H\" }\n          ]\n        }\n      ]\n    }\n  ]\n}";		
		String classPath = "com.workflowfm.composer.processes.CllTerm";
		testJson(json,classPath);
	}

	@Test
	public void testProcessPort1() {
		String json = "{ \"channel\": \"cPa_A_1\", \"cll\": { \"type\": \"var\", \"name\": \"A\" } }";		
		String classPath = "com.workflowfm.composer.processes.ProcessPort";
		ProcessPort p = (ProcessPort)testJson(json,classPath);
		assertEquals("cPa_A_1",p.getChannel());
	}
	
	@Test
	public void testProcessPort2() {
		/*  `(A ** B ++ C ** D)<>(c3Po:num)` */
		String json =  "{\n  \"channel\": \"c3Po\",\n  \"cll\": {\n    \"type\": \"times\",\n    \"args\": [\n      { \"type\": \"var\", \"name\": \"A\" },\n      {\n        \"type\": \"plus\",\n        \"args\": [\n          { \"type\": \"var\", \"name\": \"B\" },\n          {\n            \"type\": \"times\",\n            \"args\": [\n              { \"type\": \"var\", \"name\": \"C\" },\n              { \"type\": \"var\", \"name\": \"D\" }\n            ]\n          }\n        ]\n      }\n    ]\n  }\n}";		
		String classPath = "com.workflowfm.composer.processes.ProcessPort";
		ProcessPort p = (ProcessPort)testJson(json,classPath);
		assertEquals("c3Po",p.getChannel());
	}
	
	@Test
	public void testCProcess() {
		/* let mypa = Proc.create "Pa" [`A:LinProp`;`B:LinProp`] `C:LinProp`;;
		   (Json_io.string_of_json o Json_comp.from_process) mypa;;
		 */
		String json = "{\n  \"name\": \"Pa\",\n  \"inputs\": [\n    { \"channel\": \"cPa_A_1\", \"cll\": { \"type\": \"var\", \"name\": \"A\" } },\n    { \"channel\": \"cPa_B_2\", \"cll\": { \"type\": \"var\", \"name\": \"B\" } }\n  ],\n  \"output\": { \"channel\": \"oPa_C_\", \"cll\": { \"type\": \"var\", \"name\": \"C\" } },\n  \"proc\":\n    \"Pa (cPa_A_1,cPa_B_2,oPa_C_) =\\nComp (In cPa_A_1 [cPa_A_1__a_A] Zero)\\n(Comp (In cPa_B_2 [cPa_B_2__a_B] Zero)\\n(Res [oPa_C___a_C] (Out oPa_C_ [oPa_C___a_C] Zero)))\",\n  \"dependencies\": [],\n  \"copier\": false,\n  \"intermediate\": false\n}"; 
		String classPath = "com.workflowfm.composer.processes.CProcess";
		//CProcess p = (CProcess)
		testJson(json,classPath);
		//assertEquals("c3Po",p.getChannel());
	}
}
