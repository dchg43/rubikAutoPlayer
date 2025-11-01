package ch.randelshofer.geom3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class TransformNode implements SceneNode {
    private List<SceneNode> children = new ArrayList<>();

    private Transform3D transform = new Transform3D();

    private boolean isVisible = true;

    public void addChild(SceneNode sceneNode) {
        this.children.add(sceneNode);
    }

    public SceneNode getChild(int i) {
        return this.children.get(i);
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
    public void addVisibleFaces(Queue<Face3D> visibleFaces, Transform3D transform, Point3D point3D) {
        if (this.isVisible) {
            Transform3D transformClone = (Transform3D) this.transform.clone();
            transformClone.concatenate(transform);
            Iterator<SceneNode> children = this.children.iterator();
            while (children.hasNext()) {
                children.next().addVisibleFaces(visibleFaces, transformClone, point3D);
            }
        }
    }
}
