package ch.randelshofer.rubik.parserAWT;

import java.util.Hashtable;
import java.util.StringTokenizer;

public class RandelshoferGERParser extends ScriptParser {
    private static final String COMPRESSED_TOKENS = "R;O;V;L;U;H;R-;O-;V-;L-;U-;H-;R2;O2;V2;L2;U2;H2;R2-;O2-;V2-;L2-;U2-;H2-;;;;;;;;;;;;;;;;;;;;;;;;;MR;MO;MV;ML;MU;MH;MR2;MO2;MV2;ML2;MU2;MH2;;;;;;;;;;;;;BR;BO;BV;BL;BU;BH;BR2;BO2;BV2;BL2;BU2;BH2;.;r;o;v;l;u;h;+;-;++;;-;;( [ {;) ] };,;(;);;;;;;;;;/*;*/;//;";

    public RandelshoferGERParser() {
        super(getTokens(), new Hashtable<>(), 1, 1, -1, -1, -1, true);
    }

    private static String[] getTokens() {
        int i = 0;
        StringTokenizer stringTokenizer = new StringTokenizer(COMPRESSED_TOKENS, ";", true);
        String[] tokens = new String[stringTokenizer.countTokens()];
        while (stringTokenizer.hasMoreTokens()) {
            String strNextToken = stringTokenizer.nextToken();
            if (strNextToken.equals(";")) {
                i++;
            } else {
                tokens[i] = strNextToken;
            }
        }
        return tokens;
    }
}
