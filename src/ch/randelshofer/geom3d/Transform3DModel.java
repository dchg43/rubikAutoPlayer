package ch.randelshofer.geom3d;

import ch.randelshofer.gui.event.ChangeListener;

public interface Transform3DModel {
    void setToIdentity();

    void rotate(double x, double y, double z);

    void rotateX(double x);

    void rotateY(double y);

    void rotateZ(double z);

    void scale(double x, double y, double z);

    void translate(double x, double y, double z);

    void concatenate(Transform3D transform);

    Transform3D getTransform();

    Transform3D getTransform(Transform3D transform);

    void setTransform(Transform3D transform);

    void addChangeListener(ChangeListener changeListener);

    void removeChangeListener(ChangeListener changeListener);
}
