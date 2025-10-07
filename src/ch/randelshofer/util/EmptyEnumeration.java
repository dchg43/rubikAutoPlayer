package ch.randelshofer.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;

public class EmptyEnumeration implements Enumeration<DefaultMutableTreeNode> {
    public static final EmptyEnumeration EMPTY_ENUMERATION = new EmptyEnumeration();

    private EmptyEnumeration() {
    }

    @Override
    public boolean hasMoreElements() {
        return false;
    }

    @Override
    public DefaultMutableTreeNode nextElement() {
        throw new NoSuchElementException();
    }

    public static EmptyEnumeration getInstance() {
        return EMPTY_ENUMERATION;
    }
}
