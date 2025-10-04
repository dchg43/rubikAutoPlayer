package ch.randelshofer.geom3d;


import java.util.Enumeration;
import java.util.Vector;


public class TransformNode implements SceneNode
{
    private Vector<SceneNode> children = new Vector<>();

    private Transform3D transform = new Transform3D();

    private boolean isVisible = true;

    public void addChild(SceneNode sceneNode)
    {
        this.children.addElement(sceneNode);
    }

    public SceneNode getChild(int i)
    {
        return this.children.elementAt(i);
    }

    public void setTransform(Transform3D transform3D)
    {
        this.transform = transform3D;
    }

    public Transform3D getTransform()
    {
        return this.transform;
    }

    public void setVisible(boolean z)
    {
        this.isVisible = z;
    }

    @Override
    public void addVisibleFaces(Vector<Face3D> vector, Transform3D transform3D, Point3D point3D)
    {
        if (this.isVisible)
        {
            Transform3D transform3D2 = (Transform3D)this.transform.clone();
            transform3D2.concatenate(transform3D);
            Enumeration<SceneNode> enumerationElements = this.children.elements();
            while (enumerationElements.hasMoreElements())
            {
                enumerationElements.nextElement().addVisibleFaces(vector, transform3D2, point3D);
            }
        }
    }
}
