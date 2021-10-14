package com.workflowfm.composer.session.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.session.ComposerSaveState;
import com.workflowfm.composer.session.CompositionSession;

public class LoadAction extends CompositionSessionAction {

	private static final long serialVersionUID = 815069127265776997L;
	private Component parent;

	public LoadAction(Component parent, CompositionSession session, ExceptionHandler handler) {
		super("Open...", session, handler, "silk_icons/folder_page_white.png", KeyEvent.VK_O, KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JFileChooser fc = new JFileChooser(ComposerProperties.proofScriptDirectory());

		FileFilter filter = new FileNameExtensionFilter("Composer JSON file", new String[] {"json"});
		fc.setFileFilter(filter);
		fc.addChoosableFileFilter(filter);

		if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			File file = fc.getSelectedFile();
			try
			{
				if (file != null && file.getParentFile() != null && file.getParentFile().exists() && file.getParentFile().isDirectory())
					ComposerProperties.set("proofScriptDirectory", file.getParent());

				ComposerSaveState state = ComposerSaveState.loadFile(file);
				state.loadInSession(getSession());
				getSession().setSaveFile(file);
			}
			catch (Exception e1)
			{
				getExceptionHandler().handleException("Unable to load project.", e1);
			}
		}
	}
}

