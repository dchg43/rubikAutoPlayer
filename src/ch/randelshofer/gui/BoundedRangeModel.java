package ch.randelshofer.gui;


import ch.randelshofer.gui.event.ChangeListener;


public interface BoundedRangeModel
{
    int getMinimum();

    void setMinimum(int i);

    int getMaximum();

    void setMaximum(int i);

    int getValue();

    void setValue(int i);

    void setValueIsAdjusting(boolean z);

    boolean getValueIsAdjusting();

    int getExtent();

    void setExtent(int i);

    void setRangeProperties(int i, int i2, int i3, int i4, boolean z);

    void addChangeListener(ChangeListener changeListener);

    void removeChangeListener(ChangeListener changeListener);
}
