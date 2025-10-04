package ch.randelshofer.gui;


import java.awt.Event;

import ch.randelshofer.gui.event.ChangeListener;


public class ToggleButton extends AbstractButton implements ChangeListener
{
    private static final long serialVersionUID = 6416655922539824065L;

    @Override
    public boolean mouseUp(Event event, int x, int y)
    {
        if (isEnabled() && isArmed())
        {
            boolean isSelected = !isSelected();
            if (isSelected || this.group == null)
            {
                setSelected(isSelected);
            }
        }
        super.mouseUp(event, x, y);
        return true;
    }
}
