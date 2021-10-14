package com.workflowfm.composer.processes.deploy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.workflowfm.composer.exceptions.UserError;

public class DeploymentFile {
	private String path;
	private String content;
	private boolean overwrite;
	
	public boolean deploy() throws IOException, UserError
	{
		System.err.println("Deploying file: [" + path + "] o/w: " + overwrite);
		
		File file = new File(path);

		if (file.exists() && !overwrite) {
	    	System.err.println("File already exists! - skipping");
	    	return false;
	    }
	    
	    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) 
	    		throw new UserError("Unable to create directory: " + file.getParentFile().getAbsolutePath());
	    
		FileWriter outFile = null;
	    try
	    {
	      outFile = new FileWriter(file,false);
	      PrintWriter out = new PrintWriter(outFile);
	      out.print(content);
	    }
	    finally
	    {
	      if (outFile != null)
	        outFile.close();
	    }
	    return true;
	}
	
	public String getFilePath() {
		return this.path;
	}
	
	public void setFilePath(String path) {
		this.path = path;
	}

	public String getContent() {
		return content;
	}

}
