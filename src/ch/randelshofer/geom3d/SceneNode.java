package ch.randelshofer.geom3d;

import java.util.Vector;

public interface SceneNode {
    void addVisibleFaces(Vector<Face3D> visibleFaces, Transform3D transform3D, Point3D point3D);
}
