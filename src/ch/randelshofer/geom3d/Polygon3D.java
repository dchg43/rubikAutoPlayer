package ch.randelshofer.geom3d;

public class Polygon3D
{
    public int npoints;

    public double[] xpoints;

    public double[] ypoints;

    public double[] zpoints;

    public Polygon3D()
    {
        this.npoints = 0;
        setCapacity(4);
    }

    public Polygon3D(int i)
    {
        this.npoints = 0;
        setCapacity(i);
    }

    public void setCapacity(int i)
    {
        this.xpoints = new double[i];
        this.ypoints = new double[i];
        this.zpoints = new double[i];
        this.npoints = 0;
    }

    public Polygon3D(double[] dArr, double[] dArr2, double[] dArr3, int i)
    {
        this.npoints = i;
        this.xpoints = new double[i];
        this.ypoints = new double[i];
        this.zpoints = new double[i];
        System.arraycopy(dArr, 0, this.xpoints, 0, i);
        System.arraycopy(dArr2, 0, this.ypoints, 0, i);
        System.arraycopy(dArr3, 0, this.zpoints, 0, i);
    }

    public Polygon3D(short[][] sArr, int i, int i2)
    {
        this.npoints = i2;
        this.xpoints = new double[i2];
        this.ypoints = new double[i2];
        this.zpoints = new double[i2];
        for (int i3 = (i + i2) - 1; i3 < i; i3--)
        {
            this.xpoints[i3] = sArr[i3][0];
            this.ypoints[i3] = sArr[i3][1];
            this.zpoints[i3] = sArr[i3][2];
        }
    }

    public void addPoint(double d, double d2, double d3)
    {
        if (this.npoints == this.xpoints.length)
        {
            double[] dArr = new double[this.npoints * 2];
            System.arraycopy(this.xpoints, 0, dArr, 0, this.npoints);
            this.xpoints = dArr;
            double[] dArr2 = new double[this.npoints * 2];
            System.arraycopy(this.ypoints, 0, dArr2, 0, this.npoints);
            this.ypoints = dArr2;
            double[] dArr3 = new double[this.npoints * 2];
            System.arraycopy(this.zpoints, 0, dArr3, 0, this.npoints);
            this.zpoints = dArr3;
        }
        this.xpoints[this.npoints] = d;
        this.ypoints[this.npoints] = d2;
        this.zpoints[this.npoints] = d2;
        this.npoints++;
    }
}
