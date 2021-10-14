package com.workflowfm.composer.ui.dialogs;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.properties.ComposerProperties;

public class ExportImageDialog {
	
	private Component parent;
	private ProcessGraph graph;
	private ExceptionHandler handler;
	
	public ExportImageDialog(Component parent, ProcessGraph graph, ExceptionHandler handler) {
		this.parent = parent;
		this.graph = graph;
		this.handler = handler;
	}
	
	public void show() {
		final JFileChooser fc = new JFileChooser(ComposerProperties.imageDirectory()) {
			private static final long serialVersionUID = 656207869481974546L;

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
		
		FileFilter filter = new FileNameExtensionFilter("PNG image file", new String[] {"png"});
		fc.setFileFilter(filter);
		fc.addChoosableFileFilter(filter);
		
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			File file = fc.getSelectedFile();
			if (file != null && !file.getName().toLowerCase().endsWith(".png"))
				file = new File(file.toString() + ".png");
			
			try
			{
				if (file != null && file.getParentFile() != null && file.getParentFile().exists() && file.getParentFile().isDirectory())
					ComposerProperties.set("imageDirectory", file.getParent());

				graph.exportGraphAsImage(file);
			}
			catch (Exception e1)
			{
				handler.handleException("Unable to export image.",e1);
			}
		}
	}	
}
