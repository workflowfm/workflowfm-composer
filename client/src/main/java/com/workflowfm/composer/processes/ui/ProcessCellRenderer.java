package com.workflowfm.composer.processes.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.workflowfm.composer.processes.ProcessStore;
import com.workflowfm.composer.utils.UIUtils;

/**
 * List renderer that will render different icons next to services depending on
 * if they are atomic or composite services.
 */
@SuppressWarnings("serial")
public class ProcessCellRenderer extends JLabel implements ListCellRenderer<String> {
	private final ProcessStore store;

	public ProcessCellRenderer(ProcessStore store) {
		this.store = store;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value,
			int index, boolean isSelected, boolean cellHasFocus) {
		setText(value);

		try {
			setIcon(store.getProcess(value).getIcon());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}

}

@SuppressWarnings("serial")
class ButtonCellRenderer extends JButton implements ListCellRenderer {
	private final ImageIcon addIcon = UIUtils.getIcon("silk_icons/add.png");

	ButtonCellRenderer() {

	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		String s = value.toString();
		// setText(s);

		try {
			setIcon(addIcon);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// if (isSelected)
		// {
		// setBackground(list.getSelectionBackground());
		// setForeground(list.getSelectionForeground());
		// } else
		// {
		// setBackground(list.getBackground());
		// setForeground(list.getForeground());
		// }
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}
}