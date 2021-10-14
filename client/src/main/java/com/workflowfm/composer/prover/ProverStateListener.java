package com.workflowfm.composer.prover;

/** Observer that is used to react to changes in a proof script. */
public interface ProverStateListener
{
	public void logUpdated(Prover prover);
	public void executionStarted(Prover prover);
	public void executionStopped(Prover prover);
}
