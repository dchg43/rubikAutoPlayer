package ch.randelshofer.beans;


import java.util.List;

import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;
import ch.randelshofer.gui.event.EventListenerList;
import ch.randelshofer.gui.event.EventListenerList.ListenerNode;


public class AbstractStateModel
{
    private EventListenerList listenerList = new EventListenerList();

    private ChangeEvent changeEvent;

    public void addChangeListener(ChangeListener changeListener)
    {
        this.listenerList.add(ChangeListener.class, changeListener);
    }

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

}
