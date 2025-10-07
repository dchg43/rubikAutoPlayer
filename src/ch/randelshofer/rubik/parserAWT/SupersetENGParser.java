package ch.randelshofer.rubik.parserAWT;

import java.util.Hashtable;
import java.util.StringTokenizer;

public class SupersetENGParser extends ScriptParser {
    private static final String COMPRESSED_TOKENS = "R;U;F;L;D;B;R';U';F';L';D';B';R2;U2;F2;L2;D2;B2;R2';U2';F2';L2';D2';B2';TR;TU;TF;TL;TD;TB;TR';TU';TF';TL';TD';TB';TR2;TU2;TF2;TL2;TD2;TB2;TR2';TU2';TF2';TL2';TD2';TB2';MR ML';MU MD';MF MB';ML MR';MD MU';MB MF';MR2 ML2';MU2 MD2';MF2 MB2';ML2 MR2';MD2 MU2';MB2 MF2';SR SL';SU SD';SF SB';SL SR';SD SU';SB SF';SR2 SL2';SU2 SD2';SF2 SB2';SL2 SR2';SD2 SU2';SB2 SF2';CR CL';CU CD';CF CB';CL CR';CD CU';CB CF';CR2 CL2';CU2 CD2';CF2 CB2';CL2 CR2';CD2 CU2';CB2 CF2';.;r;u;f;l;d;b;+;-;++;;';;( [ {;) ] };,;(;);;;;;;;;;/*;*/;//;";

    private static final String COMPRESSED_MACROS = "CFU CUF CDB CBD;CR CU2;CUB CBU CFD CDF;CL CU2;CRU CUR CLD CDL;CR2 CB;CUL CLU CRD CDR;CR2 CF;CRF CFR CLB CBL;CR2 CU;CLF CFL CBR CRB;CR2 CD;CUFL CFLU CLUF CDBR CBRD CRDB;CL CF;CURF CRVU CFUR CDLB CLBD CBDL;CR CU;CRUB CUBR CBRU CLDF CDFL CFLD;CR CB;CBUL CULB CLBU CFDR CDRF CRFD;CL CU;CLFO CULF CFUL CRBD CDRB CBDR;CR CD;CFRU CUFR CRUF CBLD CDBL CLDB;CL CB;CBUR CRBU CURB CFDL CLFD CDLF;CL CD;CLUB CBLU CUBL CRDF CFRD CDFR;CR CF";

    public SupersetENGParser() {
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
