package ch.randelshofer.gui.event;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

public class EventListenerList {
    private List<ListenerNode> listenerList = new ArrayList<>();

    public List<ListenerNode> getListenerList() {
        return this.listenerList;
    }

    public int getListenerCount() {
        return this.listenerList.size();
    }

    public int getListenerCount(Class<?> cls) {
        int count = 0;
        for (ListenerNode listenerNode : listenerList) {
            if (cls == listenerNode.clazz) {
                count++;
            }
        }
        return count;
    }

    public synchronized void add(Class<?> cls, EventListener eventListener) {
        if (!cls.isInstance(eventListener)) {
            throw new IllegalArgumentException(new StringBuilder().append("Listener ").append(eventListener).append(" is not of type ").append(cls).toString());
        }
        if (eventListener == null) {
            throw new IllegalArgumentException(new StringBuilder().append("Listener ").append(eventListener).append(" is null").toString());
        }
        ListenerNode node = new ListenerNode(cls, eventListener);
        this.listenerList.add(node);
    }

    public synchronized void remove(Class<?> cls, EventListener eventListener) {
        this.listenerList.remove(new ListenerNode(cls, eventListener));
    }

    public static final class ListenerNode {
        private Class<?> clazz;

        private EventListener listener;

        public Class<?> getClazz() {
            return this.clazz;
        }

        public EventListener getListener() {
            return this.listener;
        }

        public ListenerNode(Class<?> clazz, EventListener listener) {
            this.clazz = clazz;
            this.listener = listener;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.clazz, this.listener);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            ListenerNode other = (ListenerNode) obj;
            return Objects.equals(this.clazz, other.clazz) && Objects.equals(this.listener, other.listener);
        }

    }

}
