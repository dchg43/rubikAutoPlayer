package ch.randelshofer.rubik;


import java.util.EventObject;


public class RubikEvent extends EventObject
{
    private static final long serialVersionUID = 6923906880495115535L;

    private int axis;

    private int layerMask;

    private int angle;

    public RubikEvent(Object obj, int i, int i2, int i3)
    {
        super(obj);
        this.axis = i;
        this.layerMask = i2;
        this.angle = i3;
    }

    public int getPartType()
    {
        return this.axis;
    }

    public int getPartLocation()
    {
        return this.layerMask;
    }

    public int getAxis()
    {
        return this.axis;
    }

    public int getLayerMask()
    {
        return this.layerMask;
    }

    public int getAngle()
    {
        return this.angle;
    }
}
