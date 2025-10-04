package ch.randelshofer.gui.event;


import java.util.EventObject;


public class ChangeEvent extends EventObject
{
    private static final long serialVersionUID = 1280599909353806955L;

    public ChangeEvent(Object obj)
    {
        super(obj);
    }
}
