package ch.randelshofer.rubik.parserAWT;

public class SequenceNode extends ScriptNode {
    private static final long serialVersionUID = 7550787881902269639L;

    public SequenceNode() {
    }

    public SequenceNode(int startpos, int endpos) {
        super(startpos, endpos);
    }

    @Override
    public int getSymbol() {
        return ScriptParser.SEQUENCE_EXPRESSION;
    }
}
