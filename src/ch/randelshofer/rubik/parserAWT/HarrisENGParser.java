package ch.randelshofer.rubik.parserAWT;


import java.util.Hashtable;
import java.util.StringTokenizer;


public class HarrisENGParser extends ScriptParser
{
    private static final String COMPRESSED_TOKENS = "R;U;F;L;D;B;R';U';F';L';D';B';R2;U2;F2;L2;D2;B2;R2';U2';F2';L2';D2';B2';r;u;f;l;d;b;r';u';f';l';d';b';r2;u2;f2;l2;d2;b2;r2';u2';f2';l2';d2';b2';M';E';S;M;E;S';M2';E2';S2;M2;E2;S2';m';e';s;m;e;s';m2';e2';s2;m2;e2;s2';x;y;z;x';y';z';x2;y2;z2;x2';y2';z2';.;;;;;;;;;;;';;( {;) };;;;*;;;;;;;;[;];;";

    public HarrisENGParser()
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
