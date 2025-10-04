package ch.randelshofer.rubik.parserAWT;


import java.util.Hashtable;
import java.util.StringTokenizer;


public class CastellaParser extends ScriptParser
{
    private static final String COMPRESSED_TOKENS = "R;O;F;Li;U;D;Ri;Oi;Fi;L;Ui;Di;R2;O2;F2;Li2;U2;D2;Ri2;Oi2;Fi2;L2;Ui2;Di2;r;o;f;li;u;d;ri;oi;fi;l;ui;di;r2;o2;f2;li2;u2;d2;ri2;ui2;fi2;l2;ui2;di2;Mi;Ei;S;M;E;Si;Mi2;Ei2;S2;M2;E2;Si2;mi;ei;s;m;e;si;mi2;ei2;s2;m2;e2;si2;x;y;z;xi;yi;zi;x2;y2;z2;xi2;yi2;zi2;.;;;;;;;;;;;';;( {;) };;;;*;;;;;;;;[;];;";

    public CastellaParser()
    {
        super(getTokens(), new Hashtable<>(), 1, 1, -1, -1, -1, true);
    }

    private static String[] getTokens()
    {
        String[] strArr = new String[113];
        int i = 0;
        StringTokenizer stringTokenizer = new StringTokenizer(COMPRESSED_TOKENS, ";", true);
        while (stringTokenizer.hasMoreTokens())
        {
            String strNextToken = stringTokenizer.nextToken();
            if (strNextToken.equals(";"))
            {
                i++;
            }
            else
            {
                strArr[i] = strNextToken;
            }
        }
        return strArr;
    }
}
