package ch.randelshofer.geom3d;

public class Point3D
{
    public double x;

    public double y;

    public double z;

    public Point3D()
    {}

    public Point3D(double d, double d2, double d3)
    {
        this.x = d;
        this.y = d2;
        this.z = d3;
    }

    @Override
    public String toString()
    {
        return new StringBuffer().append("Point3D[").append(this.x).append(", ").append(this.y).append(", ").append(
            this.z).append("]").toString();
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }
}
