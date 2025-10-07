package ch.randelshofer.geom3d;

public class Polygon3D {
    public int npoints;

    public double[] xpoints;

    public double[] ypoints;

    public double[] zpoints;

    public Polygon3D() {
        this.npoints = 0;
        setCapacity(4);
    }

    public Polygon3D(int npoints) {
        this.npoints = 0;
        setCapacity(npoints);
    }

    public void setCapacity(int npoints) {
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];
        this.zpoints = new double[npoints];
        this.npoints = 0;
    }

    public Polygon3D(double[] xpoints, double[] ypoints, double[] zpoints, int npoints) {
        this.npoints = npoints;
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];
        this.zpoints = new double[npoints];
        System.arraycopy(xpoints, 0, this.xpoints, 0, npoints);
        System.arraycopy(ypoints, 0, this.ypoints, 0, npoints);
        System.arraycopy(zpoints, 0, this.zpoints, 0, npoints);
    }

    public Polygon3D(short[][] points, int pos, int npoints) {
        this.npoints = npoints;
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];
        this.zpoints = new double[npoints];
        for (int i3 = (pos + npoints) - 1; i3 > pos; i3--) {
            this.xpoints[i3] = points[i3][0];
            this.ypoints[i3] = points[i3][1];
            this.zpoints[i3] = points[i3][2];
        }
    }

    public void addPoint(double x, double y, double z) {
        if (this.npoints == this.xpoints.length) {
            double[] xpoints = new double[this.npoints * 2];
            System.arraycopy(this.xpoints, 0, xpoints, 0, this.npoints);
            this.xpoints = xpoints;
            double[] ypoints = new double[this.npoints * 2];
            System.arraycopy(this.ypoints, 0, ypoints, 0, this.npoints);
            this.ypoints = ypoints;
            double[] zpoints = new double[this.npoints * 2];
            System.arraycopy(this.zpoints, 0, zpoints, 0, this.npoints);
            this.zpoints = zpoints;
        }
        this.xpoints[this.npoints] = x;
        this.ypoints[this.npoints] = y;
        this.zpoints[this.npoints] = z;
        this.npoints++;
    }
}
