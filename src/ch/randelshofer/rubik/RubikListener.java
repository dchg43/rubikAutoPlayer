package ch.randelshofer.rubik;

import java.util.EventListener;

public interface RubikListener extends EventListener {
    void rubikTwisting(RubikEvent rubikEvent);

    void rubikTwisted(RubikEvent rubikEvent);

    void rubikPartRotated(RubikEvent rubikEvent);

    void rubikChanged(RubikEvent rubikEvent);
}
