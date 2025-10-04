package ch.randelshofer.io;


import java.io.IOException;


public class ParseException extends IOException
{
    private static final long serialVersionUID = 5016717291818586003L;

    private int startpos;

    private int endpos;

    public ParseException(String message, int startpos, int endpos)
    {
        super(message);
        this.startpos = startpos;
        this.endpos = endpos;
    }

    public int getStartPosition()
    {
        return this.startpos;
    }

    public int getEndPosition()
    {
        return this.endpos;
    }
}
