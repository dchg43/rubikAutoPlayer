package ch.randelshofer.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class RatioLayout implements LayoutManager {
    private double ratio;

    public RatioLayout() {
        this.ratio = 0.5d;
    }

    public RatioLayout(double ratio) {
        this.ratio = ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public void layoutContainer(Container parent) {
        Dimension size = parent.getSize();
        int i = (int) (size.width * this.ratio);
        int iMin = Math.min(parent.getComponentCount(), 2);
        for (int i2 = 0; i2 < iMin; i2++) {
            Component component = parent.getComponent(i2);
            if (i2 == 0) {
                component.setBounds(0, 0, i, size.height);
            } else {
                component.setBounds(i, 0, size.width - i, size.height);
            }
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Dimension dimension = new Dimension();
        int count = Math.min(parent.getComponentCount(), 2);
        for (int i = 0; i < count; i++) {
            Dimension minimumSize = parent.getComponent(i).getMinimumSize();
            dimension.height = Math.max(dimension.height, minimumSize.height);
            dimension.width += minimumSize.width;
        }
        return dimension;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dimension = new Dimension();
        int count = Math.min(parent.getComponentCount(), 2);
        for (int i = 0; i < count; i++) {
            Dimension preferredSize = parent.getComponent(i).getPreferredSize();
            dimension.height = Math.max(dimension.height, preferredSize.height);
            dimension.width += preferredSize.width;
        }
        return dimension;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }
}
