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

    public DefaultBoundedRangeModel(int value, int extent, int min, int max)
    {
        if (max < min || value < min || value + extent > max)
        {
            throw new IllegalArgumentException("invalid range properties");
        }
        this.value = value;
        this.extent = extent;
        this.min = min;
        this.max = max;
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
    public void setValue(int value)
    {
        int valueMax = Math.max(value, this.min);
        if (valueMax > this.max - this.extent)
        {
            valueMax = this.max - this.extent;
        }
        setRangeProperties(valueMax, this.extent, this.min, this.max, this.isAdjusting);
    }

    @Override
    public void setExtent(int extent)
    {
        int extentMax = Math.max(0, extent);
        if (extentMax > this.max - this.value)
        {
            extentMax = this.max - this.value;
        }
        setRangeProperties(this.value, extentMax, this.min, this.max, this.isAdjusting);
    }

    @Override
    public void setMinimum(int min)
    {
        int max = Math.max(min, this.max);
        int value = Math.max(min, this.value);
        int extent = Math.min(max - value, this.extent);
        setRangeProperties(value, extent, min, max, this.isAdjusting);
    }

    @Override
    public void setMaximum(int max)
    {
        int min = Math.min(max, this.min);
        int value = Math.min(max, this.value);
        int extent = Math.min(max - value, this.extent);
        setRangeProperties(value, extent, min, max, this.isAdjusting);
    }

    @Override
    public void setValueIsAdjusting(boolean isAdjusting)
    {
        setRangeProperties(this.value, this.extent, this.min, this.max, isAdjusting);
    }

    @Override
    public boolean getValueIsAdjusting()
    {
        return this.isAdjusting;
    }

    @Override
    public void setRangeProperties(int value, int extent, int min, int max, boolean isAdjusting)
    {
        if (min > max)
        {
            min = max;
        }
        if (value > max)
        {
            max = value;
        }
        if (value < min)
        {
            min = value;
        }
        if (extent + value > max)
        {
            extent = max - value;
        }
        if (extent < 0)
        {
            extent = 0;
        }
        if (value != this.value || extent != this.extent || min != this.min || max != this.max
            || isAdjusting != this.isAdjusting)
        {
            this.value = value;
            this.extent = extent;
            this.min = min;
            this.max = max;
            this.isAdjusting = isAdjusting;
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
