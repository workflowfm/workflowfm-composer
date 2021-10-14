package com.workflowfm.composer.session.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.GraphWindow;
import com.workflowfm.composer.ui.Window;
import com.workflowfm.composer.ui.WindowManager;
import com.workflowfm.composer.ui.dialogs.ExportImageDialog;

public class ExportActiveImageAction extends CompositionSessionAction {

	private static final long serialVersionUID = 6627158927717654263L;
	private WindowManager manager;
	
	public ExportActiveImageAction(WindowManager manager, CompositionSession session, ExceptionHandler handler) {
		super("Export graph as PNG image", session, handler, "silk_icons/photo.png", KeyEvent.VK_E, KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
		this.manager = manager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Window window = manager.getActiveWindow();
		if (window instanceof GraphWindow)
			new ExportImageDialog(window.getPanel(), ((GraphWindow)window).getGraph(), getExceptionHandler()).show();
	}

}
