package ch.randelshofer.gui;


import ch.randelshofer.gui.event.ChangeListener;


public interface BoundedRangeModel
{
    int getMinimum();

    void setMinimum(int min);

    int getMaximum();

    void setMaximum(int max);

    int getValue();

    void setValue(int value);

    void setValueIsAdjusting(boolean isAdjusting);

    boolean getValueIsAdjusting();

    int getExtent();

    void setExtent(int extent);

    void setRangeProperties(int value, int extent, int min, int max, boolean isAdjusting);

    void addChangeListener(ChangeListener changeListener);

    void removeChangeListener(ChangeListener changeListener);
}
