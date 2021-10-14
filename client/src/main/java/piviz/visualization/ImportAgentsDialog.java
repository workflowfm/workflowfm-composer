/**
 * Created by Anja Bog. Do not edit this line.
 */
package piviz.visualization;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ImportAgentsDialog extends JDialog implements ActionListener {

	private Reader stream = null;

	private JPanel centerFile, centerText;

	private JTextField chosenFile = new JTextField();

	private File file;

	private JTextArea area;

	ImportAgentsDialog(JFrame owner) {
		super(owner, true);
		setTitle("Import Agents");
	}

	public Reader open() {

		// row layout for the dialog
		this.getContentPane().setLayout(new BorderLayout(10, 10));

		// Create the radio buttons.
		JRadioButton fromFile = new JRadioButton(
				"Import Agent(s) from File System.");
		fromFile.setMnemonic(KeyEvent.VK_F);
		fromFile.setActionCommand("fromFile");
		fromFile.setSelected(true);
		fromFile.addActionListener(this);

		JRadioButton fromText = new JRadioButton(
				"Import Agent(s) from Text Area.");
		fromText.setMnemonic(KeyEvent.VK_T);
		fromText.setActionCommand("fromText");
		fromText.addActionListener(this);

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(fromFile);
		group.add(fromText);

		JPanel radioButtons = new JPanel();
		radioButtons.setLayout(new BoxLayout(radioButtons, BoxLayout.Y_AXIS));
		this.getContentPane().add(radioButtons, BorderLayout.NORTH);

		JLabel radioLabel = new JLabel("Choose source:");
		radioButtons.add(radioLabel);
		radioButtons.add(fromFile);
		radioButtons.add(fromText);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton ok = new JButton();
		ok.setActionCommand("OK");
		ok.setText("OK");
		ok.addActionListener(this);
		buttonPane.add(ok);

		JButton cancel = new JButton();
		cancel.setActionCommand("Cancel");
		cancel.setText("Cancel");
		cancel.addActionListener(this);
		buttonPane.add(cancel);

		JPanel listPane = new JPanel();
		listPane.setLayout(new FlowLayout());
		this.getContentPane().add(listPane, BorderLayout.CENTER);

		centerFile = new JPanel(new FlowLayout());
		this.getContentPane().add(centerFile, BorderLayout.CENTER);
		chosenFile.setText("");
		chosenFile.setColumns(30);
		centerFile.add(chosenFile);

		JButton browse = new JButton("Browse...");
		browse.setActionCommand("browse");
		browse.addActionListener(this);
		centerFile.add(browse);

		centerText = new JPanel(new FlowLayout());
		area = new JTextArea(6, 55);
		centerText.add(area);
		area.setText("");

		this.pack();
		this.show();
		return stream;
	}

	private void setStream() {
		try {
			if (file != null)
				stream = new InputStreamReader(new FileInputStream(file));
			else if (!chosenFile.getText().equals("")){
				File newFile = new File(chosenFile.getText());
				stream = new InputStreamReader(new FileInputStream(newFile));
			}
			else if (!area.getText().equals(""))
				stream = new StringReader(area.getText());
			this.dispose();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "File not found!", "Error",
					JOptionPane.ERROR_MESSAGE);
			stream = null;
			this.dispose();
		}
	}

	private void showFileSelection() {
		area.setText("");
		this.getContentPane().remove(centerText);
		this.getContentPane().add(centerFile, BorderLayout.CENTER);
		this.pack();
		repaint();
	}

	private void showTextSelection() {
		file = null;
		chosenFile.setText("");
		this.getContentPane().remove(centerFile);
		this.getContentPane().add(centerText, BorderLayout.CENTER);
		this.pack();
		repaint();
	}
	
	private void chooseFile(){
		JFileChooser ch = new JFileChooser();
		ch.showOpenDialog(this);
		file = ch.getSelectedFile();
		if (file != null)
			chosenFile.setText(file.getAbsolutePath());
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("OK"))
			setStream();
		else if (cmd.equals("Cancel"))
			this.dispose();
		else if (cmd.equals("fromFile"))
			showFileSelection();
		else if (cmd.equals("fromText"))
			showTextSelection();
		else if (cmd.equals("browse"))
			chooseFile();
	}
}
