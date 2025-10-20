package ch.randelshofer.geom3d;

import ch.randelshofer.beans.AbstractStateModel;
import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;

public class RotatedTransform3DModel extends AbstractStateModel implements Transform3DModel, ChangeListener {
    private Transform3D rotator;

    private Transform3DModel model;

    public RotatedTransform3DModel(double x, double y, double z) {
        this.rotator = new Transform3D(x, y, z);
        this.model = new DefaultTransform3DModel();
        this.model.addChangeListener(this);
    }

    public RotatedTransform3DModel(double x, double y, double z, Transform3DModel model) {
        this.rotator = new Transform3D(x, y, z);
        this.model = model;
        this.model.addChangeListener(this);
    }

    public void setModel(Transform3DModel model) {
        this.model.removeChangeListener(this);
        this.model = model;
        this.model.addChangeListener(this);
    }

    @Override
    public void concatenate(Transform3D transform) {
        this.model.concatenate(transform);
    }

    @Override
    public Transform3D getTransform() {
        Transform3D transform = (Transform3D) this.model.getTransform().clone();
        transform.concatenate(this.rotator);
        return transform;
    }

    @Override
    public Transform3D getTransform(Transform3D transform) {
        this.model.getTransform(transform);
        transform.concatenate(this.rotator);
        return transform;
    }

    @Override
    public void rotate(double x, double y, double z) {
        Transform3D transform = getTransform();
        transform.rotate(x, y, z);
        transform.concatenate(this.rotator);
        this.model.setTransform(transform);
    }

    @Override
    public void rotateX(double x) {
        Transform3D transform = getTransform();
        transform.rotateX(x);
        transform.concatenate(this.rotator);
        this.model.setTransform(transform);
    }

    @Override
    public void rotateY(double y) {
        Transform3D transform = getTransform();
        transform.rotateY(y);
        transform.concatenate(this.rotator);
        this.model.setTransform(transform);
    }

    @Override
    public void rotateZ(double z) {
        Transform3D transform = getTransform();
        transform.rotateZ(z);
        transform.concatenate(this.rotator);
        this.model.setTransform(transform);
    }

    @Override
    public void scale(double x, double y, double z) {
        this.model.scale(x, y, z);
    }

    @Override
    public void setToIdentity() {
        this.model.setToIdentity();
    }

    @Override
    public void setTransform(Transform3D transform) {
        this.model.setTransform(transform);
    }

    @Override
    public void translate(double x, double y, double z) {
        this.model.translate(x, y, z);
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        fireStateChanged();
    }
}
