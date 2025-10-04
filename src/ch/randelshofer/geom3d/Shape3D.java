package ch.randelshofer.geom3d;


import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.Vector;


public class Shape3D implements SceneNode
{
    private double[] coords;

    private int[][] faces;

    private Color[][] colors;

    private boolean isVisible;

    private boolean isWireframe;

    private boolean isReduced;

    private int reducedFaceCount;

    private Face3D[] faces3D;

    private ActionListener[] faceActions;

    private Transform3D transform;

    public Shape3D(double[] coords, int[][] faces, Color[][] colors)
    {
        this(coords, faces, colors, faces.length);
    }

    public Shape3D(double[] coords, int[][] faces, Color[][] colors, int reducedFaceCount)
    {
        this.isVisible = true;
        this.isWireframe = false;
        this.isReduced = false;
        this.transform = new Transform3D();
        this.coords = coords;
        this.faces = faces;
        this.colors = colors;
        this.reducedFaceCount = reducedFaceCount;
    }

    public double[] getCoords()
    {
        return this.coords;
    }

    public int[][] getFaces()
    {
        return this.faces;
    }

    public boolean isVisible()
    {
        return this.isVisible;
    }

    public void setVisible(boolean isVisible)
    {
        this.isVisible = isVisible;
    }

    public boolean isRecuced()
    {
        return this.isReduced;
    }

    public void setReduced(boolean isReduced)
    {
        this.isReduced = isReduced;
    }

    public void setTransform(Transform3D transform)
    {
        this.transform = transform;
    }

    private void createFaces()
    {
        if (this.faces3D == null)
        {
            this.faces3D = new Face3D[this.faces.length];
            for (int i = 0; i < this.faces.length; i++)
            {
                this.faces3D[i] = new Face3D(this.coords, this.faces[i],
                    this.isWireframe ? new Color[] {null, this.colors[i][1]} : this.colors[i]);
                if (this.faceActions != null)
                {
                    this.faces3D[i].setAction(this.faceActions[i]);
                }
            }
        }
    }

    @Override
    public void addVisibleFaces(Vector<Face3D> visibleFaces, Transform3D transform3D, Point3D point3D)
    {
        if (this.isVisible)
        {
            Transform3D transform3D2 = (Transform3D)this.transform.clone();
            transform3D2.concatenate(transform3D);
            double[] fArr = new double[this.coords.length];
            transform3D2.transformTo(this.coords, 0, fArr, 0, this.coords.length);
            createFaces();
            int length = this.isReduced ? this.reducedFaceCount : this.faces.length;
            for (int i = 0; i < length; i++)
            {
                this.faces3D[i].setCoords(fArr);
                if (this.faces3D[i].isVisible(point3D))
                {
                    visibleFaces.addElement(this.faces3D[i]);
                }
            }
        }
    }

    public void setAction(int i, ActionListener actionListener)
    {
        this.faces3D = null;
        if (this.faceActions == null)
        {
            this.faceActions = new ActionListener[this.faces.length];
        }
        this.faceActions[i] = actionListener;
    }

    public void setBackgroundColor(int i, Color color)
    {
        this.colors[i][0] = color;
    }

    public Color getBackgroundColor(int i)
    {
        return this.colors[i][0];
    }

    public void setBorderColor(int i, Color color)
    {
        this.colors[i][1] = color;
    }

    public int getFaceCount()
    {
        return this.faces.length;
    }

    public boolean isWireframe()
    {
        return this.isWireframe;
    }

    public void setWireframe(boolean z)
    {
        this.isWireframe = z;
        this.faces3D = null;
    }
}
