package piviz.helperClasses;

/**
 * Name generator in case a new restricted name is needed or a name has to be
 * renamed.
 * 
 * @author Anja
 * 
 */
public abstract class NameGenerator {
	private static int uniqueNameCounter = -1;

	/**
	 * Brute Force - TODO: find nicer algorithm.
	 * 
	 * @param oldName
	 * @return
	 */
	public static String generateUniqueName(String oldName) {
		
		uniqueNameCounter++;
		int index = oldName.indexOf("#");
		String newStart;
		if (index != -1) newStart = oldName.substring(0, index);
		else newStart = oldName;
		return newStart + "#" + uniqueNameCounter;
	}

	public static void reset() {
		NameGenerator.uniqueNameCounter = -1;
	}

}
