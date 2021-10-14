package com.workflowfm.composer.ui;

import com.workflowfm.composer.utils.UIUtils;

import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/** Provides a more concise way to initialise abstract actions. */
public abstract class UIAction extends AbstractAction
{
  private static final long serialVersionUID = 1666759706338456097L;
  private static final ImageIcon missingIcon = UIUtils.getIcon("silk_icons/cross.png");

  public UIAction(String actionName, String iconFilename, int mnemonicKey)
  {
    super(actionName, iconFilename == null ? missingIcon : UIUtils.getIcon(iconFilename));
    putValue(SHORT_DESCRIPTION, actionName);
    if (mnemonicKey > 0) putValue(MNEMONIC_KEY, new Integer(mnemonicKey));
  }

  public UIAction(String actionName, String iconFilename, int mnemonicKey, int acceleratorKey, int acceleratorKeyModifier)
  {
    this(actionName, iconFilename, mnemonicKey);
    putValue(SHORT_DESCRIPTION, actionName);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(acceleratorKey, acceleratorKeyModifier));
  }
  
  public UIAction(String actionName, URL iconURL, int mnemonicKey)
  {
    super(actionName, iconURL == null ? missingIcon : new ImageIcon(iconURL));
    putValue(SHORT_DESCRIPTION, actionName);
    if (mnemonicKey > 0) putValue(MNEMONIC_KEY, new Integer(mnemonicKey));
  }

  public UIAction(String actionName, URL iconURL, int mnemonicKey, int acceleratorKey, int acceleratorKeyModifier)
  {
    this(actionName, iconURL, mnemonicKey);
    putValue(SHORT_DESCRIPTION, actionName);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(acceleratorKey, acceleratorKeyModifier));
  }
  
	public void setDescription(String description) {
		putValue(SHORT_DESCRIPTION, description);
	}
	
	public String getDescription() {
		Object val = getValue(SHORT_DESCRIPTION);
		if (val != null && val instanceof String)
			return (String)val;
		else 
			return "";
	}
}