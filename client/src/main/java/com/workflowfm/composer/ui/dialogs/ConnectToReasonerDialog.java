package com.workflowfm.composer.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.properties.ComposerProperties;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.ui.SpringUtilities;
import com.workflowfm.composer.utils.UIUtils;

public class ConnectToReasonerDialog extends JDialog implements ActionListener {
	
	private ExceptionHandler handler;
	
  private JTextField host;
  private JTextField port;

	private HolLight prover;

	public ConnectToReasonerDialog(JFrame parent, ExceptionHandler handler) {
		super(parent, "Connect to Reasoner", Dialog.ModalityType.DOCUMENT_MODAL);
    host = new JTextField(ComposerProperties.serverURL(), 20);
    port = new JTextField(Integer.toString(ComposerProperties.serverPort()), 5);
		this.handler = handler;
	}

  public void setup() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    JPanel inputPanel = new JPanel();

    JLabel hostLabel = new JLabel("Host:", JLabel.TRAILING);
    inputPanel.add(hostLabel);
    inputPanel.add(host);
    hostLabel.setLabelFor(host);

    JLabel portLabel = new JLabel("Port:", JLabel.TRAILING);
    inputPanel.add(portLabel);
    inputPanel.add(port);
    portLabel.setLabelFor(port);
      
    mainPanel.add(inputPanel, BorderLayout.CENTER);

    JButton connectButton = new JButton("Connect", UIUtils.getIcon("silk_icons/connect.png"));
    connectButton.addActionListener(this);
    mainPanel.add(connectButton, BorderLayout.SOUTH);
    getRootPane().setDefaultButton(connectButton);
    
    setIconImage(UIUtils.getIcon("silk_icons/server_connect.png").getImage());
    setContentPane(mainPanel);

    setLocationRelativeTo(getParent());
    pack();
    setResizable(false);
  }

  public Prover getProver() {
    if (prover != null && prover.isConnected())
      return prover;
    else
      return null;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
			prover = new HolLight(host.getText(), Integer.parseInt(port.getText()));
			prover.start();
      dispose();
		} catch (/*UserError*/ Exception e1) {
			handler.handleException(e1);
			return;
		}
  }

	
}
