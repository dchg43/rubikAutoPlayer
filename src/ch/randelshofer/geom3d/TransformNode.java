package ch.randelshofer.geom3d;

import java.util.Enumeration;
import java.util.Vector;

public class TransformNode implements SceneNode {
    private Vector<SceneNode> children = new Vector<>();

    private Transform3D transform = new Transform3D();

    private boolean isVisible = true;

    public void addChild(SceneNode sceneNode) {
        this.children.addElement(sceneNode);
    }

    public SceneNode getChild(int i) {
        return this.children.elementAt(i);
    }

    public void setTransform(Transform3D transform) {
        this.transform = transform;
    }

    public Transform3D getTransform() {
        return this.transform;
    }

    public void setVisible(boolean z) {
        this.isVisible = z;
    }

    @Override
    public void addVisibleFaces(Vector<Face3D> visibleFaces, Transform3D transform, Point3D point3D) {
        if (this.isVisible) {
            Transform3D transformClone = (Transform3D) this.transform.clone();
            transformClone.concatenate(transform);
            Enumeration<SceneNode> enumerationElements = this.children.elements();
            while (enumerationElements.hasMoreElements()) {
                enumerationElements.nextElement().addVisibleFaces(visibleFaces, transformClone, point3D);
            }
        }
    }
}
