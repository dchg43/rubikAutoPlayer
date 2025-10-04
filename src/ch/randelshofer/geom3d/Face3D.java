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

    public Face3D(double[] fArr, int[] iArr, Color[] colorArr)
    {
        this.coords = fArr;
        this.vertices = iArr;
        this.colors = colorArr;
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
        int i2 = this.vertices[0] * 3;
        int i3 = this.vertices[1] * 3;
        int i4 = this.vertices[2] * 3;
        double d1 = this.coords[i3] - this.coords[i2];
        double d2 = this.coords[i3 + 1] - this.coords[i2 + 1];
        double d3 = this.coords[i3 + 2] - this.coords[i2 + 2];
        double d4 = this.coords[i4] - this.coords[i2];
        double d5 = this.coords[i4 + 1] - this.coords[i2 + 1];
        double d6 = this.coords[i4 + 2] - this.coords[i2 + 2];
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

    public void setCoords(double[] fArr)
    {
        this.coords = fArr;
        updateValues();
    }

    public double getBrightness(Point3D point3D, double d, double d2)
    {
        getNormal();
        double d3 = point3D.x - this.normal.x;
        double d4 = point3D.y - this.normal.y;
        double d5 = point3D.z - this.normal.z;
        double dSqrt = (((this.normal.x * d3) + (this.normal.y * d4)) + (this.normal.z * d5))
                       / Math.sqrt((((this.normal.x * this.normal.x) + (this.normal.y * this.normal.y))
                                    + (this.normal.z * this.normal.z))
                                   * (((d3 * d3) + (d4 * d4)) + (d5 * d5)));
        return dSqrt < 0.0d ? d2 - (dSqrt * d) : d2;
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
