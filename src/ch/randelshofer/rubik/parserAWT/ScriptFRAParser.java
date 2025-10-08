package ch.randelshofer.rubik.parserAWT;

import java.util.Hashtable;
import java.util.StringTokenizer;

public class ScriptFRAParser extends ScriptParser {
    private static final String COMPRESSED_TOKENS = "Dh:Hg:Fm:Gb:Bd:Am:Db:Hd:F:Gh:Bg:A:DD:HH:FF:GG:BB:AA:::::::::::::::::::::::::::::::Mh:MCg:MFg:Mb:MCd:MFd:MM:MCC:MFF::::::::::::::::Ch:Cg:CRd:Cb:Cd:CRg:CC:CGG:CRR::::.:d:h:f:g:b:a:+:-:++:;:'::( [ {:) ] }:,:(:):;;;;;;/*;*/;//;";

    private static final String COMPRESSED_MACROS = "";

    public ScriptFRAParser() {
        super(getTokens(), getMacros(), 1, 1, -1, -1, -1, true);
    }

    private static String[] getTokens() {
        int i = 0;
        StringTokenizer stringTokenizer = new StringTokenizer(COMPRESSED_TOKENS, ":", true);
        String[] tokens = new String[stringTokenizer.countTokens()];
        while (stringTokenizer.hasMoreTokens()) {
            String strNextToken = stringTokenizer.nextToken();
            if (strNextToken.equals(":")) {
                i++;
            } else {
                tokens[i] = strNextToken;
            }
        }
        return tokens;
    }

    private static Hashtable<String, Object> getMacros() {
        Hashtable<String, Object> macros = new Hashtable<>();
        StringTokenizer stringTokenizer = new StringTokenizer(COMPRESSED_MACROS, ":", false);
        while (stringTokenizer.hasMoreTokens()) {
            StringTokenizer stringTokenizer2 = new StringTokenizer(stringTokenizer.nextToken());
            String strNextToken = stringTokenizer.nextToken();
            while (stringTokenizer2.hasMoreTokens()) {
                macros.put(stringTokenizer2.nextToken(), strNextToken);
            }
        }
        return macros;
    }
}
