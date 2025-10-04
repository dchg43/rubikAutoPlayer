package ch.randelshofer.geom3d;


import ch.randelshofer.gui.event.ChangeListener;


public interface Transform3DModel
{
    void setToIdentity();

    void rotate(double d, double d2, double d3);

    void rotateX(double d);

    void rotateY(double d);

    void rotateZ(double d);

    void scale(double d, double d2, double d3);

    void translate(double d, double d2, double d3);

    void concatenate(Transform3D transform3D);

    Transform3D getTransform();

    Transform3D getTransform(Transform3D transform3D);

    void setTransform(Transform3D transform3D);

    void addChangeListener(ChangeListener changeListener);

    void removeChangeListener(ChangeListener changeListener);
}
