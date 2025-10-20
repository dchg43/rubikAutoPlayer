package ch.randelshofer.geom3d;

import java.util.Objects;

public class Transform3D implements Cloneable {
    private double m00;

    private double m10;

    private double m20;

    private double m30;

    private double m01;

    private double m11;

    private double m21;

    private double m31;

    private double m02;

    private double m12;

    private double m22;

    private double m32;

    public Transform3D() {
        this.m00 = 1.0d;
        this.m10 = 0.0d;
        this.m20 = 0.0d;
        this.m30 = 0.0d;
        this.m01 = 0.0d;
        this.m11 = 1.0d;
        this.m21 = 0.0d;
        this.m31 = 0.0d;
        this.m02 = 0.0d;
        this.m12 = 0.0d;
        this.m22 = 1.0d;
        this.m32 = 0.0d;
    }

    public Transform3D(double x, double y, double z) {
        this.m00 = 1.0d;
        this.m10 = 0.0d;
        this.m20 = 0.0d;
        this.m30 = 0.0d;
        this.m01 = 0.0d;
        this.m11 = 1.0d;
        this.m21 = 0.0d;
        this.m31 = 0.0d;
        this.m02 = 0.0d;
        this.m12 = 0.0d;
        this.m22 = 1.0d;
        this.m32 = 0.0d;
        rotate(x, y, z);
    }

    public Transform3D(double d1, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
        this.m00 = d1;
        this.m10 = d2;
        this.m20 = d3;
        this.m30 = d4;
        this.m01 = d5;
        this.m11 = d6;
        this.m21 = d7;
        this.m31 = d8;
        this.m02 = d9;
        this.m12 = d10;
        this.m22 = d11;
        this.m32 = d12;
    }

    public Transform3D(double[][] transform) {
        this.m00 = transform[0][0];
        this.m10 = transform[1][0];
        this.m20 = transform[2][0];
        this.m30 = transform[3][0];
        this.m01 = transform[0][1];
        this.m11 = transform[1][1];
        this.m21 = transform[2][1];
        this.m31 = transform[3][1];
        this.m02 = transform[0][2];
        this.m12 = transform[1][2];
        this.m22 = transform[2][2];
        this.m32 = transform[3][2];
    }

    public void setToIdentity() {
        this.m00 = 1.0d;
        this.m10 = 0.0d;
        this.m20 = 0.0d;
        this.m30 = 0.0d;
        this.m01 = 0.0d;
        this.m11 = 1.0d;
        this.m21 = 0.0d;
        this.m31 = 0.0d;
        this.m02 = 0.0d;
        this.m12 = 0.0d;
        this.m22 = 1.0d;
        this.m32 = 0.0d;
    }

    public void rotate(double x, double y, double z) {
        rotateX(x);
        rotateY(y);
        rotateZ(z);
    }

    public void rotateX(double x) {
        double dSin = Math.sin(x);
        double dCos = Math.cos(x);
        concatenate(new Transform3D(1.0d, 0.0d, 0.0d, 0.0d, 0.0d, dCos, dSin, 0.0d, 0.0d, -dSin, dCos, 0.0d));
    }

    public void rotateY(double y) {
        double dSin = Math.sin(y);
        double dCos = Math.cos(y);
        concatenate(new Transform3D(dCos, 0.0d, -dSin, 0.0d, 0.0d, 1.0d, 0.0d, 0.0d, dSin, 0.0d, dCos, 0.0d));
    }

    public void rotateZ(double z) {
        double dSin = Math.sin(z);
        double dCos = Math.cos(z);
        concatenate(new Transform3D(dCos, dSin, 0.0d, 0.0d, -dSin, dCos, 0.0d, 0.0d, 0.0d, 0.0d, 1.0d, 0.0d));
    }

    public void scale(double x, double y, double z) {
        concatenate(new Transform3D(x, 0.0d, 0.0d, 0.0d, 0.0d, y, 0.0d, 0.0d, 0.0d, 0.0d, z, 0.0d));
    }

    public void translate(double x, double y, double z) {
        concatenate(new Transform3D(1.0d, 0.0d, 0.0d, x, 0.0d, 1.0d, 0.0d, y, 0.0d, 0.0d, 1.0d, z));
    }

    public void concatenate(Transform3D trans) {
        double d00 = this.m00;
        double d10 = this.m10;
        double d20 = this.m20;
        double d30 = this.m30;
        double d01 = this.m01;
        double d11 = this.m11;
        double d21 = this.m21;
        double d31 = this.m31;
        double d02 = this.m02;
        double d12 = this.m12;
        double d22 = this.m22;
        double d32 = this.m32;
        this.m00 = (d00 * trans.m00) + (d01 * trans.m10) + (d02 * trans.m20);
        this.m01 = (d00 * trans.m01) + (d01 * trans.m11) + (d02 * trans.m21);
        this.m02 = (d00 * trans.m02) + (d01 * trans.m12) + (d02 * trans.m22);
        this.m10 = (d10 * trans.m00) + (d11 * trans.m10) + (d12 * trans.m20);
        this.m11 = (d10 * trans.m01) + (d11 * trans.m11) + (d12 * trans.m21);
        this.m12 = (d10 * trans.m02) + (d11 * trans.m12) + (d12 * trans.m22);
        this.m20 = (d20 * trans.m00) + (d21 * trans.m10) + (d22 * trans.m20);
        this.m21 = (d20 * trans.m01) + (d21 * trans.m11) + (d22 * trans.m21);
        this.m22 = (d20 * trans.m02) + (d21 * trans.m12) + (d22 * trans.m22);
        this.m30 = (d30 * trans.m00) + (d31 * trans.m10) + (d32 * trans.m20) + trans.m30;
        this.m31 = (d30 * trans.m01) + (d31 * trans.m11) + (d32 * trans.m21) + trans.m31;
        this.m32 = (d30 * trans.m02) + (d31 * trans.m12) + (d32 * trans.m22) + trans.m32;
    }

    public double[][] getMatrix() {
        return new double[][]{{this.m00, this.m10, this.m20, this.m30}, {this.m01, this.m11, this.m21, this.m31}, {this.m02, this.m12, this.m22, this.m32},
                {0.0d, 0.0d, 0.0d, 1.0d}};
    }

    @Override
    public int hashCode() {
        return Objects.hash(m00, m01, m02, m10, m11, m12, m20, m21, m22, m30, m31, m32);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof Transform3D)) {
            return false;
        }
        Transform3D other = (Transform3D) obj;
        return Double.doubleToLongBits(m00) == Double.doubleToLongBits(other.m00) && Double.doubleToLongBits(m01) == Double.doubleToLongBits(other.m01)
               && Double.doubleToLongBits(m02) == Double.doubleToLongBits(other.m02) && Double.doubleToLongBits(m10) == Double.doubleToLongBits(other.m10)
               && Double.doubleToLongBits(m11) == Double.doubleToLongBits(other.m11) && Double.doubleToLongBits(m12) == Double.doubleToLongBits(other.m12)
               && Double.doubleToLongBits(m20) == Double.doubleToLongBits(other.m20) && Double.doubleToLongBits(m21) == Double.doubleToLongBits(other.m21)
               && Double.doubleToLongBits(m22) == Double.doubleToLongBits(other.m22) && Double.doubleToLongBits(m30) == Double.doubleToLongBits(other.m30)
               && Double.doubleToLongBits(m31) == Double.doubleToLongBits(other.m31) && Double.doubleToLongBits(m32) == Double.doubleToLongBits(other.m32);
    }

    public void transform(Point3D point3D) {
        transform(point3D, point3D);
    }

    public Point3D transform(Point3D src, Point3D dest) {
        double x = (src.x * this.m00) + (src.y * this.m10) + (src.z * this.m20) + this.m30;
        double y = (src.x * this.m01) + (src.y * this.m11) + (src.z * this.m21) + this.m31;
        double z = (src.x * this.m02) + (src.y * this.m12) + (src.z * this.m22) + this.m32;
        if (dest == null) {
            return new Point3D(x, y, z);
        }
        dest.x = x;
        dest.y = y;
        dest.z = z;
        return dest;
    }

    public Polygon3D transform(Polygon3D src, Polygon3D dest) {
        if (dest == null) {
            dest = new Polygon3D(src.npoints);
        }
        if (dest.xpoints.length < src.npoints) {
            dest.setCapacity(src.npoints);
        }
        for (int i = src.npoints - 2; i >= 0; i--) {
            dest.xpoints[i] = (src.xpoints[i] * this.m00) + (src.ypoints[i] * this.m10) + (src.zpoints[i] * this.m20) + this.m30;
            dest.ypoints[i] = (src.xpoints[i] * this.m01) + (src.ypoints[i] * this.m11) + (src.zpoints[i] * this.m21) + this.m31;
            dest.ypoints[i] = (src.xpoints[i] * this.m02) + (src.ypoints[i] * this.m12) + (src.zpoints[i] * this.m22) + this.m32;
        }
        return dest;
    }

    public void transformTo(double[] src, int srcPos, double[] dest, int destPos, int length) {
        int max = srcPos + length;
        int destp = destPos * 3;
        int srcp = srcPos;
        while (srcp < max) {
            dest[destp] = src[srcp] * this.m00 + src[srcp + 1] * this.m10 + src[srcp + 2] * this.m20 + this.m30;
            dest[destp + 1] = src[srcp] * this.m01 + src[srcp + 1] * this.m11 + src[srcp + 2] * this.m21 + this.m31;
            dest[destp + 2] = src[srcp] * this.m02 + src[srcp + 1] * this.m12 + src[srcp + 2] * this.m22 + this.m32;
            srcp += 3;
            destp += 3;
        }
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{").append(this.m00).append(",").append(this.m10).append(",").append(this.m20).append(",").append(this.m30).append(
                "\n").append(this.m01).append(",").append(this.m11).append(",").append(this.m21).append(",").append(this.m31).append("\n").append(
                        this.m02).append(",").append(this.m12).append(",").append(this.m22).append(",").append(this.m32).append("}").toString();
    }

    public boolean isNaN() {
        return Double.isNaN(this.m00) || Double.isNaN(this.m10) || Double.isNaN(this.m20) || Double.isNaN(this.m30) || Double.isNaN(this.m01)
               || Double.isNaN(this.m11) || Double.isNaN(this.m21) || Double.isNaN(this.m31) || Double.isNaN(this.m02) || Double.isNaN(this.m12)
               || Double.isNaN(this.m22) || Double.isNaN(this.m32);
    }

    public void setTransform(Transform3D transform) {
        this.m00 = transform.m00;
        this.m10 = transform.m10;
        this.m20 = transform.m20;
        this.m30 = transform.m30;
        this.m01 = transform.m01;
        this.m11 = transform.m11;
        this.m21 = transform.m21;
        this.m31 = transform.m31;
        this.m02 = transform.m02;
        this.m12 = transform.m12;
        this.m22 = transform.m22;
        this.m32 = transform.m32;
    }
}
