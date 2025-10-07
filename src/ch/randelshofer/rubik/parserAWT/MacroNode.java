package ch.randelshofer.rubik.parserAWT;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;
import ch.randelshofer.io.ParseException;

public class MacroNode extends ScriptNode {
    private static final long serialVersionUID = -7106917892877922567L;

    private String identifier;

    private String script;

    @Override
    public void transform(int axis) {
        this.identifier = null;
        super.transform(axis);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public int getSymbol() {
        return ScriptParser.MACRO_EXPRESSION;
    }

    public MacroNode(String identifier, String script, int startpos, int endpos) {
        super(startpos, endpos);
        this.identifier = identifier;
        this.script = script;
        setAllowsChildren(true);
    }

    public void expand(ScriptParser scriptParser) throws IOException, NumberFormatException {
        if (getChildCount() > 0) {
            return;
        }
        DefaultMutableTreeNode parent = getParent();
        while (true) {
            DefaultMutableTreeNode defaultMutableTreeNode = parent;
            if (defaultMutableTreeNode == null) {
                int startPosition = getStartPosition();
                int endPosition = getEndPosition();
                scriptParser.parse(new StringReader(this.script), this);
                Enumeration<?> enumerationBreadthFirstEnumeration = breadthFirstEnumeration();
                while (enumerationBreadthFirstEnumeration.hasMoreElements()) {
                    ScriptNode scriptNode = (ScriptNode) enumerationBreadthFirstEnumeration.nextElement();
                    scriptNode.setStartPosition(startPosition);
                    scriptNode.setEndPosition(endPosition);
                }
                return;
            }
            if ((defaultMutableTreeNode instanceof MacroNode) && ((MacroNode) defaultMutableTreeNode).identifier.equals(this.identifier)) {
                throw new ParseException("Macro: Illegal Recursion", getStartPosition(), getEndPosition());
            }
            parent = defaultMutableTreeNode.getParent();
        }
    }
}
