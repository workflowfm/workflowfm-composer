package com.workflowfm.composer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.workflowfm.composer.edit.CompositionBuilder;
import com.workflowfm.composer.edit.graph.AddProcessGraphEdit;
import com.workflowfm.composer.exceptions.ComponentExceptionHandler;
import com.workflowfm.composer.exceptions.ExceptionHandler;
import com.workflowfm.composer.exceptions.NotFoundException;
import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ui.ProcessGraph;
import com.workflowfm.composer.prover.HolLight;
import com.workflowfm.composer.prover.Prover;
import com.workflowfm.composer.prover.response.ComposeResponse;
import com.workflowfm.composer.prover.response.CreateProcessResponse;
import com.workflowfm.composer.prover.response.ProverResponse;
import com.workflowfm.composer.session.CompositionSession;
import com.workflowfm.composer.ui.dialogs.ExportImageDialog;
import com.workflowfm.composer.workspace.Workspace;

public class JsonFileLauncher {

	private CompositionSession session;
	private Workspace workspace;
	private Prover prover;
	
	private ProcessGraph graph;
	private ExceptionHandler exceptionHandler;
	
	public JsonFileLauncher () {
		this.prover = new HolLight();
		reset();
	};

	public JFrame setup(String name)
	{
		System.err.println("Setting up GUI.");
	
		final JFrame frame = new JFrame();
		frame.setTitle(name);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(1200, 800));
		panel.setBackground(Color.WHITE);
		panel.add(graph.getGraphEngine().getGraphComponent(), BorderLayout.CENTER);
		panel.setVisible(true);
		
		JButton button = new JButton("Export");
		button.setMnemonic(KeyEvent.VK_ENTER);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ExportImageDialog(panel, graph, exceptionHandler).show();				
			}
		});
		panel.add(button,BorderLayout.PAGE_END);
		
		frame.add(panel);				
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

	public void loadFile(String file) { loadFile(new File(file)); } 
	
	public void loadFile(File file) {
		reset();
		setup(file.getName());
		parseFile(file);
	}
	
	
	public void reset() {
		this.session = new CompositionSession(prover);
		this.workspace = session.createWorkspace();
		this.graph = new ProcessGraph();
		this.exceptionHandler = new ComponentExceptionHandler(graph.getGraphEngine().getGraphComponent());
	}
	
	private static void setLookAndFeel()
	{
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// Enable antialiasing for Swing
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
	}

	public void parseJson (String json) {
		JsonArray array = new JsonParser().parse(json).getAsJsonArray();
		Vector<String> responseStrings = new Vector<String>();

		for (JsonElement e : array) {
			responseStrings.add(e.toString());
		}
		try {
			Vector<ProverResponse> responses = prover.parseResponses(responseStrings);
			Vector<ProverResponse> compositions = new Vector<ProverResponse> ();
			for (ProverResponse r : responses) {
				System.out.println("*** Handling response: " + r.debugString());
				if (r instanceof CreateProcessResponse)
					handleCreate((CreateProcessResponse)r);
				if (r instanceof ComposeResponse) {
					compositions.add(r);
					ComposeResponse cr = (ComposeResponse)r; 
					handleCompose(cr);
					if (!cr.getAction().getResult().startsWith("_Step")) {
						// A Composition concluded!
						handleStore(cr.getAction().getResult(),compositions);
						compositions = new Vector<ProverResponse> ();
						this.workspace = session.createWorkspace();
					}
					
				}
			}
		} catch (UserError e1) {
			exceptionHandler.handleException(e1);
		}
	}
	
	public void handleCreate(CreateProcessResponse response) {	
		CProcess process = response.getProcess();
		if (session.processExists(process.getName()))
			try {
				session.updateProcess(process.getName(),process);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		else
			session.addProcess(process);
		new AddProcessGraphEdit(process, session, exceptionHandler, graph).apply(); 
	}

	public void handleCompose(ComposeResponse response) {
		workspace.handleComposeResponse(response, exceptionHandler).apply(false);
		new AddProcessGraphEdit(response.getProcess(), session, exceptionHandler, graph).apply(); 
	}
	
	public void handleStore(String name, Vector<ProverResponse> responses) {
		CompositionBuilder builder = new CompositionBuilder(name,  session, exceptionHandler, true);
		builder.build(responses);	
	}
	
	protected void parseFile(File file) {
		FileInputStream stream = null;
		StringBuffer buffer = new StringBuffer();

		try {
			stream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(stream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String read;
			while ((read = br.readLine()) != null) {
				buffer.append(read + "\n");
			}
			in.close();
			stream.close();
			
			parseJson(buffer.toString());
			
		} catch (FileNotFoundException e) {
			exceptionHandler.handleException(e);
		} catch (IOException e) {
			exceptionHandler.handleException(e);
		}
	}

	public File chooseFile(JFrame frame) throws UserError {
		final JFileChooser fc = new JFileChooser();

		FileFilter filter = new FileNameExtensionFilter("Composer JSON file", new String[] {"json"});
		fc.setFileFilter(filter);
		fc.addChoosableFileFilter(filter);

		if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			return fc.getSelectedFile();
		} else {
			throw new UserError("No file selected...");
		}
	}
		
	public static void main(String args[])
	{
		setLookAndFeel();
		JsonFileLauncher launcher = new JsonFileLauncher();

        //launcher.setup("Toy");
		//String json = "[{\"response\":\"CreateProcess\",\"process\":{\"name\":\"Pa\",\"inputs\":[{\"channel\":\"cPa_X_1\",\"cll\":{\"type\":\"var\",\"name\":\"X\"}}],\"output\":{\"channel\":\"oPa_lB_A_x_lB_B_x_C_rB_rB_\",\"cll\":{\"type\":\"times\",\"args\":[{\"type\":\"var\",\"name\":\"A\"},{\"type\":\"var\",\"name\":\"B\"},{\"type\":\"var\",\"name\":\"C\"}]}},\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pa\"},{\"type\":\"source\",\"name\":\"Pa\"},{\"type\":\"source\",\"name\":\"Pa\"}]},\"proc\":\"Pa (cPa_X_1,oPa_lB_A_x_lB_B_x_C_rB_rB_) =\\nComp (In cPa_X_1 [cPa_X_1__a_X] Zero)\\n(Res [oPa_lB_A_x_lB_B_x_C_rB_rB__A; oPa_lB_A_x_lB_B_x_C_rB_rB__lB_B_x_C_rB]\\n(Out oPa_lB_A_x_lB_B_x_C_rB_rB_\\n [oPa_lB_A_x_lB_B_x_C_rB_rB__A; oPa_lB_A_x_lB_B_x_C_rB_rB__lB_B_x_C_rB]\\n(Comp\\n (Res [oPa_lB_A_x_lB_B_x_C_rB_rB__l_a_A]\\n (Out oPa_lB_A_x_lB_B_x_C_rB_rB__A [oPa_lB_A_x_lB_B_x_C_rB_rB__l_a_A] Zero))\\n(Res [oPa_lB_A_x_lB_B_x_C_rB_rB__rB; oPa_lB_A_x_lB_B_x_C_rB_rB__rC]\\n(Out oPa_lB_A_x_lB_B_x_C_rB_rB__lB_B_x_C_rB\\n [oPa_lB_A_x_lB_B_x_C_rB_rB__rB; oPa_lB_A_x_lB_B_x_C_rB_rB__rC]\\n(Comp\\n (Res [oPa_lB_A_x_lB_B_x_C_rB_rB__rl_a_B]\\n (Out oPa_lB_A_x_lB_B_x_C_rB_rB__rB [oPa_lB_A_x_lB_B_x_C_rB_rB__rl_a_B] Zero))\\n(Res [oPa_lB_A_x_lB_B_x_C_rB_rB__rr_a_C]\\n(Out oPa_lB_A_x_lB_B_x_C_rB_rB__rC [oPa_lB_A_x_lB_B_x_C_rB_rB__rr_a_C] Zero))))))))\",\"actions\":[],\"copier\":false,\"intermediate\":false}},{\"response\":\"CreateProcess\",\"process\":{\"name\":\"Pb\",\"inputs\":[{\"channel\":\"cPb_A_1\",\"cll\":{\"type\":\"var\",\"name\":\"A\"}}],\"output\":{\"channel\":\"oPb_D_\",\"cll\":{\"type\":\"var\",\"name\":\"D\"}},\"prov\":{\"type\":\"source\",\"name\":\"Pb\"},\"proc\":\"Pb (cPb_A_1,oPb_D_) =\\nComp (In cPb_A_1 [cPb_A_1__a_A] Zero)\\n(Res [oPb_D___a_D] (Out oPb_D_ [oPb_D___a_D] Zero))\",\"actions\":[],\"copier\":false,\"intermediate\":false}},{\"response\":\"CreateProcess\",\"process\":{\"name\":\"Pc\",\"inputs\":[{\"channel\":\"cPc_B_1\",\"cll\":{\"type\":\"var\",\"name\":\"B\"}}],\"output\":{\"channel\":\"oPc_E_\",\"cll\":{\"type\":\"var\",\"name\":\"E\"}},\"prov\":{\"type\":\"source\",\"name\":\"Pc\"},\"proc\":\"Pc (cPc_B_1,oPc_E_) =\\nComp (In cPc_B_1 [cPc_B_1__a_B] Zero)\\n(Res [oPc_E___a_E] (Out oPc_E_ [oPc_E___a_E] Zero))\",\"actions\":[],\"copier\":false,\"intermediate\":false}},{\"response\":\"CreateProcess\",\"process\":{\"name\":\"Pd\",\"inputs\":[{\"channel\":\"cPd_C_1\",\"cll\":{\"type\":\"var\",\"name\":\"C\"}}],\"output\":{\"channel\":\"oPd_F_\",\"cll\":{\"type\":\"var\",\"name\":\"F\"}},\"prov\":{\"type\":\"source\",\"name\":\"Pd\"},\"proc\":\"Pd (cPd_C_1,oPd_F_) =\\nComp (In cPd_C_1 [cPd_C_1__a_C] Zero)\\n(Res [oPd_F___a_F] (Out oPd_F_ [oPd_F___a_F] Zero))\",\"actions\":[],\"copier\":false,\"intermediate\":false}},{\"response\":\"Compose\",\"action\":{\"act\":\"JOIN\",\"larg\":\"Pa\",\"lsel\":\"lr\",\"rarg\":\"Pb\",\"rsel\":\"(NEG A)\",\"res\":\"_Step0\"},\"process\":{\"name\":\"_Step0\",\"inputs\":[{\"channel\":\"cPa_X_1\",\"cll\":{\"type\":\"var\",\"name\":\"X\"}}],\"output\":{\"channel\":\"Res___Step0__z11\",\"cll\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"var\",\"name\":\"D\"},{\"type\":\"var\",\"name\":\"B\"}]},{\"type\":\"var\",\"name\":\"C\"}]}},\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pb\"},{\"type\":\"source\",\"name\":\"Pa\"}]},{\"type\":\"source\",\"name\":\"Pa\"}]},\"proc\":\"_Step0 (cPa_X_1,Res___Step0__z11) =\\nPiCutProc (A ** B ** C) Res___Step0__z15 Res___Step0__z14\\noPa_lB_A_x_lB_B_x_C_rB_rB_\\n(PiParProc (NEG A) (NEG (B ** C)) Res___Step0__z14 cPb_A_1 Res___Step0__z13\\n(PiParProc (NEG B) (NEG C) Res___Step0__z13 Res___Step0__buf7\\n Res___Step0__buf11\\n(PiTimesProc (D ** B) C Res___Step0__z11 Res___Step0__z7 Res___Step0__b11\\n (PiTimesProc D B Res___Step0__z7 oPb_D_ Res___Step0__b7\\n  (Pb (cPb_A_1,oPb_D_))\\n (PiIdProc B Res___Step0__buf7 Res___Step0__b7 Res___Step0__m8))\\n(PiIdProc C Res___Step0__buf11 Res___Step0__b11 Res___Step0__m12))))\\n(Pa (cPa_X_1,oPa_lB_A_x_lB_B_x_C_rB_rB_))\",\"actions\":[{\"act\":\"JOIN\",\"larg\":\"Pa\",\"lsel\":\"lr\",\"rarg\":\"Pb\",\"rsel\":\"(NEG A)\",\"res\":\"_Step0\"}],\"copier\":false,\"intermediate\":true},\"state\":{\"label\":\"Res\",\"ctr\":0,\"buffered\":[{\"type\":\"var\",\"name\":\"C\"},{\"type\":\"var\",\"name\":\"B\"}],\"joined\":[{\"channel\":\"cPb_A_1\",\"cll\":{\"type\":\"neg\",\"args\":[{\"type\":\"var\",\"name\":\"A\"}]}}],\"iprov\":[{\"term\":{\"type\":\"times\",\"args\":[{\"type\":\"var\",\"name\":\"A\"},{\"type\":\"var\",\"name\":\"B\"},{\"type\":\"var\",\"name\":\"C\"}]},\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"cPb_A_1:0\"},{\"type\":\"source\",\"name\":\"#\"},{\"type\":\"source\",\"name\":\"#\"}]}}],\"prov\":[{\"name\":\"Pb\",\"prov\":{\"type\":\"source\",\"name\":\"Pb\"}},{\"name\":\"_Step0\",\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pb\"},{\"type\":\"source\",\"name\":\"Pa\"}]},{\"type\":\"source\",\"name\":\"Pa\"}]}},{\"name\":\"Pa\",\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pa\"},{\"type\":\"source\",\"name\":\"Pa\"},{\"type\":\"source\",\"name\":\"Pa\"}]}},{\"name\":\"Pc\",\"prov\":{\"type\":\"source\",\"name\":\"Pc\"}},{\"name\":\"Pd\",\"prov\":{\"type\":\"source\",\"name\":\"Pd\"}}]}},{\"response\":\"Compose\",\"action\":{\"act\":\"JOIN\",\"larg\":\"_Step0\",\"lsel\":\"lrr\",\"rarg\":\"Pc\",\"rsel\":\"(NEG B)\",\"res\":\"_Step1\"},\"process\":{\"name\":\"_Step1\",\"inputs\":[{\"channel\":\"cPa_X_1\",\"cll\":{\"type\":\"var\",\"name\":\"X\"}}],\"output\":{\"channel\":\"Res___Step1__z12\",\"cll\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"var\",\"name\":\"D\"},{\"type\":\"var\",\"name\":\"E\"}]},{\"type\":\"var\",\"name\":\"C\"}]}},\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pb\"},{\"type\":\"source\",\"name\":\"Pc\"}]},{\"type\":\"source\",\"name\":\"Pa\"}]},\"proc\":\"_Step1 (cPa_X_1,Res___Step1__z12) =\\nPiCutProc ((D ** B) ** C) Res___Step1__z15 Res___Step1__z14 Res___Step0__z11\\n(PiParProc (NEG (D ** B)) (NEG C) Res___Step1__z14 Res___Step1__z9\\n Res___Step1__buf12\\n(PiTimesProc (D ** E) C Res___Step1__z12 Res___Step1__z7 Res___Step1__b12\\n (PiParProc (NEG D) (NEG B) Res___Step1__z9 Res___Step1__buf7 cPc_B_1\\n (PiTimesProc D E Res___Step1__z7 Res___Step1__b7 oPc_E_\\n  (PiIdProc D Res___Step1__buf7 Res___Step1__b7 Res___Step1__m8)\\n (Pc (cPc_B_1,oPc_E_))))\\n(PiIdProc C Res___Step1__buf12 Res___Step1__b12 Res___Step1__m13)))\\n(PiCutProc (A ** B ** C) Res___Step0__z15 Res___Step0__z14\\n oPa_lB_A_x_lB_B_x_C_rB_rB_\\n (PiParProc (NEG A) (NEG (B ** C)) Res___Step0__z14 cPb_A_1 Res___Step0__z13\\n (PiParProc (NEG B) (NEG C) Res___Step0__z13 Res___Step0__buf7\\n  Res___Step0__buf11\\n (PiTimesProc (D ** B) C Res___Step0__z11 Res___Step0__z7 Res___Step0__b11\\n  (PiTimesProc D B Res___Step0__z7 oPb_D_ Res___Step0__b7\\n   (Pb (cPb_A_1,oPb_D_))\\n  (PiIdProc B Res___Step0__buf7 Res___Step0__b7 Res___Step0__m8))\\n (PiIdProc C Res___Step0__buf11 Res___Step0__b11 Res___Step0__m12))))\\n(Pa (cPa_X_1,oPa_lB_A_x_lB_B_x_C_rB_rB_)))\",\"actions\":[{\"act\":\"JOIN\",\"larg\":\"_Step0\",\"lsel\":\"lrr\",\"rarg\":\"Pc\",\"rsel\":\"(NEG B)\",\"res\":\"_Step1\"}],\"copier\":false,\"intermediate\":true},\"state\":{\"label\":\"Res\",\"ctr\":0,\"buffered\":[{\"type\":\"var\",\"name\":\"C\"},{\"type\":\"var\",\"name\":\"D\"}],\"joined\":[{\"channel\":\"cPc_B_1\",\"cll\":{\"type\":\"neg\",\"args\":[{\"type\":\"var\",\"name\":\"B\"}]}}],\"iprov\":[{\"term\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"var\",\"name\":\"D\"},{\"type\":\"var\",\"name\":\"B\"}]},{\"type\":\"var\",\"name\":\"C\"}]},\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"#\"},{\"type\":\"source\",\"name\":\"cPc_B_1:2\"}]},{\"type\":\"source\",\"name\":\"#\"}]}},{\"term\":{\"type\":\"times\",\"args\":[{\"type\":\"var\",\"name\":\"A\"},{\"type\":\"var\",\"name\":\"B\"},{\"type\":\"var\",\"name\":\"C\"}]},\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"cPb_A_1:0\"},{\"type\":\"source\",\"name\":\"#\"},{\"type\":\"source\",\"name\":\"#\"}]}}],\"prov\":[{\"name\":\"Pc\",\"prov\":{\"type\":\"source\",\"name\":\"Pc\"}},{\"name\":\"_Step1\",\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pb\"},{\"type\":\"source\",\"name\":\"Pc\"}]},{\"type\":\"source\",\"name\":\"Pa\"}]}},{\"name\":\"Pb\",\"prov\":{\"type\":\"source\",\"name\":\"Pb\"}},{\"name\":\"_Step0\",\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pb\"},{\"type\":\"source\",\"name\":\"Pa\"}]},{\"type\":\"source\",\"name\":\"Pa\"}]}},{\"name\":\"Pa\",\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pa\"},{\"type\":\"source\",\"name\":\"Pa\"},{\"type\":\"source\",\"name\":\"Pa\"}]}},{\"name\":\"Pd\",\"prov\":{\"type\":\"source\",\"name\":\"Pd\"}}]}},{\"response\":\"Compose\",\"action\":{\"act\":\"JOIN\",\"larg\":\"_Step1\",\"lsel\":\"r\",\"rarg\":\"Pd\",\"rsel\":\"(NEG C)\",\"res\":\"Res\"},\"process\":{\"name\":\"Res\",\"inputs\":[{\"channel\":\"cPa_X_1\",\"cll\":{\"type\":\"var\",\"name\":\"X\"}}],\"output\":{\"channel\":\"Res__Res__z11\",\"cll\":{\"type\":\"times\",\"args\":[{\"type\":\"var\",\"name\":\"E\"},{\"type\":\"var\",\"name\":\"D\"},{\"type\":\"var\",\"name\":\"F\"}]}},\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pc\"},{\"type\":\"source\",\"name\":\"Pb\"},{\"type\":\"source\",\"name\":\"Pd\"}]},\"proc\":\"Res (cPa_X_1,Res__Res__z11) =\\nPiCutProc ((D ** E) ** C) Res__Res__z15 Res__Res__z14 Res___Step1__z12\\n(PiParProc (NEG (D ** E)) (NEG C) Res__Res__z14 Res__Res__z13 cPd_C_1\\n(PiParProc (NEG D) (NEG E) Res__Res__z13 Res__Res__buf7 Res__Res__buf11\\n(PiTimesProc E (D ** F) Res__Res__z11 Res__Res__b11 Res__Res__z7\\n (PiIdProc E Res__Res__buf11 Res__Res__b11 Res__Res__m12)\\n(PiTimesProc D F Res__Res__z7 Res__Res__b7 oPd_F_\\n (PiIdProc D Res__Res__buf7 Res__Res__b7 Res__Res__m8)\\n(Pd (cPd_C_1,oPd_F_))))))\\n(PiCutProc ((D ** B) ** C) Res___Step1__z15 Res___Step1__z14 Res___Step0__z11\\n (PiParProc (NEG (D ** B)) (NEG C) Res___Step1__z14 Res___Step1__z9\\n  Res___Step1__buf12\\n (PiTimesProc (D ** E) C Res___Step1__z12 Res___Step1__z7 Res___Step1__b12\\n  (PiParProc (NEG D) (NEG B) Res___Step1__z9 Res___Step1__buf7 cPc_B_1\\n  (PiTimesProc D E Res___Step1__z7 Res___Step1__b7 oPc_E_\\n   (PiIdProc D Res___Step1__buf7 Res___Step1__b7 Res___Step1__m8)\\n  (Pc (cPc_B_1,oPc_E_))))\\n (PiIdProc C Res___Step1__buf12 Res___Step1__b12 Res___Step1__m13)))\\n(PiCutProc (A ** B ** C) Res___Step0__z15 Res___Step0__z14\\n oPa_lB_A_x_lB_B_x_C_rB_rB_\\n (PiParProc (NEG A) (NEG (B ** C)) Res___Step0__z14 cPb_A_1 Res___Step0__z13\\n (PiParProc (NEG B) (NEG C) Res___Step0__z13 Res___Step0__buf7\\n  Res___Step0__buf11\\n (PiTimesProc (D ** B) C Res___Step0__z11 Res___Step0__z7 Res___Step0__b11\\n  (PiTimesProc D B Res___Step0__z7 oPb_D_ Res___Step0__b7\\n   (Pb (cPb_A_1,oPb_D_))\\n  (PiIdProc B Res___Step0__buf7 Res___Step0__b7 Res___Step0__m8))\\n (PiIdProc C Res___Step0__buf11 Res___Step0__b11 Res___Step0__m12))))\\n(Pa (cPa_X_1,oPa_lB_A_x_lB_B_x_C_rB_rB_))))\",\"actions\":[{\"act\":\"JOIN\",\"larg\":\"_Step1\",\"lsel\":\"r\",\"rarg\":\"Pd\",\"rsel\":\"(NEG C)\",\"res\":\"Res\"}],\"copier\":false,\"intermediate\":true},\"state\":{\"label\":\"Res\",\"ctr\":0,\"buffered\":[{\"type\":\"var\",\"name\":\"E\"},{\"type\":\"var\",\"name\":\"D\"}],\"joined\":[{\"channel\":\"cPd_C_1\",\"cll\":{\"type\":\"neg\",\"args\":[{\"type\":\"var\",\"name\":\"C\"}]}}],\"iprov\":[{\"term\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"var\",\"name\":\"D\"},{\"type\":\"var\",\"name\":\"E\"}]},{\"type\":\"var\",\"name\":\"C\"}]},\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"#\"},{\"type\":\"source\",\"name\":\"#\"}]},{\"type\":\"source\",\"name\":\"cPd_C_1:0\"}]}},{\"term\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"var\",\"name\":\"D\"},{\"type\":\"var\",\"name\":\"B\"}]},{\"type\":\"var\",\"name\":\"C\"}]},\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"#\"},{\"type\":\"source\",\"name\":\"cPc_B_1:2\"}]},{\"type\":\"source\",\"name\":\"#\"}]}},{\"term\":{\"type\":\"times\",\"args\":[{\"type\":\"var\",\"name\":\"A\"},{\"type\":\"var\",\"name\":\"B\"},{\"type\":\"var\",\"name\":\"C\"}]},\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"cPb_A_1:0\"},{\"type\":\"source\",\"name\":\"#\"},{\"type\":\"source\",\"name\":\"#\"}]}}],\"prov\":[{\"name\":\"Pd\",\"prov\":{\"type\":\"source\",\"name\":\"Pd\"}},{\"name\":\"Res\",\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pc\"},{\"type\":\"source\",\"name\":\"Pb\"},{\"type\":\"source\",\"name\":\"Pd\"}]}},{\"name\":\"Pc\",\"prov\":{\"type\":\"source\",\"name\":\"Pc\"}},{\"name\":\"_Step1\",\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pb\"},{\"type\":\"source\",\"name\":\"Pc\"}]},{\"type\":\"source\",\"name\":\"Pa\"}]}},{\"name\":\"Pb\",\"prov\":{\"type\":\"source\",\"name\":\"Pb\"}},{\"name\":\"_Step0\",\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pb\"},{\"type\":\"source\",\"name\":\"Pa\"}]},{\"type\":\"source\",\"name\":\"Pa\"}]}},{\"name\":\"Pa\",\"prov\":{\"type\":\"times\",\"args\":[{\"type\":\"source\",\"name\":\"Pa\"},{\"type\":\"source\",\"name\":\"Pa\"},{\"type\":\"source\",\"name\":\"Pa\"}]}}]}}]";
		//launcher.parseJson(json);
		
		//File toy = new File("json/toy.json");
		//launcher.parseFile(toy);
		//launcher.parseFile(toy);
		
		for (String arg : args) 
			launcher.loadFile(arg);
	}
}