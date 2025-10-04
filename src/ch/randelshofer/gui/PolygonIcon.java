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

    public PolygonIcon(Polygon[] polygons, Dimension size)
    {
        this.polygons = polygons;
        this.size = size;
    }

    public PolygonIcon(Polygon polygon, Dimension size)
    {
        this.polygons = new Polygon[] {polygon};
        this.size = size;
    }

    public void setForeground(Color color)
    {
        this.color = color;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y)
    {
        graphics.setColor(
            component.isEnabled() ? this.color != null ? this.color : component.getForeground() : Color.gray);
        graphics.translate(x, y);
        if (this.polygons != null)
        {
            for (Polygon polygon : this.polygons)
            {
                graphics.fillPolygon(polygon);
                graphics.drawPolygon(polygon);
            }
        }
        graphics.translate(-x, -y);
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
