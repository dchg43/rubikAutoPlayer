package ch.randelshofer.gui;

import java.awt.Component;
import java.awt.Graphics;

public interface Icon {
    void paintIcon(Component component, Graphics graphics, int x, int y);

    int getIconWidth();

    int getIconHeight();
}
