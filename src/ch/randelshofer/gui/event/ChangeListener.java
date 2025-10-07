package ch.randelshofer.gui.event;

import java.util.EventListener;

public interface ChangeListener extends EventListener {
    void stateChanged(ChangeEvent changeEvent);
}
