package ch.randelshofer.rubik.parserAWT;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import ch.randelshofer.io.ParseException;
import ch.randelshofer.io.StreamPosTokenizer;
import ch.randelshofer.rubik.RubiksCubeCore;

public class ScriptParser {
    public static final int POSITION_UNSUPPORTED = -1;

    public static final int POSITION_PREFIX = 0;

    public static final int POSITION_SUFFIX = 1;

    public static final int POSITION_HEADER = 2;

    private boolean DEBUG;

    private String[][] tokens;

    private Hashtable<String, Integer> transformationMap;

    private Hashtable<String, Integer> permutationMap;

    private Hashtable<String, Object> macroMap;

    private int commutatorPos;

    private int conjugatorPos;

    private int invertorPos;

    private int repetitorPos;

    @SuppressWarnings("unused")
    private int reflectorPos;

    private boolean isSequenceSupported;

    private boolean isAmbiguousSeqBeginPermBegin;

    private boolean isAmbiguousSeqBeginCmtrBegin;

    private boolean isAmbiguousSeqBeginCngrBegin;

    @SuppressWarnings("unused")
    private boolean isAmbiguousCngrBeginCmtrBegin;

    private boolean isAmbiguousCngrDelimCmtrDelim;

    @SuppressWarnings("unused")
    private boolean isAmbiguousCngrBeginPermBegin;

    @SuppressWarnings("unused")
    private boolean isAmbiguousCmtrBeginPermBegin;

    private boolean isAmbiguousSeqEndCmtrEnd;

    private boolean isAmbiguousSeqEndCngrEnd;

    private boolean isAmbiguousCngrEndCmtrEnd;

    public static final int R = 0;

    public static final int U = 1;

    public static final int F = 2;

    public static final int L = 3;

    public static final int D = 4;

    public static final int B = 5;

    public static final int Ri = 6;

    public static final int Ui = 7;

    public static final int Fi = 8;

    public static final int Li = 9;

    public static final int Di = 10;

    public static final int Bi = 11;

    public static final int R2 = 12;

    public static final int U2 = 13;

    public static final int F2 = 14;

    public static final int L2 = 15;

    public static final int D2 = 16;

    public static final int B2 = 17;

    public static final int R2i = 18;

    public static final int U2i = 19;

    public static final int F2i = 20;

    public static final int L2i = 21;

    public static final int D2i = 22;

    public static final int B2i = 23;

    public static final int TR = 24;

    public static final int TU = 25;

    public static final int TF = 26;

    public static final int TL = 27;

    public static final int TD = 28;

    public static final int TB = 29;

    public static final int TRi = 30;

    public static final int TUi = 31;

    public static final int TFi = 32;

    public static final int TLi = 33;

    public static final int TDi = 34;

    public static final int TBi = 35;

    public static final int TR2 = 36;

    public static final int TU2 = 37;

    public static final int TF2 = 38;

    public static final int TL2 = 39;

    public static final int TD2 = 40;

    public static final int TB2 = 41;

    public static final int TR2i = 42;

    public static final int TU2i = 43;

    public static final int TF2i = 44;

    public static final int TL2i = 45;

    public static final int TD2i = 46;

    public static final int TB2i = 47;

    public static final int MR = 48;

    public static final int MU = 49;

    public static final int FB = 50;

    public static final int ML = 51;

    public static final int MD = 52;

    public static final int BF = 53;

    public static final int MR2 = 54;

    public static final int MU2 = 55;

    public static final int MF2 = 56;

    public static final int ML2 = 57;

    public static final int MD2 = 58;

    public static final int MB2 = 59;

    public static final int SR = 60;

    public static final int SU = 61;

    public static final int SB = 62;

    public static final int SL = 63;

    public static final int SD = 64;

    public static final int SF = 65;

    public static final int SR2 = 66;

    public static final int SU2 = 67;

    public static final int SF2 = 68;

    public static final int SL2 = 69;

    public static final int SD2 = 70;

    public static final int SB2 = 71;

    public static final int CR = 72;

    public static final int CU = 73;

    public static final int CF = 74;

    public static final int CL = 75;

    public static final int CD = 76;

    public static final int CB = 77;

    public static final int CR2 = 78;

    public static final int CU2 = 79;

    public static final int CF2 = 80;

    public static final int CL2 = 81;

    public static final int CD2 = 82;

    public static final int CB2 = 83;

    public static final int NOP = 84;

    //    private static final int TWIST_FIRST_TOKEN = 0;
    //
    //    private static final int TWIST_LAST_TOKEN = 84;
    //
    //    private static final int FACE_FIRST_TOKEN = 0;
    //
    //    private static final int FACE_LAST_TOKEN = 23;
    //
    //    private static final int TWOLAYER_FIRST_TOKEN = 24;
    //
    //    private static final int TWOLAYER_LAST_TOKEN = 47;
    //
    //    private static final int MIDLAYER_FIRST_TOKEN = 48;
    //
    //    private static final int MIDLAYER_LAST_TOKEN = 59;
    //
    //    private static final int SLICE_FIRST_TOKEN = 60;
    //
    //    private static final int SLICE_LAST_TOKEN = 71;
    //
    //    private static final int ROTATION_FIRST_TOKEN = 72;
    //
    //    private static final int ROTATION_LAST_TOKEN = 83;

    public static final int PR = 85;

    public static final int PU = 86;

    public static final int PF = 87;

    public static final int PL = 88;

    public static final int PD = 89;

    public static final int PB = 90;

    public static final int PPLUS = 91;

    public static final int PMINUS = 92;

    public static final int PPLUSPLUS = 93;

    // private static final int PERMUTATION_FIRST_TOKEN = 85;

    // private static final int PERMUTATION_LAST_TOKEN = 93;

    public static final int STATEMENT_DELIMITER = 94;

    public static final int INVERTOR = 95;

    public static final int REFLECTOR = 96;

    public static final int SEQUENCE_BEGIN = 97;

    public static final int SEQUENCE_END = 98;

    public static final int PERMUTATION_DELIMITER = 99;

    public static final int PERMUTATION_BEGIN = 100;

    public static final int PERMUTATION_END = 101;

    public static final int REPETITOR_BEGIN = 102;

    public static final int REPETITOR_END = 103;

    public static final int COMMUTATOR_BEGIN = 104;

    public static final int COMMUTATOR_END = 105;

    public static final int COMMUTATOR_DELIMITER = 106;

    public static final int CONJUGATOR_BEGIN = 107;

    public static final int CONJUGATOR_END = 108;

    public static final int CONJUGATOR_DELIMITER = 109;

    public static final int COMMENT_BEGIN = 110;

    public static final int COMMENT_END = 111;

    public static final int SINGLE_LINE_COMMENT_BEGIN = 112;

    public static final int TOKEN_COUNT = 113;

    public static final int SCRIPT_EXPRESSION = 113;

    public static final int MACRO_EXPRESSION = 114;

    public static final int STATEMENT_EXPRESSION = 115;

    public static final int SEQUENCE_EXPRESSION = 116;

    public static final int INVERSION_EXPRESSION = 117;

    public static final int REPETITION_EXPRESSION = 118;

    public static final int PERMUTATION_EXPRESSION = 119;

    public static final int COMMUTATION_EXPRESSION = 120;

    public static final int CONJUGATION_EXPRESSION = 121;

    public static final int REFLECTION_EXPRESSION = 122;

    private static String[] defaultTokens;

    private static final String COMPRESSED_TOKENS = "R;U;F;L;D;B;Ri;Ui;Fi;Li;Di;Bi;R2;U2;F2;L2;D2;B2;R2i;U2i;F2i;L2i;D2i;B2i;TR;TU;TF;TL;TD;TB;TRi;TUi;TFi;TLi;TDi;TBi;TR2;TU2;TF2;TL2;TD2;TB2;TR2i;TU2i;TF2i;TL2i;TD2i;TB2i;MR;MU;MF;ML;MD;MB;MR2;MU2;MF2;ML2;MD2;MB2;SR;SU;SF;SL;SD;SB;SR2;SU2;SF2;SL2;SD2;SB2;CR;CU;CF;CL;CD;CB;CR2;CU2;CF2;CL2;CD2;CB2;NOP;permR;permU;permB;permL;permD;permF;permPlus;permMinus;permPlusPlus;statementDelimiter;invertor;reflector;sequenceBegin;sequenceEnd;permutationDelimiter;permutationBegin;permutationEnd;repetitorBegin;repetitorEnd;commutatorBegin;commutatorEnd;commutatorDelimiter;conjugatorBegin;conjugatorEnd;conjugatorDelimiter;commentBegin;commentEnd;singleLineCommentBegin;";

    public ScriptParser() {
        this(getDefaultTokens(), 1, 1, -1, 0, 0);
    }

    public ScriptParser(String[] tokens, int repetitorPos, int invertorPos, int reflectorPos, int conjugatorPos, int commutatorPos) {
        this(tokens, null, repetitorPos, invertorPos, reflectorPos, conjugatorPos, commutatorPos, true);
    }

    public ScriptParser(String[] tokens, Hashtable<String, Object> hashtable, int repetitorPos, int invertorPos, int reflectorPos, int conjugatorPos,
            int commutatorPos, boolean isSequenceSupported) {
        this.DEBUG = false;
        this.transformationMap = new Hashtable<>();
        this.permutationMap = new Hashtable<>();
        if (hashtable != null) {
            this.macroMap = hashtable;
        } else {
            this.macroMap = new Hashtable<>();
        }
        this.isSequenceSupported = isSequenceSupported;
        this.repetitorPos = repetitorPos;
        this.invertorPos = invertorPos;
        this.reflectorPos = reflectorPos;
        this.conjugatorPos = conjugatorPos;
        this.commutatorPos = commutatorPos;
        this.tokens = new String[tokens.length][0];
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] != null) {
                StringTokenizer stringTokenizer = new StringTokenizer(tokens[i], " ", false);
                this.tokens[i] = new String[stringTokenizer.countTokens()];
                for (int j = 0; j < this.tokens[i].length; j++) {
                    String strNextToken = stringTokenizer.nextToken();
                    this.tokens[i][j] = strNextToken;
                    if (85 > i || i > 93) {
                        if (strNextToken != null) {
                            this.transformationMap.put(strNextToken, i);
                        }
                    } else if (strNextToken != null) {
                        this.permutationMap.put(strNextToken, i);
                    }
                }
            }
        }
        this.isAmbiguousSeqBeginPermBegin = isAmbiguous(97, 100);
        this.isAmbiguousSeqBeginCmtrBegin = isAmbiguous(97, COMMUTATOR_BEGIN);
        this.isAmbiguousSeqBeginCngrBegin = isAmbiguous(97, CONJUGATOR_BEGIN);
        this.isAmbiguousCngrBeginCmtrBegin = isAmbiguous(COMMUTATOR_BEGIN, CONJUGATOR_BEGIN);
        this.isAmbiguousCngrDelimCmtrDelim = isAmbiguous(COMMUTATOR_DELIMITER, CONJUGATOR_DELIMITER);
        this.isAmbiguousCngrBeginPermBegin = isAmbiguous(CONJUGATOR_BEGIN, 100);
        this.isAmbiguousCmtrBeginPermBegin = isAmbiguous(COMMUTATOR_BEGIN, 100);
        this.isAmbiguousSeqEndCmtrEnd = isAmbiguous(98, COMMUTATOR_END);
        this.isAmbiguousSeqEndCngrEnd = isAmbiguous(98, CONJUGATOR_END);
        this.isAmbiguousCngrEndCmtrEnd = isAmbiguous(COMMUTATOR_END, CONJUGATOR_END);
        if (!(this.tokens[110].length == 0 && this.tokens[111].length == 0) && (this.tokens[110].length == 0 || this.tokens[111].length == 0
                                                                                || this.tokens[110].length != 1 || this.tokens[111].length != 1
                                                                                || this.tokens[110][0].length() < 1 || this.tokens[110][0].length() > 2
                                                                                || this.tokens[111][0].length() < 1 || this.tokens[111][0].length() > 2)) {
            throw new IllegalArgumentException(new StringBuffer().append("Illegal Comment Tokens ").append(Arrays.toString(this.tokens[110])).append(
                    " ").append(Arrays.toString(this.tokens[111])).toString());
        }
        if (this.tokens[112].length != 0 && (this.tokens[112].length != 1 || this.tokens[112][0].length() < 1 || this.tokens[112][0].length() > 2)) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Illegal Single Line Comment Token ");
            for (int i8 = 0; i8 < this.tokens[112].length; i8++) {
                if (i8 > 0) {
                    stringBuffer.append(',');
                }
                stringBuffer.append('\'');
                stringBuffer.append(this.tokens[112][i8]);
                stringBuffer.append('\'');
            }
            throw new IllegalArgumentException(stringBuffer.toString());
        }
    }

    private boolean isAmbiguous(int i, int i2) {
        String[] strArr = this.tokens[i];
        String[] strArr2 = this.tokens[i2];
        if (strArr != null && strArr2 != null) {
            for (String element : strArr) {
                if (element.length() > 0) {
                    for (String str : strArr2) {
                        if (element.equals(str)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int getInvertorPosition() {
        return this.invertorPos;
    }

    public boolean isInversionSupported() {
        return this.invertorPos != -1;
    }

    public boolean isSequenceSupported() {
        return this.isSequenceSupported;
    }

    public int getRepetitorPosition() {
        return this.repetitorPos;
    }

    public boolean isRepetitionSupported() {
        return this.repetitorPos != -1;
    }

    public boolean isPermutationSupported() {
        return this.permutationMap.size() > 0;
    }

    public ScriptNode parse(String str) throws IOException {
        return parse(new StringReader(str), null);
    }

    public ScriptNode parse(Reader reader) throws IOException {
        return parse(reader, null);
    }

    public ScriptNode parse(Reader reader, ScriptNode scriptNode) throws IOException, NumberFormatException {
        if (this.DEBUG) {
            System.out.println("BEGIN PARSE");
        }
        StreamPosTokenizer streamPosTokenizer = new StreamPosTokenizer(reader);
        streamPosTokenizer.resetSyntax();
        streamPosTokenizer.wordChars(33, 65535);
        streamPosTokenizer.whitespaceChars(0, 32);
        streamPosTokenizer.eolIsSignificant(false);
        if (this.tokens[110].length != 0) {
            streamPosTokenizer.slashStarComments(true);
            streamPosTokenizer.setSlashStarTokens(this.tokens[110][0], this.tokens[111][0]);
        }
        if (this.tokens[112].length != 0) {
            streamPosTokenizer.slashSlashComments(true);
            streamPosTokenizer.setSlashSlashToken(this.tokens[112][0]);
        }
        ScriptNode scriptNode2 = new ScriptNode();
        if (scriptNode != null) {
            scriptNode.add(scriptNode2);
        }
        scriptNode2.setStartPosition(0);
        while (streamPosTokenizer.nextToken() != -1) {
            streamPosTokenizer.pushBack();
            parseExpression(streamPosTokenizer, scriptNode2);
        }
        scriptNode2.setEndPosition(streamPosTokenizer.getEndPosition());
        if (this.DEBUG) {
            System.out.println("END PARSE");
        }
        return scriptNode2;
    }

    private void printVerbose(StreamPosTokenizer streamPosTokenizer, String str, ScriptNode scriptNode) throws IOException {
        if (!this.DEBUG) {
            return;
        }
        int depth = scriptNode.getDepth();
        StringBuffer stringBuffer = new StringBuffer();
        while (depth-- > 0) {
            stringBuffer.append('.');
        }
        stringBuffer.append(str);
        stringBuffer.append(' ');
        streamPosTokenizer.nextToken();
        stringBuffer.append(streamPosTokenizer.sval);
        streamPosTokenizer.pushBack();
        System.out.println(stringBuffer.toString());
    }

    private ExpressionNode parseExpression(StreamPosTokenizer streamPosTokenizer, ScriptNode scriptNode) throws IOException, NumberFormatException {
        if (this.DEBUG) {
            printVerbose(streamPosTokenizer, "expression", scriptNode);
        }
        if (streamPosTokenizer.nextToken() == StreamPosTokenizer.TT_WORD /* -3 */) {
            String greedy = parseGreedy(streamPosTokenizer.sval);
            Integer num = this.transformationMap.get(greedy);
            if (num != null && (num.intValue() == 94 || num.intValue() == 99)) {
                consumeGreedy(streamPosTokenizer, greedy);
                return null;
            }
        }
        streamPosTokenizer.pushBack();
        ExpressionNode expressionNode = new ExpressionNode();
        scriptNode.add(expressionNode);
        expressionNode.setStartPosition(streamPosTokenizer.getStartPosition());
        ScriptNode scriptNodeTmp = expressionNode;
        ScriptNode scriptNode2 = scriptNodeTmp;
        while ((scriptNodeTmp = parsePrefix(streamPosTokenizer, scriptNodeTmp)) != null) {
            scriptNode2 = scriptNodeTmp;
        }
        expressionNode.setEndPosition(parseStatement(streamPosTokenizer, scriptNode2).getEndPosition());
        ScriptNode childNode = (ScriptNode) expressionNode.getChildAt(0);
        // TODO: 有可能死循环？
        while ((scriptNodeTmp = parseSuffix(streamPosTokenizer, expressionNode)) != null) {
            scriptNodeTmp.add(childNode);
            childNode = scriptNodeTmp;
            expressionNode.setEndPosition(scriptNodeTmp.getEndPosition());
        }
        return expressionNode;
    }

    // TODO: 看起来不正确
    private ScriptNode parsePrefix(StreamPosTokenizer streamPosTokenizer, ScriptNode scriptNode) throws IOException, NumberFormatException {
        if (this.DEBUG) {
            printVerbose(streamPosTokenizer, "prefix", scriptNode);
        }
        if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
            streamPosTokenizer.pushBack();
            return null;
        }
        String greedyInt = null;
        Integer num = this.transformationMap.get(parseGreedy(streamPosTokenizer.sval));
        if (num == null) {
            greedyInt = parseGreedyInt(streamPosTokenizer.sval);
        }
        streamPosTokenizer.pushBack();
        if (num == null && greedyInt == "\000") {
            return null;
        }
        if (num == null) {
            if (this.repetitorPos == 0) {
                return parseRepetitor(streamPosTokenizer, scriptNode);
            }
            return null;
        }
        int iIntValue = num.intValue();
        if (this.invertorPos == 0 && iIntValue == 95) {
            return parseInvertor(streamPosTokenizer, scriptNode);
        }
        if (this.repetitorPos == 0 && iIntValue == 102) {
            return parseRepetitor(streamPosTokenizer, scriptNode);
        }
        return null;
    }

    private ScriptNode parseSuffix(StreamPosTokenizer streamPosTokenizer, ScriptNode scriptNode) throws IOException, NumberFormatException {
        if (this.DEBUG) {
            printVerbose(streamPosTokenizer, "suffix", scriptNode);
        }
        if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
            streamPosTokenizer.pushBack();
            return null;
        }
        String greedyInt = null;
        Integer num = this.transformationMap.get(parseGreedy(streamPosTokenizer.sval));
        if (num == null) {
            greedyInt = parseGreedyInt(streamPosTokenizer.sval);
        }
        streamPosTokenizer.pushBack();
        if (num == null && greedyInt == "\000") {
            return null;
        }
        if (num == null) {
            if (this.repetitorPos == 1) {
                return parseRepetitor(streamPosTokenizer, scriptNode);
            }
            return null;
        }
        int iIntValue = num.intValue();
        if (this.invertorPos == 1 && iIntValue == 95) {
            return parseInvertor(streamPosTokenizer, scriptNode);
        }
        if (this.repetitorPos == 1 && iIntValue == 102) {
            return parseRepetitor(streamPosTokenizer, scriptNode);
        }
        return null;
    }

    private ScriptNode parseInvertor(StreamPosTokenizer streamPosTokenizer, ScriptNode scriptNode) throws IOException {
        if (this.DEBUG) {
            printVerbose(streamPosTokenizer, "invertor", scriptNode);
        }
        InversionNode inversionNode = new InversionNode();
        scriptNode.add(inversionNode);
        inversionNode.setStartPosition(streamPosTokenizer.getStartPosition());
        if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
            throw new ParseException("Invertor: Token missing.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        Hashtable<String, Integer> hashtable = this.transformationMap;
        String greedy = parseGreedy(streamPosTokenizer.sval);
        Integer num = hashtable.get(greedy);
        if (num == null || num.intValue() != 95) {
            throw new ParseException(new StringBuffer().append("Invertor: Illegal token ").append(streamPosTokenizer.sval).toString(),
                    streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        inversionNode.setEndPosition((streamPosTokenizer.getStartPosition() + greedy.length()) - 1);
        consumeGreedy(streamPosTokenizer, greedy);
        return inversionNode;
    }

    private ScriptNode parseRepetitor(StreamPosTokenizer streamPosTokenizer, ScriptNode scriptNode) throws IOException, NumberFormatException {
        if (this.DEBUG) {
            printVerbose(streamPosTokenizer, "repetitor", scriptNode);
        }
        RepetitionNode repetitionNode = new RepetitionNode();
        scriptNode.add(repetitionNode);
        repetitionNode.setStartPosition(streamPosTokenizer.getStartPosition());
        if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
            throw new ParseException("Repetitor: Token missing.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        Hashtable<String, Integer> hashtable = this.transformationMap;
        String greedy = parseGreedy(streamPosTokenizer.sval);
        Integer num = hashtable.get(greedy);
        if (num == null || num.intValue() != 102) {
            streamPosTokenizer.pushBack();
        } else {
            consumeGreedy(streamPosTokenizer, greedy);
        }
        if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
            throw new ParseException("Repetitor: Repeat count missing.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        String greedyInt = parseGreedyInt(streamPosTokenizer.sval);
        if (greedyInt == "\000") {
            throw new ParseException(new StringBuffer().append("Repetitor: Invalid repeat count ").append(streamPosTokenizer.sval).toString(),
                    streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        int i;
        try {
            i = Integer.parseInt(greedyInt);
        } catch (NumberFormatException e) {
            throw new ParseException(new StringBuffer().append("Repetitor: Internal Error ").append(e.getMessage()).toString(),
                    streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        if (i < 1) {
            throw new ParseException(new StringBuffer().append("Repetitor: Invalid repeat count ").append(i).toString(), streamPosTokenizer.getStartPosition(),
                    streamPosTokenizer.getEndPosition());
        }
        repetitionNode.setRepeatCount(i);
        repetitionNode.setEndPosition((streamPosTokenizer.getStartPosition() + greedyInt.length()) - 1);
        consumeGreedy(streamPosTokenizer, greedyInt);
        if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
            streamPosTokenizer.pushBack();
            return repetitionNode;
        }
        Hashtable<String, Integer> hashtable2 = this.transformationMap;
        String greedy2 = parseGreedy(streamPosTokenizer.sval);
        Integer num2 = hashtable2.get(greedy2);
        if (num2 == null) {
            streamPosTokenizer.pushBack();
            return repetitionNode;
        }
        if (num2.intValue() == 103) {
            consumeGreedy(streamPosTokenizer, greedy2);
        } else {
            streamPosTokenizer.pushBack();
        }
        return repetitionNode;
    }

    private ScriptNode parseStatement(StreamPosTokenizer streamPosTokenizer, ScriptNode scriptNode) throws IOException, NumberFormatException {
        if (this.DEBUG) {
            printVerbose(streamPosTokenizer, "statement", scriptNode);
        }
        if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
            throw new ParseException("Statement: Token missing.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        String greedy = parseGreedy(streamPosTokenizer.sval);
        Integer num = this.transformationMap.get(greedy);
        if (num == null) {
            if (this.macroMap.get(greedy) == null) {
                throw new ParseException(new StringBuffer().append("Statement: Unknown token ").append(streamPosTokenizer.sval).toString(),
                        streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
            }
            streamPosTokenizer.pushBack();
            return parseMacro(streamPosTokenizer, scriptNode);
        }
        int iIntValue = num.intValue();
        if (0 <= iIntValue && iIntValue <= 84) {
            streamPosTokenizer.pushBack();
            return parseTwist(streamPosTokenizer, scriptNode);
        }
        if (!this.isAmbiguousSeqBeginPermBegin && iIntValue == 97) {
            int startPosition = streamPosTokenizer.getStartPosition();
            consumeGreedy(streamPosTokenizer, greedy);
            return parseSequence(streamPosTokenizer, scriptNode, startPosition, 0x1 | ((this.conjugatorPos == 2 && this.isAmbiguousSeqBeginCngrBegin) ? 2 : 0)
                                                                                | ((this.commutatorPos == 2 && this.isAmbiguousSeqBeginCmtrBegin) ? 4 : 0));
        }
        if (iIntValue == 100 && !this.isAmbiguousSeqBeginPermBegin) {
            int startPosition2 = streamPosTokenizer.getStartPosition();
            consumeGreedy(streamPosTokenizer, greedy);
            return parsePermutation(streamPosTokenizer, scriptNode, startPosition2);
        }
        if (iIntValue != 97 && iIntValue != 100 && ((iIntValue != 107 || this.conjugatorPos != 2) && (iIntValue != 104 || this.commutatorPos != 2))) {
            throw new ParseException(new StringBuffer().append("Statement: Illegal Token ").append(streamPosTokenizer.sval).toString(),
                    streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        int startPosition3 = streamPosTokenizer.getStartPosition();
        consumeGreedy(streamPosTokenizer, greedy);
        if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
            throw new ParseException("Statement: Token missing.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        Integer num2 = this.permutationMap.get(parseGreedy(streamPosTokenizer.sval));
        streamPosTokenizer.pushBack();
        if (num2 == null || 85 > num2.intValue() || num2.intValue() > 93) {
            return parseSequence(streamPosTokenizer, scriptNode, startPosition3, 1 | ((this.conjugatorPos == 2 && this.isAmbiguousSeqBeginCngrBegin) ? 2 : 0)
                                                                                 | ((this.commutatorPos == 2 && this.isAmbiguousSeqBeginCmtrBegin) ? 4 : 0));
        }
        return parsePermutation(streamPosTokenizer, scriptNode, startPosition3);
    }

    private ScriptNode parseSequence(StreamPosTokenizer paramStreamPosTokenizer, ScriptNode paramScriptNode, int paramInt1, int paramInt2) throws IOException {
        if (this.DEBUG) {
            printVerbose(paramStreamPosTokenizer, "sequence", paramScriptNode);
        }
        ScriptNode scriptNode1 = new ScriptNode();
        scriptNode1.setStartPosition(paramInt1);
        paramScriptNode.add(scriptNode1);
        ScriptNode scriptNode2 = null;
        ScriptNode scriptNode3 = scriptNode1;
        while (true) {
            int next = paramStreamPosTokenizer.nextToken();
            if (next == StreamPosTokenizer.TT_WORD /* -3 */) {
                String str = parseGreedy(paramStreamPosTokenizer.sval);
                Integer integer = this.transformationMap.get(str);
                int bool = (integer == null) ? -1 : integer.intValue();
                if (bool == 98 || bool == 101) {
                    if (!this.isAmbiguousSeqEndCngrEnd) {
                        paramInt2 &= 0x5;
                    }
                    if (!this.isAmbiguousSeqEndCmtrEnd) {
                        paramInt2 &= 0x3;
                    }
                    scriptNode3.setEndPosition(paramStreamPosTokenizer.getStartPosition() + str.length() - 1);
                    consumeGreedy(paramStreamPosTokenizer, str);
                    break;
                }
                if (bool == 109 && this.conjugatorPos == 2) {
                    if ((paramInt2 & 0x1) == 1 && !this.isAmbiguousSeqBeginCngrBegin) {
                        throw new ParseException("Sequence: Illegal delimiter.", paramStreamPosTokenizer.getStartPosition(),
                                paramStreamPosTokenizer.getEndPosition());
                    }
                    if (!this.isAmbiguousCngrDelimCmtrDelim) {
                        paramInt2 = 2;
                    }
                    if (scriptNode2 == null) {
                        scriptNode1.setEndPosition(paramStreamPosTokenizer.getStartPosition());
                        scriptNode2 = new ScriptNode();
                        scriptNode2.setStartPosition(paramStreamPosTokenizer.getEndPosition());
                        paramScriptNode.add(scriptNode2);
                        scriptNode3 = scriptNode2;
                    } else {
                        throw new ParseException("Conjugation: Delimiter must occur only once", paramStreamPosTokenizer.getStartPosition(),
                                paramStreamPosTokenizer.getEndPosition());
                    }
                    consumeGreedy(paramStreamPosTokenizer, str);
                    continue;
                }
                if (bool == 106 && this.commutatorPos == 2) {
                    if ((paramInt2 & 0x1) == 1 && !this.isAmbiguousSeqBeginCmtrBegin) {
                        throw new ParseException("Sequence: Illegal delimiter.", paramStreamPosTokenizer.getStartPosition(),
                                paramStreamPosTokenizer.getEndPosition());
                    }
                    if (!this.isAmbiguousCngrDelimCmtrDelim) {
                        paramInt2 = 4;
                    }
                    if (scriptNode2 == null) {
                        scriptNode1.setEndPosition(paramStreamPosTokenizer.getStartPosition());
                        scriptNode2 = new ScriptNode();
                        scriptNode2.setStartPosition(paramStreamPosTokenizer.getEndPosition());
                        paramScriptNode.add(scriptNode2);
                        scriptNode3 = scriptNode2;
                    } else {
                        throw new ParseException("Commutation: Delimiter must occur only once", paramStreamPosTokenizer.getStartPosition(),
                                paramStreamPosTokenizer.getEndPosition());
                    }
                    consumeGreedy(paramStreamPosTokenizer, str);
                    continue;
                }
                if (bool == 108 && this.conjugatorPos == 2) {
                    if (!this.isAmbiguousCngrEndCmtrEnd) {
                        paramInt2 &= 0x3;
                    }
                    if (!this.isAmbiguousSeqEndCngrEnd) {
                        paramInt2 &= 0x6;
                    }
                    scriptNode3.setEndPosition(paramStreamPosTokenizer.getStartPosition() + str.length() - 1);
                    consumeGreedy(paramStreamPosTokenizer, str);
                    break;
                }
                if (bool == 105 && this.commutatorPos == 2) {
                    if (!this.isAmbiguousCngrEndCmtrEnd) {
                        paramInt2 &= 0x5;
                    }
                    if (!this.isAmbiguousSeqEndCmtrEnd) {
                        paramInt2 &= 0x6;
                    }
                    scriptNode3.setEndPosition(paramStreamPosTokenizer.getStartPosition() + str.length() - 1);
                    consumeGreedy(paramStreamPosTokenizer, str);
                    break;
                }
                paramStreamPosTokenizer.pushBack();
                parseExpression(paramStreamPosTokenizer, scriptNode3);
                continue;
            } else if (next == -1) {
                throw new ParseException("Sequence: Close bracket missing.", paramStreamPosTokenizer.getStartPosition(),
                        paramStreamPosTokenizer.getEndPosition());
            } else {
                throw new ParseException("Sequence: Internal error.", paramStreamPosTokenizer.getStartPosition(), paramStreamPosTokenizer.getEndPosition());
            }
        }
        scriptNode1.removeFromParent();
        if (scriptNode2 != null) {
            scriptNode2.removeFromParent();
        }
        switch (paramInt2) {
        case 1:
            if (scriptNode2 != null) {
                throw new ParseException("Sequence: Illegal Sequence.", paramInt1, paramStreamPosTokenizer.getEndPosition());
            }
            scriptNode3 = new SequenceNode(paramInt1, paramStreamPosTokenizer.getEndPosition());
            scriptNode3.add(scriptNode1);
            break;
        }
        paramScriptNode.add(scriptNode3);
        return scriptNode3;
    }

    private ScriptNode parsePermutation(StreamPosTokenizer paramStreamPosTokenizer, ScriptNode paramScriptNode, int paramInt) throws IOException {
        if (this.DEBUG) {
            printVerbose(paramStreamPosTokenizer, "permutation", paramScriptNode);
        }
        PermutationNode permutationNode = new PermutationNode();
        paramScriptNode.add(permutationNode);
        permutationNode.setStartPosition(paramInt);
        while (true) {
            switch (paramStreamPosTokenizer.nextToken()) {
            case StreamPosTokenizer.TT_WORD: /* -3 */
                String str = parseGreedy(paramStreamPosTokenizer.sval);
                Integer integer = this.transformationMap.get(str);
                int bool = (integer == null) ? -1 : integer.intValue();
                if (bool == 98 || bool == 101) {
                    permutationNode.setEndPosition(paramStreamPosTokenizer.getStartPosition() + str.length() - 1);
                    consumeGreedy(paramStreamPosTokenizer, str);
                    break;
                }
                paramStreamPosTokenizer.pushBack();
                parsePermutationItem(paramStreamPosTokenizer, permutationNode);
                if (paramStreamPosTokenizer.nextToken() == StreamPosTokenizer.TT_WORD /* -3 */) {
                    str = parseGreedy(paramStreamPosTokenizer.sval);
                    integer = this.transformationMap.get(str);
                    if (integer.intValue() == 99) {
                        consumeGreedy(paramStreamPosTokenizer, str);
                        continue;
                    }
                    paramStreamPosTokenizer.pushBack();
                    continue;
                }
                paramStreamPosTokenizer.pushBack();
                continue;
            case -1:
                throw new ParseException("Permutation: Close bracket missing.", paramStreamPosTokenizer.getStartPosition(),
                        paramStreamPosTokenizer.getEndPosition());
            default:
                throw new ParseException("Permutation: Internal error.", paramStreamPosTokenizer.getStartPosition(), paramStreamPosTokenizer.getEndPosition());
            }
            if (permutationNode.getPermItemCount() == 0) {
                throw new ParseException("Permutation: Illegal empty Permutation.", permutationNode.getStartPosition(), permutationNode.getEndPosition());
            }
            return permutationNode;
        }
    }

    private void parsePermutationItem(StreamPosTokenizer streamPosTokenizer, PermutationNode permutationNode) throws IOException {
        if (this.DEBUG) {
            printVerbose(streamPosTokenizer, "permutationItem", permutationNode);
        }
        if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
            throw new ParseException("PermutationItem: Token missing.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        int startPosition = streamPosTokenizer.getStartPosition();
        String greedy = parseGreedy(streamPosTokenizer.sval);
        Integer num = this.permutationMap.get(greedy);
        if (num == null) {
            throw new ParseException(new StringBuffer().append("PermutationItem: Illegal token ").append(streamPosTokenizer.sval).toString(),
                    streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        int iIntValue = num.intValue();
        int i;
        switch (iIntValue) {
        case PPLUS: /* 91 */
        case PMINUS: /* 92 */
        case 93:
            i = iIntValue;
            consumeGreedy(streamPosTokenizer, greedy);
            break;
        default:
            i = 0;
            streamPosTokenizer.pushBack();
            break;
        }
        int[] iArr = new int[3];
        int i2 = 0;
        while (i2 < 3) {
            if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
                throw new ParseException("PermutationItem: Face token missing.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
            }
            String greedy2 = parseGreedy(streamPosTokenizer.sval);
            Integer num2 = this.permutationMap.get(greedy2);
            if (num2 == null) {
                throw new ParseException(new StringBuffer().append("PermutationItem: Illegal or unknown token ").append(streamPosTokenizer.sval).toString(),
                        streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
            }
            int iIntValue2 = num2.intValue();
            if (85 > iIntValue2 || iIntValue2 > 90) {
                throw new ParseException(new StringBuffer().append("PermutationItem: Illegal token ").append(streamPosTokenizer.sval).toString(),
                        streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
            }
            if (this.DEBUG) {
                printVerbose(streamPosTokenizer, new StringBuffer().append("permutationItem Face:").append(greedy2).toString(), permutationNode);
            }
            iArr[i2++] = iIntValue2;
            consumeGreedy(streamPosTokenizer, greedy2);
            if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
                throw new ParseException("PermutationItem: Token missing.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
            }
            Integer num3 = this.transformationMap.get(parseGreedy(streamPosTokenizer.sval));
            int iIntValue3 = num3 == null ? -1 : num3.intValue();
            streamPosTokenizer.pushBack();
            switch (iIntValue3) {
            case 94:
            case 98:
            case 99:
            case 101:
                break;
            default:
                i2--; // TODO:
                break;
            }
        }
        try {
            permutationNode.addPermItem(i2, i, iArr);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), startPosition, streamPosTokenizer.getEndPosition());
        }
    }

    private ScriptNode parseTwist(StreamPosTokenizer streamPosTokenizer, ScriptNode scriptNode) throws IOException {
        if (this.DEBUG) {
            printVerbose(streamPosTokenizer, "transformation", scriptNode);
        }
        TwistNode twistNode = new TwistNode();
        scriptNode.add(twistNode);
        if (streamPosTokenizer.nextToken() != StreamPosTokenizer.TT_WORD /* -3 */) {
            throw new ParseException("Twist: Token missing.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        twistNode.setStartPosition(streamPosTokenizer.getStartPosition());
        String greedy = parseGreedy(streamPosTokenizer.sval);
        Integer num = this.transformationMap.get(greedy);
        int iIntValue = num == null ? -1 : num.intValue();
        if (0 > iIntValue || iIntValue > 84) {
            throw new ParseException(new StringBuffer().append("Twist: Illegal token ").append(streamPosTokenizer.sval).toString(),
                    streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
        twistNode.setSymbol(iIntValue);
        twistNode.setEndPosition((streamPosTokenizer.getStartPosition() + greedy.length()) - 1);
        consumeGreedy(streamPosTokenizer, greedy);
        return twistNode;
    }

    private ScriptNode parseMacro(StreamPosTokenizer streamPosTokenizer, ScriptNode scriptNode) throws IOException {
        if (this.DEBUG) {
            printVerbose(streamPosTokenizer, "macro", scriptNode);
        }
        switch (streamPosTokenizer.nextToken()) {
        case StreamPosTokenizer.TT_WORD: /* -3 */
            String greedy = parseGreedy(streamPosTokenizer.sval);
            Object obj = this.macroMap.get(greedy);
            if (obj == null) {
                throw new ParseException("Macro: Unexpected or unknown Token.", streamPosTokenizer.getStartPosition(), (streamPosTokenizer.getStartPosition()
                                                                                                                        + greedy.length()) - 1);
            }
            MacroNode macroNode;
            if (obj instanceof String) {
                macroNode = new MacroNode(greedy, (String) obj, streamPosTokenizer.getStartPosition(), (streamPosTokenizer.getStartPosition() + greedy.length())
                                                                                                       - 1);
                this.macroMap.put(greedy, macroNode);
            } else {
                macroNode = (MacroNode) ((MacroNode) obj).cloneSubtree();
                Enumeration<?> enumerationPreorderEnumeration = macroNode.preorderEnumeration();
                while (enumerationPreorderEnumeration.hasMoreElements()) {
                    ScriptNode scriptNode2 = (ScriptNode) enumerationPreorderEnumeration.nextElement();
                    scriptNode2.setStartPosition(streamPosTokenizer.getStartPosition());
                    scriptNode2.setEndPosition((streamPosTokenizer.getStartPosition() + greedy.length()) - 1);
                }
            }
            scriptNode.add(macroNode);
            try {
                macroNode.expand(this);
                consumeGreedy(streamPosTokenizer, greedy);
                return macroNode;
            } catch (IOException e) {
                if (!(e instanceof ParseException)) {
                    throw new ParseException(new StringBuffer().append("Macro '").append(greedy).append("': ").append(e.getMessage()).toString(),
                            streamPosTokenizer.getStartPosition(), (streamPosTokenizer.getStartPosition() + greedy.length()) - 1);
                }
                ParseException parseException = (ParseException) e;
                throw new ParseException(new StringBuffer().append("Macro '").append(greedy).append("': ").append(e.getMessage()).append(" @").append(
                        parseException.getStartPosition()).append("..").append(parseException.getEndPosition()).toString(),
                        streamPosTokenizer.getStartPosition(), (streamPosTokenizer.getStartPosition() + greedy.length()) - 1);
            }
        case StreamPosTokenizer.TT_EOF: /* -1 */
            throw new ParseException("Macro: Token missing.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        default:
            throw new ParseException("Macro: Internal error.", streamPosTokenizer.getStartPosition(), streamPosTokenizer.getEndPosition());
        }
    }

    private String parseGreedy(String str) {
        return (this.transformationMap.get(str) == null && this.permutationMap.get(str) == null && this.macroMap.get(
                str) == null) ? str.length() > 1 ? parseGreedy(str.substring(0, str.length() - 1)) : "\000" : str;
    }

    private String parseGreedyInt(String str) throws NumberFormatException {
        try {
            Integer.parseInt(str);
            return str;
        } catch (NumberFormatException e) {
            return str.length() > 1 ? parseGreedyInt(str.substring(0, str.length() - 1)) : "\000";
        }
    }

    private void consumeGreedy(StreamPosTokenizer streamPosTokenizer, String str) {
        if (str.length() < streamPosTokenizer.sval.length()) {
            streamPosTokenizer.pushBack();
            streamPosTokenizer.setStartPosition(streamPosTokenizer.getStartPosition() + str.length());
            streamPosTokenizer.sval = streamPosTokenizer.sval.substring(str.length());
        }
    }

    public String getFirstToken(int i) {
        if (0 > i || i >= this.tokens.length || this.tokens[i] == null || this.tokens[i].length <= 0) {
            return null;
        }
        return this.tokens[i][0];
    }

    public int getSymbol(String str) {
        Integer num = this.transformationMap.get(str);
        if (num == null) {
            num = this.permutationMap.get(str);
        }
        if (num == null) {
            return -1;
        }
        return num.intValue();
    }

    public static void applyTo(RubiksCubeCore rubiksCubeCore, int i, boolean z) {
        if (0 > i || i >= 84) {
            return;
        }
        int angle = getAngle(i);
        rubiksCubeCore.transform(getAxis(i), getLayerMask(i), z ? -angle : angle);
    }

    public static int getAxis(int i) {
        if (0 > i || i >= 84) {
            return -1;
        }
        return i % 3;
    }

    public static int getAngle(int i) {
        if (0 > i || i >= 84) {
            return 0;
        }
        int i2 = (i / 3) % 2 == 0 ? 1 : -1;
        if (i <= 47) {
            i2 = (i / 6) % 2 == 0 ? i2 : -i2;
        }
        if ((12 <= i && i <= 23) || ((36 <= i && i <= 47) || ((54 <= i && i <= 59) || ((66 <= i && i <= 71) || (78 <= i && i <= 83))))) {
            i2 *= 2;
        }
        return i2;
    }

    public static int getLayerMask(int i) {
        if (0 > i || i > 83) {
            return 0;
        }
        if (i <= 23) {
            return ((i / 3) & 1) == 1 ? 1 : 4;
        }
        if (i <= 47) {
            return ((i / 3) & 1) == 1 ? 3 : 6;
        }
        if (i <= 59) {
            return 2;
        }
        return i <= 71 ? 5 : 7;
    }

    public static int getSymbol(int i, int i2, int i3) {
        if (i == -1 || i2 == 0 || i3 == 0) {
            return 84;
        }
        int i4 = (i3 == 2 || i3 == -2) ? 1 : 0;
        int i5 = i3 < 0 ? 1 : 0;
        switch (i2) {
        case 1:
            return 3 + i + (i4 * 12) + ((1 - i5) * 6);
        case 2:
            return 48 + i + (i4 * 6) + (i5 * 3);
        case 3:
            return 27 + i + (i4 * 12) + ((1 - i5) * 6);
        case D: /* 4 */
            return 0 + i + (i4 * 12) + (i5 * 6);
        case B: /* 5 */
            return 60 + i + (i4 * 12) + (i5 * 6);
        case Ri:/* 6 */
            return 24 + i + (i4 * 12) + (i5 * 6);
        case Ui: /* 7 */
            return 72 + i + (i4 * 6) + (i5 * 3);
        default:
            return 84;
        }
    }

    public static int inverseSymbol(int i) {
        if (i >= 0) {
            if (i <= 47) {
                return (i / 6) % 2 == 0 ? i + 6 : i - 6;
            }
            if (i <= 83) {
                return ((i - 48) / 3) % 2 == 0 ? i + 3 : i - 3;
            }
        }
        return i;
    }

    public static int reflectSymbol(int paramInt) {
        if (0 > paramInt || paramInt > 84) {
            return paramInt;
        }
        int i = getAxis(paramInt);
        int j = getLayerMask(paramInt);
        int k = getAngle(paramInt);
        int m = (j & 0x4) >>> 2 | j & 0x2 | (j & 0x1) << 2;
        return getSymbol(i, m, k);
    }

    public static boolean isRotationSymbol(int i) {
        return 72 <= i && i <= 83;
    }

    public static boolean isMidlayerSymbol(int i) {
        return 48 <= i && i <= 59;
    }

    public static boolean isSliceSymbol(int i) {
        return 60 <= i && i <= 71;
    }

    public static int transformSymbol(int i, int i2) {
        if (i == 96) {
            return reflectSymbol(i2);
        }
        if (i == 95) {
            return inverseSymbol(i2);
        }
        if (i2 >= 84 || i >= 84) {
            return i2;
        }
        int axis = getAxis(i2);
        int axis2 = getAxis(i);
        int angle = getAngle(i2);
        int angle2 = getAngle(i);
        int layerMask = getLayerMask(i2);
        int i3 = -1;
        int i4 = -1;
        int i5 = -1;
        int i6 = ((layerMask & 0x4) >>> 2) | (layerMask & 0x2) | ((layerMask & 0x1) << 2);
        if (axis == axis2 || angle2 == 0) {
            return i2;
        }
        switch (axis2) {
        case 0:
            switch (angle2) {
            case StreamPosTokenizer.TT_NUMBER: /* -2 */
            case 2:
                i3 = axis;
                i4 = -angle;
                i5 = i6;
                break;
            case StreamPosTokenizer.TT_EOF: /* -1 */
                i3 = axis == 2 ? 1 : 2;
                i5 = axis == 2 ? i6 : layerMask;
                i4 = axis == 2 ? -angle : angle;
                break;
            case 1:
                i3 = axis == 1 ? 2 : 1;
                i5 = axis == 1 ? i6 : layerMask;
                i4 = axis == 1 ? -angle : angle;
                break;
            }
        case 1:
            switch (angle2) {
            case StreamPosTokenizer.TT_NUMBER: /* -2 */
            case 2:
                i3 = axis;
                i5 = i6;
                i4 = -angle;
                break;
            case -1:
                i3 = axis == 0 ? 2 : 0;
                i5 = axis == 0 ? i6 : layerMask;
                i4 = axis == 0 ? -angle : angle;
                break;
            case 1:
                i3 = axis == 2 ? 0 : 2;
                i5 = axis == 2 ? i6 : layerMask;
                i4 = axis == 2 ? -angle : angle;
                break;
            }
        case 2:
            switch (angle2) {
            case StreamPosTokenizer.TT_NUMBER: /* -2 */
            case 2:
                i3 = axis;
                i5 = i6;
                i4 = -angle;
                break;
            case -1:
                i3 = axis == 1 ? 0 : 1;
                i5 = axis == 1 ? i6 : layerMask;
                i4 = axis == 1 ? -angle : angle;
                break;
            case 1:
                i3 = axis == 0 ? 1 : 0;
                i5 = axis == 0 ? i6 : layerMask;
                i4 = axis == 0 ? -angle : angle;
                break;
            }
        }
        return getSymbol(i3, i5, i4);
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getClass().getName());
        stringBuffer.append('\n');
        for (int i = 0; i < this.tokens.length; i++) {
            stringBuffer.append(new StringBuffer().append(i).append(":").toString());
            for (int i2 = 0; i2 < this.tokens[i].length; i2++) {
                if (i2 != 0) {
                    stringBuffer.append(",");
                }
                stringBuffer.append(this.tokens[i][i2]);
            }
            stringBuffer.append('\n');
        }
        return stringBuffer.toString();
    }

    private static synchronized String[] getDefaultTokens() {
        if (defaultTokens == null) {
            StringTokenizer stringTokenizer = new StringTokenizer(COMPRESSED_TOKENS, ";", false);
            defaultTokens = new String[stringTokenizer.countTokens()];
            int i = 0;
            while (stringTokenizer.hasMoreTokens()) {
                String str = stringTokenizer.nextToken();
                int index = str.indexOf(' ');
                defaultTokens[i++] = index == -1 ? str : str.substring(0, index);
            }
        }
        return defaultTokens;
    }
}
