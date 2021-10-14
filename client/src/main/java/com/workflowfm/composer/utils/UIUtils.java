package com.workflowfm.composer.utils;

import javax.swing.*;

public class UIUtils {
    public static JLabel createImageLabel(String name, String icon)
    {
        JLabel j = new JLabel(name);
        j.setIcon(getIcon(icon));
        return j;
    }

    public static ImageIcon getIcon(String name)
    {
        return new ImageIcon(Utils.class.getClassLoader().getResource(name));
    }
}
