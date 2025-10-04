package ch.randelshofer.geom3d;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import ch.randelshofer.util.Comparable;


public class Face3D implements Comparable
{
    private double[] coords;

    private int[] vertices;

    private double zAvg;

    private Color[] colors;

    private ActionListener action;

    private Point3D normal;

    public Face3D(double[] coords, int[] vertices, Color[] colors)
    {
        this.coords = coords;
        this.vertices = vertices;
        this.colors = colors;
        updateValues();
    }

    private void updateValues()
    {
        this.zAvg = 0.0d;
        for (int vertex : this.vertices)
        {
            this.zAvg += this.coords[(vertex * 3) + 2];
        }
        this.zAvg /= this.vertices.length;
        int i0 = this.vertices[0] * 3;
        int i1 = this.vertices[1] * 3;
        int i2 = this.vertices[2] * 3;
        double d1 = this.coords[i1] - this.coords[i0];
        double d2 = this.coords[i1 + 1] - this.coords[i0 + 1];
        double d3 = this.coords[i1 + 2] - this.coords[i0 + 2];
        double d4 = this.coords[i2] - this.coords[i0];
        double d5 = this.coords[i2 + 1] - this.coords[i0 + 1];
        double d6 = this.coords[i2 + 2] - this.coords[i0 + 2];
        this.normal = new Point3D((d2 * d6) - (d3 * d5), (d3 * d4) - (d1 * d6), (d1 * d5) - (d2 * d4));
    }

    public Color getBorderColor()
    {
        return this.colors[1];
    }

    public Color getFillColor()
    {
        return this.colors[0];
    }

    public void setAction(ActionListener actionListener)
    {
        this.action = actionListener;
    }

    public boolean handleEvent(MouseEvent mouseEvent)
    {
        if (this.action == null)
        {
            return true;
        }
        this.action.actionPerformed(new ActionEvent(mouseEvent, 1001, (String)null, mouseEvent.getModifiersEx()));
        return true;
    }

    public ActionListener getAction()
    {
        return this.action;
    }

    public boolean isVisible(Point3D point3D)
    {
        return ((((this.coords[this.vertices[0] * 3]) - point3D.x) * this.normal.x)
                + (((this.coords[(this.vertices[0] * 3) + 1]) - point3D.y) * this.normal.y))
               + (((this.coords[(this.vertices[0] * 3) + 2]) - point3D.z) * this.normal.z) > 0.0d;
    }

    public int[] getVertices()
    {
        return this.vertices;
    }

    public double[] getCoords()
    {
        return this.coords;
    }

    public void setCoords(double[] coords)
    {
        this.coords = coords;
        updateValues();
    }

    public double getBrightness(Point3D point3D, double sourceIntensity, double ambientIntensity)
    {
        getNormal();
        double x = point3D.x - this.normal.x;
        double y = point3D.y - this.normal.y;
        double z = point3D.z - this.normal.z;
        double dSqrt = (((this.normal.x * x) + (this.normal.y * y)) + (this.normal.z * z))
                       / Math.sqrt((((this.normal.x * this.normal.x) + (this.normal.y * this.normal.y))
                                    + (this.normal.z * this.normal.z))
                                   * (((x * x) + (y * y)) + (z * z)));
        return dSqrt < 0.0d ? ambientIntensity - (dSqrt * sourceIntensity) : ambientIntensity;
    }

    @Override
    public int compareTo(Object obj)
    {
        double d = this.zAvg - ((Face3D)obj).zAvg;
        if (d > 0.0d)
        {
            return 1;
        }
        return d < 0.0d ? -1 : 0;
    }

    private Point3D getNormal()
    {
        return this.normal;
    }
}
