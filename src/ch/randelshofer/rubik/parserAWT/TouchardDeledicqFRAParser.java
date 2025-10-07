package ch.randelshofer.rubik.parserAWT;

import java.util.Hashtable;
import java.util.StringTokenizer;

public class TouchardDeledicqFRAParser extends ScriptParser {
    private static final String COMPRESSED_TOKENS = "D;H;A;G;B;P;D-;H-;A-;G-;B-;P-;D2;H2;A2;G2;B2;P2;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;Ds;Hs;As;Gs;Bs;Ps;D2s;H2s;A2s;G2s;B2s;P2s;;;;;;;;;;;;;.;;;;;;;;;;;-;;( [ {;) ] };;;;;;;;;;;;/*;*/;//;";

    private static final String COMPRESSED_MACROS = "";

    public TouchardDeledicqFRAParser() {
        super(getTokens(), getMacros(), 1, 1, -1, -1, -1, true);
    }

    private static String[] getTokens() {
        String[] strArr = new String[113];
        int i = 0;
        StringTokenizer stringTokenizer = new StringTokenizer(COMPRESSED_TOKENS, ";", true);
        while (stringTokenizer.hasMoreTokens()) {
            String strNextToken = stringTokenizer.nextToken();
            if (strNextToken.equals(";")) {
                i++;
            } else {
                strArr[i] = strNextToken;
            }
        }
        return strArr;
    }

    private static Hashtable<String, Object> getMacros() {
        Hashtable<String, Object> hashtable = new Hashtable<>();
        StringTokenizer stringTokenizer = new StringTokenizer(COMPRESSED_MACROS, ";", false);
        while (stringTokenizer.hasMoreTokens()) {
            StringTokenizer stringTokenizer2 = new StringTokenizer(stringTokenizer.nextToken());
            String strNextToken = stringTokenizer.nextToken();
            while (stringTokenizer2.hasMoreTokens()) {
                hashtable.put(stringTokenizer2.nextToken(), strNextToken);
            }
        }
        return hashtable;
    }
}
