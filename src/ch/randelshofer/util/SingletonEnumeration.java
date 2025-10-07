package ch.randelshofer.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;

public class SingletonEnumeration implements Enumeration<DefaultMutableTreeNode> {
    private DefaultMutableTreeNode object;

    public SingletonEnumeration(DefaultMutableTreeNode defaultMutableTreeNode) {
        this.object = defaultMutableTreeNode;
    }

    @Override
    public boolean hasMoreElements() {
        return this.object != null;
    }

    @Override
    public synchronized DefaultMutableTreeNode nextElement() {
        if (this.object == null) {
            throw new NoSuchElementException();
        }
        DefaultMutableTreeNode obj = this.object;
        this.object = null;
        return obj;
    }
}
