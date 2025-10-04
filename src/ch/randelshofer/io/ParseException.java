package ch.randelshofer.io;


import java.io.IOException;


public class ParseException extends IOException
{
    private static final long serialVersionUID = 5016717291818586003L;

    private int startpos;

    private int endpos;

    public ParseException(String str, int i, int i2)
    {
        super(str);
        this.startpos = i;
        this.endpos = i2;
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
