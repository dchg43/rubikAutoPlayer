package ch.randelshofer.geom3d;


import ch.randelshofer.beans.AbstractStateModel;


public class DefaultTransform3DModel extends AbstractStateModel implements Transform3DModel
{
    private Transform3D transform;

    public DefaultTransform3DModel()
    {
        this.transform = new Transform3D();
    }

    public DefaultTransform3DModel(double d1, double d2, double d3, double d4, double d5, double d6, double d7,
                                   double d8, double d9, double d10, double d11, double d12)
    {
        this.transform = new Transform3D(d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12);
    }

    public DefaultTransform3DModel(double[][] dArr)
    {
        this.transform = new Transform3D(dArr);
    }

    @Override
    public void setToIdentity()
    {
        this.transform.setToIdentity();
        fireStateChanged();
    }

    @Override
    public void rotateX(double d)
    {
        this.transform.rotateX(d);
        fireStateChanged();
    }

    @Override
    public void rotateY(double d)
    {
        this.transform.rotateY(d);
        fireStateChanged();
    }

    @Override
    public void rotateZ(double d)
    {
        this.transform.rotateZ(d);
        fireStateChanged();
    }

    @Override
    public void scale(double d, double d2, double d3)
    {
        this.transform.scale(d, d2, d3);
        fireStateChanged();
    }

    @Override
    public void translate(double d, double d2, double d3)
    {
        this.transform.translate(d, d2, d3);
        fireStateChanged();
    }

    @Override
    public void concatenate(Transform3D transform3D)
    {
        this.transform.concatenate(transform3D);
        fireStateChanged();
    }

    @Override
    public void setTransform(Transform3D transform3D)
    {
        this.transform.setTransform(transform3D);
        fireStateChanged();
    }

    @Override
    public Transform3D getTransform()
    {
        return this.transform;
    }

    @Override
    public Transform3D getTransform(Transform3D transform3D)
    {
        transform3D.setTransform(this.transform);
        return transform3D;
    }

    @Override
    public void rotate(double d, double d2, double d3)
    {
        this.transform.rotate(d, d2, d3);
        fireStateChanged();
    }
}
