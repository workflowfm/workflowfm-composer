package com.workflowfm.composer.session;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Vector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.utils.CustomGson;
import com.workflowfm.composer.utils.Log;
import com.workflowfm.composer.workspace.Workspace;
import com.workflowfm.composer.workspace.WorkspaceSaveState;

public class ComposerSaveState {

	private Collection<CProcess> processes;
	private Collection<WorkspaceSaveState> workspaces;
	private int workspaceCounter;
	
	public ComposerSaveState(CompositionSession session) {
		this.processes = session.getProcesses();
		this.workspaces = new Vector<WorkspaceSaveState>();
		for (Workspace workspace : session.getWorkspaces())
			this.workspaces.add(new WorkspaceSaveState(workspace));
		this.workspaceCounter = session.getWorkspaceCounter();
	}

	public void loadInSession(CompositionSession session) {
		session.reset();
		session.setWorkspaceCounter(workspaceCounter);
		for (CProcess process : this.processes) {
			process.unCheck();
			session.addProcess(process);
		}
		for (WorkspaceSaveState save : this.workspaces)
			save.loadInSession(session);
	}

	public void saveToFile(File file) throws IOException {
		Log.d("Saving to file: " + file);
		String result = CustomGson.getGson().toJson(this);

		PrintWriter out = null;
		//FileWriter outFile = null;
		try
		{
			FileWriter outFile = new FileWriter(file);
			out = new PrintWriter(outFile);
			out.print(result);

			//setCurrentFile(file);
			//proofScriptTextArea.setText(script);
		} 
		finally	{
			out.close();
		}
	}

	public static ComposerSaveState loadFile(File file) throws FileNotFoundException, IOException, ClassNotFoundException, JsonSyntaxException {
		if (file != null && file.exists() && file.isFile() && 
				file.getParentFile() != null && file.getParentFile().exists() && file.getParentFile().isDirectory())
			ComposerProperties.set("proofScriptDirectory", file.getParent());

		FileInputStream stream = null;
		StringBuffer buffer = new StringBuffer();
		ComposerSaveState result = null;
		
		try {
			stream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(stream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String read;
			while ((read = br.readLine()) != null) {
				buffer.append(read + "\n");
			}
			in.close();
			
			JsonObject object = new JsonParser().parse(buffer.toString()).getAsJsonObject();

			String classPath = "com.workflowfm.composer.session.ComposerSaveState";
			result = (ComposerSaveState) CustomGson.getGson().fromJson(object,
					(Class<?>) Class.forName(classPath));			
		} finally {
			if (stream != null)
				stream.close();
		}
		return result;
	}
}
