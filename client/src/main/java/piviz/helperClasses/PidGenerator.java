package piviz.helperClasses;

/**
 * Generate unique process identifiers.
 * @author Anja
 *
 */
public abstract class PidGenerator {
	private static int pidCounter = -1;
	private static int initialPid = -1;

	/**
	 * Brute Force - TODO: find nicer algorithm.
	 * 
	 * @param oldName
	 * @return
	 */
	public static String generateNewPidFromOldOne(String oldName) {
		pidCounter++;
		int index = oldName.indexOf("#");
		String newStart;
		if (index != -1) newStart = oldName.substring(0, index);
		else newStart = oldName;
		return newStart + "#" + pidCounter;
	}
	
	public static String generateNewPid(){
		initialPid++;
		return "pid_" + initialPid;
	}
	
	public static void reset(){
		PidGenerator.initialPid = -1;
		PidGenerator.pidCounter = -1;
	}
	
	public static String getIdFromName(String name){
		int index = name.indexOf("#");
		String newName;
		if (index != -1){ 
			newName = name.substring(index);
			return newName;
		}
		else return "";
	}
	
	public static String getNameWithoutId(String name){
		int index = name.indexOf("#");
		String newName;
		if (index != -1){ 
			newName = name.substring(0, index);
			return newName;
		}
		else return name;
	}

}
