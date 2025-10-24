package ch.randelshofer.media;

import java.awt.Component;
import java.awt.image.ImageProducer;

import ch.randelshofer.gui.BoundedRangeModel;
import ch.randelshofer.gui.event.ChangeListener;

public interface Player {
    void start();

    void stop();

    boolean isActive();

    boolean isInactive();

    BoundedRangeModel getBoundedRangeModel();

    ImageProducer getImageProducer();

    Component getVisualComponent();

    Component getControlPanelComponent();

    void addChangeListener(ChangeListener changeListener);

    void removeChangeListener(ChangeListener changeListener);
}
