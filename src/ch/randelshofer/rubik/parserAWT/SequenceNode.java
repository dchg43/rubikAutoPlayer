package ch.randelshofer.rubik.parserAWT;

public class SequenceNode extends ScriptNode
{
    private static final long serialVersionUID = 7550787881902269639L;

    public SequenceNode()
    {}

    public SequenceNode(int i, int i2)
    {
        super(i, i2);
    }

    @Override
    public int getSymbol()
    {
        return ScriptParser.SEQUENCE_EXPRESSION;
    }
}
