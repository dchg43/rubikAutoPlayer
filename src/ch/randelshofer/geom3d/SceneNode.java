package ch.randelshofer.geom3d;

import java.util.List;

public interface SceneNode {
    void addVisibleFaces(List<Face3D> visibleFaces, Transform3D transform, Point3D point3D);
}
