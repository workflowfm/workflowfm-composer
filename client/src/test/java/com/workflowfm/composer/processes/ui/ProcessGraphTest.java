package com.workflowfm.composer.processes.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.google.gson.JsonSyntaxException;
import com.workflowfm.composer.exceptions.UserError;
import com.workflowfm.composer.processes.CProcess;
import com.workflowfm.composer.processes.ui.ProcessGraph;

public class ProcessGraphTest extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1085462473352210084L;

	private JPanel panel = new JPanel();
	private ProcessGraph graph = new ProcessGraph();

	public ProcessGraphTest()
	{
		//workspace.addAction("CreateAtomicProcessAction", new CreateAtomicProcessAction());
	}

	private void setupGraph()
	{
		// Shift the viewport slightly so initially added nodes aren't cropped
		//workspace.getGraph().getView().setTranslate(new mxPoint(15, 15));

	}

	private static void setLookAndFeel()
	{
		// Enable antialiasing for Swing
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");

		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setup()
	{
		System.err.println("Setting up GUI.");

		final JFrame frame = this;
		frame.setSize(1200, 800);

		setupGraph();

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(1200, 800));
		panel.setBackground(Color.WHITE);
		panel.add(graph.getGraphEngine().getGraphComponent());
		panel.setVisible(true);
		
		frame.add(panel);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent winEv)
			{
				System.exit(0);
			}
		});
		frame.pack();
		frame.setVisible(true);
	}

	public void drawProcessFromJson(String json) {
		try {
			CProcess process = CProcess.fromJson(json);
			if (process != null) {
				graph.createProcessGraph(process);
				graph.createProcessGraph(process,true);
				graph.layout();
			}
		} catch (JsonSyntaxException | UserError e) {
			e.printStackTrace();
		}

	}		


	public static void main(String args[])
	{
		setLookAndFeel();
		ProcessGraphTest gui = new ProcessGraphTest();
		gui.setup();

		// Proc.create "Pa" [`A:LinProp`;`B:LinProp`] `C:LinProp`;;
		gui.drawProcessFromJson("{\n  \"name\": \"Pa\",\n  \"inputs\": [\n    { \"channel\": \"cPa_A_1\", \"cll\": { \"type\": \"var\", \"name\": \"A\" } },\n    { \"channel\": \"cPa_B_2\", \"cll\": { \"type\": \"var\", \"name\": \"B\" } }\n  ],\n  \"output\": { \"channel\": \"oPa_C_\", \"cll\": { \"type\": \"var\", \"name\": \"C\" } },\n  \"proc\":\n    \"Pa (cPa_A_1,cPa_B_2,oPa_C_) =\\nComp (In cPa_A_1 [cPa_A_1__a_A] Zero)\\n(Comp (In cPa_B_2 [cPa_B_2__a_B] Zero)\\n(Res [oPa_C___a_C] (Out oPa_C_ [oPa_C___a_C] Zero)))\",\n  \"atomic\": true,\n  \"copier\": false,\n  \"intermediate\": false\n}");
		// Proc.create "Px" [`A ++ C`;`B:LinProp`;`D ++ (E ** G)`] `(C ** G) ++ (H ** R ** S) ++ AA`;;
		gui.drawProcessFromJson("{\n  \"name\": \"Px\",\n  \"inputs\": [\n    {\n      \"channel\": \"cPx_lB_A_Plus_C_rB_1\",\n      \"cll\": {\n        \"type\": \"plus\",\n        \"args\": [\n          { \"type\": \"var\", \"name\": \"A\" },\n          { \"type\": \"var\", \"name\": \"C\" }\n        ]\n      }\n    },\n    { \"channel\": \"cPx_B_2\", \"cll\": { \"type\": \"var\", \"name\": \"B\" } },\n    {\n      \"channel\": \"cPx_lB_D_Plus_lB_E_x_G_rB_rB_3\",\n      \"cll\": {\n        \"type\": \"plus\",\n        \"args\": [\n          { \"type\": \"var\", \"name\": \"D\" },\n          {\n            \"type\": \"times\",\n            \"args\": [\n              { \"type\": \"var\", \"name\": \"E\" },\n              { \"type\": \"var\", \"name\": \"G\" }\n            ]\n          }\n        ]\n      }\n    }\n  ],\n  \"output\": {\n    \"channel\":\n      \"oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB_\",\n    \"cll\": {\n      \"type\": \"plus\",\n      \"args\": [\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"C\" },\n            { \"type\": \"var\", \"name\": \"G\" }\n          ]\n        },\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"H\" },\n            { \"type\": \"var\", \"name\": \"R\" },\n            { \"type\": \"var\", \"name\": \"S\" }\n          ]\n        },\n        { \"type\": \"var\", \"name\": \"AA\" }\n      ]\n    }\n  },\n  \"proc\":\n    \"Px\\n(cPx_lB_A_Plus_C_rB_1,\\n cPx_B_2,\\n cPx_lB_D_Plus_lB_E_x_G_rB_rB_3,\\n oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB_) =\\nComp\\n(Res [cPx_lB_A_Plus_C_rB_1__opt_A; cPx_lB_A_Plus_C_rB_1__opt_C]\\n(Out cPx_lB_A_Plus_C_rB_1\\n [cPx_lB_A_Plus_C_rB_1__opt_A; cPx_lB_A_Plus_C_rB_1__opt_C]\\n(Plus\\n (In cPx_lB_A_Plus_C_rB_1__opt_A [cPx_lB_A_Plus_C_rB_1_A]\\n (In cPx_lB_A_Plus_C_rB_1_A [cPx_lB_A_Plus_C_rB_1__a_A] Zero))\\n(In cPx_lB_A_Plus_C_rB_1__opt_C [cPx_lB_A_Plus_C_rB_1_C]\\n(In cPx_lB_A_Plus_C_rB_1_C [cPx_lB_A_Plus_C_rB_1__a_C] Zero)))))\\n(Comp (In cPx_B_2 [cPx_B_2__a_B] Zero)\\n(Comp\\n (Res\\n  [cPx_lB_D_Plus_lB_E_x_G_rB_rB_3__opt_D; cPx_lB_D_Plus_lB_E_x_G_rB_rB_3__opt_lB_E_Par_G_rB]\\n (Out cPx_lB_D_Plus_lB_E_x_G_rB_rB_3\\n  [cPx_lB_D_Plus_lB_E_x_G_rB_rB_3__opt_D; cPx_lB_D_Plus_lB_E_x_G_rB_rB_3__opt_lB_E_Par_G_rB]\\n (Plus\\n  (In cPx_lB_D_Plus_lB_E_x_G_rB_rB_3__opt_D\\n   [cPx_lB_D_Plus_lB_E_x_G_rB_rB_3_D]\\n  (In cPx_lB_D_Plus_lB_E_x_G_rB_rB_3_D [cPx_lB_D_Plus_lB_E_x_G_rB_rB_3__a_D]\\n  Zero))\\n (In cPx_lB_D_Plus_lB_E_x_G_rB_rB_3__opt_lB_E_Par_G_rB\\n  [cPx_lB_D_Plus_lB_E_x_G_rB_rB_3_lB_E_Par_G_rB]\\n (In cPx_lB_D_Plus_lB_E_x_G_rB_rB_3_lB_E_Par_G_rB\\n  [cPx_lB_D_Plus_lB_E_x_G_rB_rB_3_E; cPx_lB_D_Plus_lB_E_x_G_rB_rB_3_G]\\n (Comp\\n  (In cPx_lB_D_Plus_lB_E_x_G_rB_rB_3_E [cPx_lB_D_Plus_lB_E_x_G_rB_rB_3__a_E]\\n  Zero)\\n (In cPx_lB_D_Plus_lB_E_x_G_rB_rB_3_G [cPx_lB_D_Plus_lB_E_x_G_rB_rB_3__a_G]\\n Zero)))))))\\n(Res\\n [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_C_x_G_rB; oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB]\\n(In oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB_\\n [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___opt_lB_C_x_G_rB; oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___opt_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB]\\n(Plus\\n (Out\\n  oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___opt_lB_C_x_G_rB\\n  [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_C_x_G_rB]\\n (Res\\n  [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__C; oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__G]\\n (Out\\n  oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_C_x_G_rB\\n  [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__C; oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__G]\\n (Comp\\n  (Res [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_C]\\n  (Out oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__C\\n   [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_C]\\n  Zero))\\n (Res [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_G]\\n (Out oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__G\\n  [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_G]\\n Zero))))))\\n(Out\\n oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___opt_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB\\n [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB]\\n(Res\\n [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_H_x_lB_R_x_S_rB_rB; oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__AA]\\n(In\\n oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB\\n [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___opt_lB_H_x_lB_R_x_S_rB_rB; oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___opt_AA]\\n(Plus\\n (Out\\n  oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___opt_lB_H_x_lB_R_x_S_rB_rB\\n  [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_H_x_lB_R_x_S_rB_rB]\\n (Res\\n  [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__H; oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_R_x_S_rB]\\n (Out\\n  oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_H_x_lB_R_x_S_rB_rB\\n  [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__H; oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_R_x_S_rB]\\n (Comp\\n  (Res [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_H]\\n  (Out oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__H\\n   [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_H]\\n  Zero))\\n (Res\\n  [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__R; oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__S]\\n (Out\\n  oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__lB_R_x_S_rB\\n  [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__R; oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__S]\\n (Comp\\n  (Res [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_R]\\n  (Out oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__R\\n   [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_R]\\n  Zero))\\n (Res [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_S]\\n (Out oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__S\\n  [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_S]\\n Zero)))))))))\\n(Out oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___opt_AA\\n [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__AA]\\n(Res [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_AA]\\n(Out oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB__AA\\n [oPx_lB_lB_C_x_G_rB_Plus_lB_lB_H_x_lB_R_x_S_rB_rB_Plus_AA_rB_rB___a_AA]\\nZero))))))))))))\",\n  \"atomic\": true,\n  \"copier\": false,\n  \"intermediate\": false\n}");
		// Proc.create "Py" [`A ++ C`] `(A ** G) ** (B ++ C) ++ R`;;
		gui.drawProcessFromJson("{\n  \"name\": \"Py\",\n  \"inputs\": [\n    {\n      \"channel\": \"cPy_lB_A_Plus_C_rB_1\",\n      \"cll\": {\n        \"type\": \"plus\",\n        \"args\": [\n          { \"type\": \"var\", \"name\": \"A\" },\n          { \"type\": \"var\", \"name\": \"C\" }\n        ]\n      }\n    }\n  ],\n  \"output\": {\n    \"channel\": \"oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB_\",\n    \"cll\": {\n      \"type\": \"times\",\n      \"args\": [\n        {\n          \"type\": \"times\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"A\" },\n            { \"type\": \"var\", \"name\": \"G\" }\n          ]\n        },\n        {\n          \"type\": \"plus\",\n          \"args\": [\n            {\n              \"type\": \"plus\",\n              \"args\": [\n                { \"type\": \"var\", \"name\": \"B\" },\n                { \"type\": \"var\", \"name\": \"C\" }\n              ]\n            },\n            { \"type\": \"var\", \"name\": \"R\" }\n          ]\n        }\n      ]\n    }\n  },\n  \"proc\":\n    \"Py\\n(cPy_lB_A_Plus_C_rB_1,oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB_) =\\nComp\\n(Res [cPy_lB_A_Plus_C_rB_1__opt_A; cPy_lB_A_Plus_C_rB_1__opt_C]\\n(Out cPy_lB_A_Plus_C_rB_1\\n [cPy_lB_A_Plus_C_rB_1__opt_A; cPy_lB_A_Plus_C_rB_1__opt_C]\\n(Plus\\n (In cPy_lB_A_Plus_C_rB_1__opt_A [cPy_lB_A_Plus_C_rB_1_A]\\n (In cPy_lB_A_Plus_C_rB_1_A [cPy_lB_A_Plus_C_rB_1__a_A] Zero))\\n(In cPy_lB_A_Plus_C_rB_1__opt_C [cPy_lB_A_Plus_C_rB_1_C]\\n(In cPy_lB_A_Plus_C_rB_1_C [cPy_lB_A_Plus_C_rB_1__a_C] Zero)))))\\n(Res\\n [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__lB_A_x_G_rB; oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__lB_lB_B_Plus_C_rB_Plus_R_rB]\\n(Out oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB_\\n [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__lB_A_x_G_rB; oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__lB_lB_B_Plus_C_rB_Plus_R_rB]\\n(Comp\\n (Res\\n  [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__A; oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__G]\\n (Out oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__lB_A_x_G_rB\\n  [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__A; oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__G]\\n (Comp\\n  (Res [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___a_A]\\n  (Out oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__A\\n   [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___a_A]\\n  Zero))\\n (Res [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___a_G]\\n (Out oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__G\\n  [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___a_G]\\n Zero)))))\\n(Res\\n [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__lB_B_Plus_C_rB; oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__R]\\n(In\\n oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__lB_lB_B_Plus_C_rB_Plus_R_rB\\n [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___opt_lB_B_Plus_C_rB; oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___opt_R]\\n(Plus\\n (Out\\n  oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___opt_lB_B_Plus_C_rB\\n  [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__lB_B_Plus_C_rB]\\n (Res\\n  [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__B; oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__C]\\n (In oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__lB_B_Plus_C_rB\\n  [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___opt_B; oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___opt_C]\\n (Plus\\n  (Out oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___opt_B\\n   [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__B]\\n  (Res [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___a_B]\\n  (Out oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__B\\n   [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___a_B]\\n  Zero)))\\n (Out oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___opt_C\\n  [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__C]\\n (Res [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___a_C]\\n (Out oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__C\\n  [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___a_C]\\n Zero)))))))\\n(Out oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___opt_R\\n [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__R]\\n(Res [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___a_R]\\n(Out oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB__R\\n [oPy_lB_lB_A_x_G_rB_x_lB_lB_B_Plus_C_rB_Plus_R_rB_rB___a_R]\\nZero)))))))))\",\n  \"atomic\": true,\n  \"copier\": false,\n  \"intermediate\": false\n}");
		// Proc.create "Pz" [`A ++ C`] `A ** G ** (B ++ C ++ R)`;;
		gui.drawProcessFromJson("{\n  \"name\": \"Pz\",\n  \"inputs\": [\n    {\n      \"channel\": \"cPz_lB_A_Plus_C_rB_1\",\n      \"cll\": {\n        \"type\": \"plus\",\n        \"args\": [\n          { \"type\": \"var\", \"name\": \"A\" },\n          { \"type\": \"var\", \"name\": \"C\" }\n        ]\n      }\n    }\n  ],\n  \"output\": {\n    \"channel\": \"oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB_\",\n    \"cll\": {\n      \"type\": \"times\",\n      \"args\": [\n        { \"type\": \"var\", \"name\": \"A\" },\n        { \"type\": \"var\", \"name\": \"G\" },\n        {\n          \"type\": \"plus\",\n          \"args\": [\n            { \"type\": \"var\", \"name\": \"B\" },\n            { \"type\": \"var\", \"name\": \"C\" },\n            { \"type\": \"var\", \"name\": \"R\" }\n          ]\n        }\n      ]\n    }\n  },\n  \"proc\":\n    \"Pz\\n(cPz_lB_A_Plus_C_rB_1,oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB_) =\\nComp\\n(Res [cPz_lB_A_Plus_C_rB_1__opt_A; cPz_lB_A_Plus_C_rB_1__opt_C]\\n(Out cPz_lB_A_Plus_C_rB_1\\n [cPz_lB_A_Plus_C_rB_1__opt_A; cPz_lB_A_Plus_C_rB_1__opt_C]\\n(Plus\\n (In cPz_lB_A_Plus_C_rB_1__opt_A [cPz_lB_A_Plus_C_rB_1_A]\\n (In cPz_lB_A_Plus_C_rB_1_A [cPz_lB_A_Plus_C_rB_1__a_A] Zero))\\n(In cPz_lB_A_Plus_C_rB_1__opt_C [cPz_lB_A_Plus_C_rB_1_C]\\n(In cPz_lB_A_Plus_C_rB_1_C [cPz_lB_A_Plus_C_rB_1__a_C] Zero)))))\\n(Res\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__A; oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB]\\n(Out oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB_\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__A; oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB]\\n(Comp\\n (Res [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___a_A]\\n (Out oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__A\\n  [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___a_A]\\n Zero))\\n(Res\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__G; oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__lB_B_Plus_lB_C_Plus_R_rB_rB]\\n(Out\\n oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__G; oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__lB_B_Plus_lB_C_Plus_R_rB_rB]\\n(Comp\\n (Res [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___a_G]\\n (Out oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__G\\n  [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___a_G]\\n Zero))\\n(Res\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__B; oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__lB_C_Plus_R_rB]\\n(In\\n oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__lB_B_Plus_lB_C_Plus_R_rB_rB\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___opt_B; oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___opt_lB_C_Plus_R_rB]\\n(Plus\\n (Out oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___opt_B\\n  [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__B]\\n (Res [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___a_B]\\n (Out oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__B\\n  [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___a_B]\\n Zero)))\\n(Out oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___opt_lB_C_Plus_R_rB\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__lB_C_Plus_R_rB]\\n(Res\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__C; oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__R]\\n(In oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__lB_C_Plus_R_rB\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___opt_C; oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___opt_R]\\n(Plus\\n (Out oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___opt_C\\n  [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__C]\\n (Res [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___a_C]\\n (Out oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__C\\n  [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___a_C]\\n Zero)))\\n(Out oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___opt_R\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__R]\\n(Res [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___a_R]\\n(Out oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB__R\\n [oPz_lB_A_x_lB_G_x_lB_B_Plus_lB_C_Plus_R_rB_rB_rB_rB___a_R]\\nZero))))))))))))))))\",\n  \"atomic\": true,\n  \"copier\": false,\n  \"intermediate\": false\n}");
	}


}
