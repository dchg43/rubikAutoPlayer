package ch.randelshofer.geom3d;

import java.util.Queue;

public interface SceneNode {
    /** 注意：PriorityQueue为有序队列，插入新数据时会自动插入到合适的位置以保证队列有序，无需重新排序，遍历时需要用poll方法 */
    void addVisibleFaces(Queue<Face3D> visibleFaces, Transform3D transform, Point3D point3D);
}
