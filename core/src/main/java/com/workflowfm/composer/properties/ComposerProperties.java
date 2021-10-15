package com.workflowfm.composer.properties;

import java.io.File;

import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.utils.Log;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;

public class ComposerProperties {

  final private static String FILENAME = "composer.properties";
  final private static String PATH = ".workflowfm";

  private static FileBasedConfiguration prefs;

  static {
    try {
      String dir = System.getProperty("user.home") + File.separator + PATH;
      File file = new File(dir + File.separator + FILENAME);

      if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
        Log.d("Reading preferences from: " + file.getAbsolutePath());
        file.createNewFile();

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
          new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
          .configure(params.properties()
                     .setFile(file));
        builder.setAutoSave(true);
        prefs = builder.getConfiguration();
      } else {
        throw new NotFoundException("directory", dir);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 
  } 

  public static void load(File file) {
    try {
      Parameters params = new Parameters();
      FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
        new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
        .configure(params.properties()
                   .setFile(file));
      builder.setAutoSave(true);
      prefs = builder.getConfiguration();
    } catch (Exception e) {
      e.printStackTrace();
    } 
  } 

	public static void set(String key, int value) { 
		Log.d("Set preference [" + key + "] to [" + value + "]");
		prefs.setProperty(key, value); 
	}
	public static void set(String key, boolean value) { 
		Log.d("Set preference [" + key + "] to [" + value + "]");
		prefs.setProperty(key, value); 
	}
	public static void set(String key, String value) {
		if (value.length() == 0) {
			Log.d("Deleting preference [" + key + "] (empty value)");
			prefs.clearProperty(key);
		} else {
			Log.d("Set preference [" + key + "] to [" + value + "]");
			prefs.setProperty(key, value);
		}
	}
	
	public static void remove(String key) { 
		Log.d("Deleting preference [" + key + "] (empty value)");
		prefs.clearProperty(key); 
	}
	
	
	public static int processNodeWidth() { return prefs.getInt("processNodeWidth", 120); } 
	public static int processNodeHeight() { return prefs.getInt("processNodeWidth", 40); }
	public static int processCopierNodeRadius() { return prefs.getInt("processCopierNodeRadius", 15); }
	
	public static boolean processNodeAutoResize() { return prefs.getBoolean("processNodeAutoResize", true); }
	
	public static int intraCellSpacing() { return prefs.getInt("processNodeWidth", 20); }
	public static int interHierarchySpacing() { return prefs.getInt("interHierarchySpacing", 40); }
	public static int interRankCellSpacing() { return prefs.getInt("interRankCellSpacing", 110); } //130
	public static int groupPadding() { return prefs.getInt("groupPadding",2); } //12
	
	public static String processColour(boolean atomic) { return (atomic?
			prefs.getString("atomicProcessColour","#BBDEFB"): //#F78400
				prefs.getString("compositeProcessColour","#64B5F6")); } //#DE7600
	public static String portEdgeColour() { return prefs.getString("portEdgeColour", processColour(true)); }
	public static String edgeColour() { return prefs.getString("edgeColour","#686868"); } //#555555
	public static String bufferColour() { return prefs.getString("bufferColour","#686868"); } // "#BBBBBB"
	public static String hoverHighlightColour() { return prefs.getString("hoverHighlightColour","#F78400"); } // "#BBBBBB"
	public static String selectHighlightColour() { return prefs.getString("selectHighlightColour","#33691E"); } // "#BBBBBB"
	
	public static String workingDirectory() { return System.getProperty("user.dir"); }
	public static String launchProverCommand() {
		String command = prefs.getString("launchProverCommand", "scripts" + File.separator + "launch_prover");
		return command;
	}
	
	public static String proofScriptDirectory() { return prefs.getString("proofScriptDirectory",workingDirectory() + File.separator + "proofs"); }
	public static String imageDirectory() { return prefs.getString("imageDirectory",proofScriptDirectory()); }

	public static boolean consoleVisible() { return prefs.getBoolean("consoleVisible",false); }
	public static int processesDividerLocation() { return prefs.getInt("processesDividerLocation", 600); }
	public static int editorDividerLocation() { return prefs.getInt("editorDividerLocation", 300); }
	public static int consoleDividerLocation() { return prefs.getInt("consoleDividerLocation", 800); }
	
	public static int frameWidth() { return prefs.getInt("frameWidght", 1000); }
	public static int frameHeight() { return prefs.getInt("frameHeight", 1000); }
	
	public static boolean deployStateful() { return prefs.getBoolean("deployStateful", true); }
	public static boolean deployMain() { return prefs.getBoolean("deployMain", true); }
	public static boolean deployJava() { return prefs.getBoolean("deployJava", false); }
	
	public static String projectName() { return prefs.getString("projectName", ""); }
	public static String deployPackageName() { return prefs.getString("deployPackageName", "com.workflowfm." + projectName().toLowerCase()); }
	public static String deployFolder() { return prefs.getString("deployFolder", workingDirectory()); }
	
	public static String serverURL() { return prefs.getString("server", "localhost"); }
	public static int serverPort() { return prefs.getInt("port", 7000); }
	public static int serverMaxAttempts() { return prefs.getInt("serverMaxAttempts", 10); }
	
	public static String configurationFile() { return prefs.getString("configurationFile", "conf/default.properties"); }
}
