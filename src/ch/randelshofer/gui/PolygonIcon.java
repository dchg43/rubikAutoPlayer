package ch.randelshofer.gui;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;


public class PolygonIcon implements Icon
{
    private Polygon[] polygons;

    private Dimension size;

    private Color color;

    public PolygonIcon(Polygon[] polygonArr, Dimension dimension)
    {
        this.polygons = polygonArr;
        this.size = dimension;
    }

    public PolygonIcon(Polygon polygon, Dimension dimension)
    {
        this.polygons = new Polygon[] {polygon};
        this.size = dimension;
    }

    public void setForeground(Color color)
    {
        this.color = color;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int i, int i2)
    {
        graphics.setColor(
            component.isEnabled() ? this.color != null ? this.color : component.getForeground() : Color.gray);
        graphics.translate(i, i2);
        if (this.polygons != null)
        {
            for (Polygon polygon : this.polygons)
            {
                graphics.fillPolygon(polygon);
                graphics.drawPolygon(polygon);
            }
        }
        graphics.translate(-i, -i2);
    }

    @Override
    public int getIconWidth()
    {
        return this.size.width;
    }

    @Override
    public int getIconHeight()
    {
        return this.size.height;
    }
}
