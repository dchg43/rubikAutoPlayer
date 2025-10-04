package ch.randelshofer.geom3d;


import ch.randelshofer.beans.AbstractStateModel;
import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;


public class RotatedTransform3DModel extends AbstractStateModel implements Transform3DModel, ChangeListener
{
    private Transform3D rotator;

    private Transform3DModel model;

    public RotatedTransform3DModel(double d, double d2, double d3)
    {
        this.rotator = new Transform3D(d, d2, d3);
        this.model = new DefaultTransform3DModel();
        this.model.addChangeListener(this);
    }

    public RotatedTransform3DModel(double d, double d2, double d3, Transform3DModel transform3DModel)
    {
        this.rotator = new Transform3D(d, d2, d3);
        this.model = transform3DModel;
        this.model.addChangeListener(this);
    }

    public void setModel(Transform3DModel transform3DModel)
    {
        this.model.removeChangeListener(this);
        this.model = transform3DModel;
        this.model.addChangeListener(this);
    }

    @Override
    public void concatenate(Transform3D transform3D)
    {
        this.model.concatenate(transform3D);
    }

    @Override
    public Transform3D getTransform()
    {
        Transform3D transform = (Transform3D)this.model.getTransform().clone();
        transform.concatenate(this.rotator);
        return transform;
    }

    @Override
    public Transform3D getTransform(Transform3D transform3D)
    {
        this.model.getTransform(transform3D);
        transform3D.concatenate(this.rotator);
        return transform3D;
    }

    @Override
    public void rotate(double d, double d2, double d3)
    {
        Transform3D transform = getTransform();
        transform.rotate(d, d2, d3);
        transform.concatenate(this.rotator);
        this.model.setTransform(transform);
    }

    @Override
    public void rotateX(double d)
    {
        Transform3D transform = getTransform();
        transform.rotateX(d);
        transform.concatenate(this.rotator);
        this.model.setTransform(transform);
    }

    @Override
    public void rotateY(double d)
    {
        Transform3D transform = getTransform();
        transform.rotateY(d);
        transform.concatenate(this.rotator);
        this.model.setTransform(transform);
    }

    @Override
    public void rotateZ(double d)
    {
        Transform3D transform = getTransform();
        transform.rotateZ(d);
        transform.concatenate(this.rotator);
        this.model.setTransform(transform);
    }

    @Override
    public void scale(double d, double d2, double d3)
    {
        this.model.scale(d, d2, d3);
    }

    @Override
    public void setToIdentity()
    {
        this.model.setToIdentity();
    }

    @Override
    public void setTransform(Transform3D transform3D)
    {
        this.model.setTransform(transform3D);
    }

    @Override
    public void translate(double d, double d2, double d3)
    {
        this.model.translate(d, d2, d3);
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent)
    {
        fireStateChanged();
    }
}
