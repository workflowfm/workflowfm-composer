package com.workflowfm.composer.graph;


/** All vertices that are parts of a graph of a composable process must have an identifiable bundle. 
 * It must also be possible to clone them with a new bundle name. */
public interface ComposableCell {
	public String getBundle();
	public ComposableCell newBundle (String newBundle);
}
