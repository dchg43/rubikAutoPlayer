package ch.randelshofer.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;

public class EmptyEnumeration implements Iterator<DefaultMutableTreeNode> {
    private static final EmptyEnumeration EMPTY_ENUMERATION = new EmptyEnumeration();

    private EmptyEnumeration() {
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public DefaultMutableTreeNode next() {
        throw new NoSuchElementException();
    }

    public static EmptyEnumeration getInstance() {
        return EMPTY_ENUMERATION;
    }
}
