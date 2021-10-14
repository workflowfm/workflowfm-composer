package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.session.CompositionSession;

public class VerifyAncestorsAction extends CompositionSessionAction {

	private static final long serialVersionUID = 192827604138761066L;
	private CProcess process;
	
	public VerifyAncestorsAction(CProcess process, CompositionSession session, ExceptionHandler exceptionHandler) {
		super("Verify Ancestors", session, exceptionHandler, "silk_icons/shield_go.png", KeyEvent.VK_B, KeyEvent.VK_B, ActionEvent.ALT_MASK);
		this.process = process;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!process.isChecked() || !process.isValid()) return;
		for (CProcess p : getSession().getAncestors(process)) {
			if (!p.isChecked()) new VerifyProcessAction(p, getSession(), getExceptionHandler()).actionPerformed(e);
		}
	}	

}