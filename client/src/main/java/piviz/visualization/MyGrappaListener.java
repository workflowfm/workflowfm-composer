package piviz.visualization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.JPopupMenu;

import piviz.att.grappa.Edge;
import piviz.att.grappa.Element;
import piviz.att.grappa.Grappa;
import piviz.att.grappa.GrappaAdapter;
import piviz.att.grappa.GrappaBox;
import piviz.att.grappa.GrappaConstants;
import piviz.att.grappa.GrappaListener;
import piviz.att.grappa.GrappaPanel;
import piviz.att.grappa.GrappaPoint;
import piviz.att.grappa.Node;
import piviz.att.grappa.Subgraph;

/**
 * Implementation of the visualizers action handling.
 * 
 * @author Anja
 * 
 */
public class MyGrappaListener implements GrappaListener, GrappaConstants,
		ActionListener {

	// /
	private Visualizer parent;
	
	private GraphListener graphListener;
	
	/**
	 * 
	 * @param parent
	 */
	public MyGrappaListener(Visualizer parent) {
		this.parent = parent;
	}

	/**
	 * Handle users popup menu selection.
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String cmd = e.getActionCommand();

		Object invoker = ((javax.swing.JPopupMenu) (((javax.swing.JMenuItem) source)
				.getParent())).getInvoker();
		if (invoker instanceof GrappaPanel) {
			GrappaPanel gp = (GrappaPanel) invoker;
			Subgraph subg = gp.getSubgraph();
			if (cmd.equals("Print")) {
				PageFormat pf = new PageFormat();
				Rectangle2D bb = subg.getBoundingBox();
				if (bb.getWidth() > bb.getHeight())
					pf.setOrientation(PageFormat.LANDSCAPE);
				try {
					PrinterJob printJob = PrinterJob.getPrinterJob();
					printJob.setPrintable(gp, pf);
					if (printJob.printDialog()) {
						printJob.print();
					}
				} catch (Exception ex) {
					Grappa.displayException(ex, "Problem with print request");
				}
			} else if (cmd.equals("Zoom In")) {
				gp.setScaleToFit(false);
				gp.setScaleToSize(null);
				gp.multiplyScaleFactor(1.25);
				gp.clearOutline();

			} else if (cmd.equals("Zoom Out")) {
				gp.setScaleToFit(false);
				gp.setScaleToSize(null);
				gp.multiplyScaleFactor(0.8);
				gp.clearOutline();

			} else if (cmd.equals("Scale to Fit")) {
				gp.setScaleToFit(true);

			} else if (cmd.equals("Original Size")) {
				gp.setScaleToFit(false);
				gp.setScaleToSize(null);
				gp.resetZoom();
				gp.clearOutline();

			}
		}

	}
	
	public void setGraphListener(GraphListener graphListener)
  {
    this.graphListener = graphListener;
  }

	/**
	 * Not needed.
	 */
	public void grappaDragged(Subgraph subg, GrappaPoint currentPt,
			int currentModifiers, Element pressedElem, GrappaPoint pressedPt,
			int pressedModifiers, GrappaBox outline, GrappaPanel panel) {
	}

	/**
	 * Handle selecting an egde for the execution.
	 */
	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {

		if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
			if (elem == null)
				return;
			if (elem.isEdge()) {
			  if (graphListener != null)
        {
          graphListener.edgeClicked((String) elem
          .getAttributeValue(GrappaConstants.LABEL_ATTR));
        }
			  
				parent.executeStepForEdge((Edge) elem);
			}
			if (elem.isNode()) {
			  if (graphListener != null)
			  {
			    graphListener.nodeClicked((String) elem
          .getAttributeValue(GrappaConstants.LABEL_ATTR));
			  }
			  
				// See if this node is a closed subgraph
				Integer shape = (Integer)elem.getAttributeValue(GrappaConstants.SHAPE_ATTR);
				if (shape.intValue() == GrappaConstants.BOX_SHAPE)
					parent.togglePoolVisibility(((String) elem
							.getAttributeValue(GrappaConstants.LABEL_ATTR)));
				else
					parent.executeTauNode((Node) elem);
			}
			if (elem.isSubgraph())
				parent.togglePoolVisibility(((String) elem
						.getAttributeValue(GrappaConstants.LABEL_ATTR)));
		}

	}

	/**
	 * Give the user a nice popup menu with some zooming options.
	 */
	public void grappaPressed(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, GrappaPanel panel) {
		if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
			// pop-up menu if button2
			JPopupMenu popup = new javax.swing.JPopupMenu();
			javax.swing.JMenuItem item = null;
			popup.add(item = new javax.swing.JMenuItem("Print"));
			item.setActionCommand("Print");
			item.addActionListener(this);
			popup.addSeparator();

			popup.add(item = new javax.swing.JMenuItem("Zoom In"));
			item.setActionCommand("Zoom In");
			item.addActionListener(this);
			popup.add(item = new javax.swing.JMenuItem("Zoom Out"));
			item.setActionCommand("Zoom Out");
			item.addActionListener(this);
			popup.add(item = new javax.swing.JMenuItem("Original Size"));
			item.setActionCommand("Original Size");
			item.addActionListener(this);
			popup.add(item = new javax.swing.JMenuItem("Scale to Fit"));
			item.setActionCommand("Scale to Fit");
			item.addActionListener(this);

			Point2D mpt = panel.getTransform().transform(pt, null);
			popup.show(panel, (int) mpt.getX(), (int) mpt.getY());

		}

	}

	/**
	 * Not needed.
	 */
	public void grappaReleased(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, Element pressedElem, GrappaPoint pressedPt,
			int pressedModifiers, GrappaBox outline, GrappaPanel panel) {
	}

	/**
	 * Not needed.
	 */
	public String grappaTip(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, GrappaPanel panel) {
		GrappaAdapter adapter = new GrappaAdapter();

		return adapter.grappaTip(subg, elem, pt, modifiers, panel);

	}

}
