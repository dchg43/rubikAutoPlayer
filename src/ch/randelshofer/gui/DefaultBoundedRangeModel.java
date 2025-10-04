package ch.randelshofer.gui;


import java.util.List;

import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;
import ch.randelshofer.gui.event.EventListenerList;
import ch.randelshofer.gui.event.EventListenerList.ListenerNode;


public class DefaultBoundedRangeModel implements BoundedRangeModel
{
    private transient ChangeEvent changeEvent = null;

    private EventListenerList listenerList = new EventListenerList();

    private int value = 0;

    private int extent = 0;

    private int min = 0;

    private int max = 100;

    private boolean isAdjusting = false;

    public DefaultBoundedRangeModel()
    {}

    public DefaultBoundedRangeModel(int i1, int i2, int i3, int i4)
    {
        if (i4 < i3 || i1 < i3 || i1 + i2 > i4)
        {
            throw new IllegalArgumentException("invalid range properties");
        }
        this.value = i1;
        this.extent = i2;
        this.min = i3;
        this.max = i4;
    }

    @Override
    public int getValue()
    {
        return this.value;
    }

    @Override
    public int getExtent()
    {
        return this.extent;
    }

    @Override
    public int getMinimum()
    {
        return this.min;
    }

    @Override
    public int getMaximum()
    {
        return this.max;
    }

    @Override
    public void setValue(int i)
    {
        int iMax = Math.max(i, this.min);
        if (iMax + this.extent > this.max)
        {
            iMax = this.max - this.extent;
        }
        setRangeProperties(iMax, this.extent, this.min, this.max, this.isAdjusting);
    }

    @Override
    public void setExtent(int i)
    {
        int iMax = Math.max(0, i);
        if (this.value + iMax > this.max)
        {
            iMax = this.max - this.value;
        }
        setRangeProperties(this.value, iMax, this.min, this.max, this.isAdjusting);
    }

    @Override
    public void setMinimum(int i)
    {
        int maxMax = Math.max(i, this.max);
        int valueMax = Math.max(i, this.value);
        int extentMin = Math.min(maxMax - valueMax, this.extent);
        setRangeProperties(valueMax, extentMin, i, maxMax, this.isAdjusting);
    }

    @Override
    public void setMaximum(int i)
    {
        int minMin = Math.min(i, this.min);
        int valueMin = Math.min(i, this.value);
        int extentMin = Math.min(i - valueMin, this.extent);
        setRangeProperties(valueMin, extentMin, minMin, i, this.isAdjusting);
    }

    @Override
    public void setValueIsAdjusting(boolean z)
    {
        setRangeProperties(this.value, this.extent, this.min, this.max, z);
    }

    @Override
    public boolean getValueIsAdjusting()
    {
        return this.isAdjusting;
    }

    @Override
    public void setRangeProperties(int i, int i2, int i3, int i4, boolean z)
    {
        if (i3 > i4)
        {
            i3 = i4;
        }
        if (i > i4)
        {
            i4 = i;
        }
        if (i < i3)
        {
            i3 = i;
        }
        if (i2 + i > i4)
        {
            i2 = i4 - i;
        }
        if (i2 < 0)
        {
            i2 = 0;
        }
        if (i != this.value || i2 != this.extent || i3 != this.min || i4 != this.max || z != this.isAdjusting)
        {
            this.value = i;
            this.extent = i2;
            this.min = i3;
            this.max = i4;
            this.isAdjusting = z;
            fireStateChanged();
        }
    }

    @Override
    public void addChangeListener(ChangeListener changeListener)
    {
        this.listenerList.add(ChangeListener.class, changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener)
    {
        this.listenerList.remove(ChangeListener.class, changeListener);
    }

    protected void fireStateChanged()
    {
        List<ListenerNode> listenerList = this.listenerList.getListenerList();
        for (ListenerNode node : listenerList)
        {
            if (node.getClazz() == ChangeListener.class)
            {
                if (this.changeEvent == null)
                {
                    this.changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)node.getListener()).stateChanged(this.changeEvent);
            }
        }
    }

    @Override
    public String toString()
    {
        return new StringBuffer().append(getClass().getName()).append("[").append(
            new StringBuffer().append("value=").append(getValue()).append(", ").append("extent=").append(
                getExtent()).append(", ").append("min=").append(getMinimum()).append(", ").append("max=").append(
                    getMaximum()).append(", ").append("adj=").append(getValueIsAdjusting()).toString()).append(
                        "]").toString();
    }

}
