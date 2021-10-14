package com.workflowfm.composer.utils;

public interface Completable {
	public void addCompletionListener(CompletionListener a);
	public void removeCompletionListener(CompletionListener a);
}
