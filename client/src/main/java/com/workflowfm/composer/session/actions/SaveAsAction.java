package com.workflowfm.composer.session.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.session.ComposerSaveState;
import com.workflowfm.composer.session.CompositionSession;

public class SaveAsAction extends CompositionSessionAction {

	private static final long serialVersionUID = -7299426220928788446L;
	private Component parent;
	
	public SaveAsAction(Component parent, CompositionSession session, ExceptionHandler handler) {
		super("Save as...", session, handler, "silk_icons/drive_disk.png", KeyEvent.VK_A, KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JFileChooser fc = new JFileChooser(ComposerProperties.proofScriptDirectory()) {

			private static final long serialVersionUID = -4157844272109292853L;

			@Override
			public void approveSelection(){
				File f = getSelectedFile();
				if(f.exists() && getDialogType() == SAVE_DIALOG){
					int result = JOptionPane.showConfirmDialog(this,"The file already exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
					switch(result){
					case JOptionPane.YES_OPTION:
						super.approveSelection();
						return;
					case JOptionPane.NO_OPTION:
						return;
					case JOptionPane.CLOSED_OPTION:
						return;
					case JOptionPane.CANCEL_OPTION:
						cancelSelection();
						return;
					}
				}
				super.approveSelection();
			}        
		};

		FileFilter filter = new FileNameExtensionFilter("Composer JSON file", new String[] {"json"});
		fc.setFileFilter(filter);
		fc.addChoosableFileFilter(filter);

		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			File file = fc.getSelectedFile();
			if (file != null && !file.getName().toLowerCase().endsWith(".json"))
				file = new File(file.toString() + ".json");
			
			try
			{
				if (file != null && file.getParentFile() != null && file.getParentFile().exists() && file.getParentFile().isDirectory())
					ComposerProperties.set("proofScriptDirectory", file.getParent());

				new ComposerSaveState(getSession()).saveToFile(file);
				getSession().setSaveFile(file);
				getSession().notifySessionSaved();
			}
			catch (Exception e1)
			{
				getExceptionHandler().handleException("Unable to save project.", e1);
			}
		}
	}
}

