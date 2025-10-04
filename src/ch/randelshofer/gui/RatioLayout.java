package ch.randelshofer.gui;


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;


public class RatioLayout implements LayoutManager
{
    private double ratio;

    public RatioLayout(double f)
    {
        this.ratio = f;
    }

    public RatioLayout()
    {
        this.ratio = 0.5d;
    }

    public void setRatio(double f)
    {
        this.ratio = f;
    }

    @Override
    public void addLayoutComponent(String str, Component component)
    {}

    @Override
    public void layoutContainer(Container container)
    {
        Dimension size = container.getSize();
        int i = (int)(size.width * this.ratio);
        int iMin = Math.min(container.getComponentCount(), 2);
        for (int i2 = 0; i2 < iMin; i2++)
        {
            Component component = container.getComponent(i2);
            if (i2 == 0)
            {
                component.setBounds(0, 0, i, size.height);
            }
            else
            {
                component.setBounds(i, 0, size.width - i, size.height);
            }
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container container)
    {
        Dimension dimension = new Dimension();
        int iMin = Math.min(container.getComponentCount(), 2);
        for (int i = 0; i < iMin; i++)
        {
            Dimension minimumSize = container.getComponent(i).getMinimumSize();
            dimension.height = Math.max(dimension.height, minimumSize.height);
            dimension.width += minimumSize.width;
        }
        return dimension;
    }

    @Override
    public Dimension preferredLayoutSize(Container container)
    {
        Dimension dimension = new Dimension();
        int iMin = Math.min(container.getComponentCount(), 2);
        for (int i = 0; i < iMin; i++)
        {
            Dimension preferredSize = container.getComponent(i).getPreferredSize();
            dimension.height = Math.max(dimension.height, preferredSize.height);
            dimension.width += preferredSize.width;
        }
        return dimension;
    }

    @Override
    public void removeLayoutComponent(Component component)
    {}
}
