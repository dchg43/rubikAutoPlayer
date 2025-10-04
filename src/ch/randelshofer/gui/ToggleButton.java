package ch.randelshofer.gui;


import java.awt.Event;

import ch.randelshofer.gui.event.ChangeListener;


public class ToggleButton extends AbstractButton implements ChangeListener
{
    private static final long serialVersionUID = 6416655922539824065L;

    @Override
    public boolean mouseUp(Event event, int i, int i2)
    {
        if (isEnabled() && isArmed())
        {
            boolean z = !isSelected();
            if (z || this.group == null)
            {
                setSelected(z);
            }
        }
        super.mouseUp(event, i, i2);
        return true;
    }
}
