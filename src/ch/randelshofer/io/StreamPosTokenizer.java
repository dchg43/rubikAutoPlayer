package ch.randelshofer.io;

import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

import ch.randelshofer.rubik.parserAWT.ScriptParser;

public class StreamPosTokenizer {
    private Reader reader;

    private int readpos;

    private int startpos;

    private int endpos;

    private Vector<Integer> unread;

    private char[] buf;

    private int peekc;

    private static final int NEED_CHAR = Integer.MAX_VALUE;

    private static final int SKIP_LF = NEED_CHAR - 1;

    private boolean pushedBack;

    private boolean forceLower;

    private int LINENO;

    private boolean eolIsSignificantP;

    private boolean slashSlashCommentsP;

    private boolean slashStarCommentsP;

    private char[] slashSlash;

    private char[] slashStar;

    private char[] starSlash;

    private byte[] ctype;

    private static final byte CT_WHITESPACE = 1;

    private static final byte CT_DIGIT = 2;

    private static final byte CT_ALPHA = 4;

    private static final byte CT_QUOTE = 8;

    private static final byte CT_COMMENT = 16;

    public int ttype;

    public static final int TT_EOF = -1;

    public static final int TT_EOL = 10;

    public static final int TT_NUMBER = -2;

    public static final int TT_WORD = -3;

    private static final int TT_NOTHING = -4;

    public String sval;

    public double nval;

    private StreamPosTokenizer() {
        this.reader = null;
        this.readpos = 0;
        this.startpos = -1;
        this.endpos = -1;
        this.unread = new Vector<>();
        this.buf = new char[20];
        this.peekc = NEED_CHAR;
        this.LINENO = 1;
        this.eolIsSignificantP = false;
        this.slashSlashCommentsP = false;
        this.slashStarCommentsP = false;
        this.slashSlash = new char[]{'/', '/'};
        this.slashStar = new char[]{'/', '*'};
        this.starSlash = new char[]{'*', '/'};
        this.ctype = new byte[256];
        this.ttype = TT_NOTHING;
        wordChars(97, 122);
        wordChars(65, 90);
        wordChars(160, 255);
        whitespaceChars(0, 32);
        commentChar(47);
        quoteChar(34);
        quoteChar(39);
        parseNumbers();
    }

    public StreamPosTokenizer(Reader reader) {
        this();
        if (reader == null) {
            throw new NullPointerException();
        }
        this.reader = reader;
    }

    public void resetSyntax() {
        int i = this.ctype.length;
        while (--i >= 0) {
            this.ctype[i] = 0;
        }
    }

    public void wordChars(int i, int i2) {
        if (i < 0) {
            i = 0;
        }
        if (i2 >= this.ctype.length) {
            i2 = this.ctype.length - 1;
        }
        while (i <= i2) {
            this.ctype[i] = (byte) (this.ctype[i] | 0x4);
            i++;
        }
    }

    public void whitespaceChars(int i, int i2) {
        if (i < 0) {
            i = 0;
        }
        if (i2 >= this.ctype.length) {
            i2 = this.ctype.length - 1;
        }
        while (i <= i2) {
            this.ctype[i++] = 1;
        }
    }

    public void ordinaryChars(int i, int i2) {
        if (i < 0) {
            i = 0;
        }
        if (i2 >= this.ctype.length) {
            i2 = this.ctype.length - 1;
        }
        while (i <= i2) {
            this.ctype[i++] = 0;
        }
    }

    public void ordinaryChar(int i) {
        if (i >= 0 && i < this.ctype.length) {
            this.ctype[i] = 0;
        }
    }

    public void commentChar(int i) {
        if (i >= 0 && i < this.ctype.length) {
            this.ctype[i] = 16;
        }
    }

    public void quoteChar(int i) {
        if (i >= 0 && i < this.ctype.length) {
            this.ctype[i] = 8;
        }
    }

    public void parseNumbers() {
        for (int i = 48; i <= 57; i++) {
            this.ctype[i] = (byte) (this.ctype[i] | 0x2);
        }
        this.ctype[46] = (byte) (this.ctype[46] | 0x2);
        this.ctype[45] = (byte) (this.ctype[45] | 0x2);
    }

    public void eolIsSignificant(boolean eolIsSignificantP) {
        this.eolIsSignificantP = eolIsSignificantP;
    }

    public void slashStarComments(boolean slashStarCommentsP) {
        this.slashStarCommentsP = slashStarCommentsP;
    }

    public void slashSlashComments(boolean slashSlashCommentsP) {
        this.slashSlashCommentsP = slashSlashCommentsP;
    }

    public void lowerCaseMode(boolean forceLower) {
        this.forceLower = forceLower;
    }

    private int read() throws IOException {
        int iIntValue;
        if (this.unread.size() > 0) {
            iIntValue = this.unread.lastElement().intValue();
            this.unread.removeElementAt(this.unread.size() - 1);
        } else {
            iIntValue = this.reader.read();
        }
        if (iIntValue != -1) {
            this.readpos++;
        }
        return iIntValue;
    }

    private void unread(int i) {
        this.unread.addElement(i);
        this.readpos--;
    }

    // TODO:
    public int nextToken() throws IOException {
        int i;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        double d;
        if (this.pushedBack) {
            this.pushedBack = false;
            return this.ttype;
        }
        byte[] bArr = this.ctype;
        this.sval = null;
        int i12 = this.peekc;
        if (i12 < 0) {
            i12 = NEED_CHAR;
        }
        if (i12 == SKIP_LF) {
            i12 = read();
            if (i12 < 0) {
                this.startpos = this.endpos = this.readpos - 1;
                this.ttype = -1;
                return -1;
            }
            if (i12 == 10) {
                i12 = NEED_CHAR;
            }
        }
        if (i12 == NEED_CHAR && (i12 = read()) < 0) {
            this.startpos = this.endpos = this.readpos - 1;
            this.ttype = -1;
            return -1;
        }
        this.ttype = i12;
        this.peekc = NEED_CHAR;
        byte b = i12 < 256 ? bArr[i12] : CT_ALPHA;
        while ((b & CT_WHITESPACE) != 0) {
            if (i12 == 13) {
                this.LINENO++;
                if (this.eolIsSignificantP) {
                    this.peekc = SKIP_LF;
                    int i16 = this.readpos - 1;
                    this.endpos = i16;
                    this.startpos = i16;
                    this.ttype = 10;
                    return 10;
                }
                int i17 = read();
                i11 = i17 == 10 ? read() : i17;
            } else {
                if (i12 == 10) {
                    this.LINENO++;
                    if (this.eolIsSignificantP) {
                        int i18 = this.readpos - 1;
                        this.endpos = i18;
                        this.startpos = i18;
                        this.ttype = 10;
                        return 10;
                    }
                }
                i11 = read();
            }
            if (i11 < 0) {
                int i19 = this.readpos;
                this.endpos = i19;
                this.startpos = i19;
                this.ttype = -1;
                return -1;
            }
            b = i11 < 256 ? bArr[i11] : CT_ALPHA;
            i12 = i11;
        }
        this.startpos = this.readpos - 1;
        if ((b & CT_DIGIT) != 0) {
            int i20 = 0;
            boolean z = false;
            if (i12 == 45) {
                i9 = read();
                if (i9 == 46 || (i9 >= 48 && i9 <= 57)) {
                    z = true;
                } else {
                    this.peekc = i9;
                    unread(i9);
                    i = 45;
                }
            } else {
                i9 = i12;
            }
            double d2 = 0.0d;
            int i21 = 0;
            int i22 = 0;
            int i23 = i9;
            while (true) {
                if (i23 == 46 && i22 == 0) {
                    i10 = 1;
                } else {
                    if (48 > i23 || i23 > 57) {
                        break;
                    }
                    i20++;
                    d2 = (d2 * 10.0d) + (i23 - 48);
                    i21 += i22;
                    i10 = i22;
                }
                i23 = read();
                i22 = i10;
            }
            this.peekc = i23;
            if (i21 != 0) {
                double d3 = 10.0d;
                for (int i24 = i21 - 1; i24 > 0; i24--) {
                    d3 *= 10.0d;
                }
                d = d2 / d3;
            } else {
                d = d2;
            }
            if (z) {
                d = -d;
            }
            this.nval = d;
            this.endpos = i23 == -1 ? this.readpos - 1 : this.readpos - 2;
            if (i20 != 0) {
                this.ttype = -2;
                return -2;
            }
            unread(i23);
            if (z) {
                unread(46);
                i = 45;
            } else {
                i = 46;
            }
        } else {
            i = i12;
        }
        if ((b & CT_ALPHA) != 0) {
            int i25 = 0;
            int i26 = i;
            while (true) {
                if (i25 >= this.buf.length) {
                    char[] cArr = new char[this.buf.length * 2];
                    System.arraycopy(this.buf, 0, cArr, 0, this.buf.length);
                    this.buf = cArr;
                }
                i8 = i25 + 1;
                this.buf[i25] = (char) i26;
                i26 = read();
                if (((i26 < 0 ? CT_WHITESPACE : i26 < 256 ? bArr[i26] : CT_ALPHA) & 6) == 0) {
                    break;
                }
                i25 = i8;
            }
            this.peekc = i26;
            this.sval = String.copyValueOf(this.buf, 0, i8);
            if (this.forceLower) {
                this.sval = this.sval.toLowerCase();
            }
            this.endpos = i26 == -1 ? this.readpos - 1 : this.readpos - 2;
            this.ttype = -3;
            return -3;
        }
        if ((b & CT_QUOTE) != 0) {
            this.ttype = i;
            int i27 = read();
            int i28 = 0;
            while (i27 >= 0 && i27 != this.ttype && i27 != 10 && i27 != 13) {
                if (i27 == 92) {
                    int i29 = read();
                    if (i29 < 48 || i29 > 55) {
                        switch (i29) {
                        case 97:
                            i29 = 7;
                            break;
                        case 98:
                            i29 = 8;
                            break;
                        case 102:
                            i29 = 12;
                            break;
                        case 110:
                            i29 = 10;
                            break;
                        case 114:
                            i29 = 13;
                            break;
                        case 116:
                            i29 = 9;
                            break;
                        case 118:
                            i29 = 11;
                            break;
                        }
                        i7 = read();
                        i27 = i29;
                    } else {
                        i27 = i29 - 48;
                        i7 = read();
                        if (48 <= i7 && i7 <= 55) {
                            i27 = (i27 << 3) + (i7 - 48);
                            i7 = read();
                            if (48 <= i7 && i7 <= 55 && i29 <= 51) {
                                i27 = (i27 << 3) + (i7 - 48);
                                i7 = read();
                            }
                        }
                    }
                } else {
                    i7 = read();
                }
                if (i28 >= this.buf.length) {
                    char[] cArr2 = new char[this.buf.length * 2];
                    System.arraycopy(this.buf, 0, cArr2, 0, this.buf.length);
                    this.buf = cArr2;
                }
                this.buf[i28] = (char) i27;
                i27 = i7;
                i28++;
            }
            if (i27 == this.ttype) {
                i27 = NEED_CHAR;
            }
            this.peekc = i27;
            this.sval = String.copyValueOf(this.buf, 0, i28);
            this.endpos = this.readpos - 2;
            return this.ttype;
        }
        if ((!this.slashSlashCommentsP || i != this.slashSlash[0]) && (!this.slashStarCommentsP || i != this.slashStar[0])) {
            if ((b & CT_COMMENT) == 0) {
                this.endpos = this.readpos - 1;
                this.ttype = i;
                return i;
            }
            int i2;
            do {
                i2 = read();
                if (i2 == 10 || i2 == 13) {
                    break;
                }
            } while (i2 >= 0);
            this.peekc = i2;
            return nextToken();
        }
        if (i == this.slashStar[0] && this.slashStar.length == 1) {
            do {
                i6 = read();
                if (i6 == this.starSlash[0]) {
                    return nextToken();
                }
                if (i6 == 13) {
                    this.LINENO++;
                    i6 = read();
                    if (i6 == 10) {
                        i6 = read();
                    }
                } else if (i6 == 10) {
                    this.LINENO++;
                    i6 = read();
                }
            } while (i6 >= 0);
            this.endpos = this.readpos;
            this.ttype = -1;
            return -1;
        }
        if (i == this.slashSlash[0] && this.slashSlash.length == 1) {
            do {
                i5 = read();
                if (i5 == 10 || i5 == 13) {
                    break;
                }
            } while (i5 >= 0);
            this.peekc = i5;
            return nextToken();
        }
        int i30 = read();
        if (i30 == this.slashStar[1] && this.slashStarCommentsP) {
            int i31 = 0;
            do {
                int i32 = read();
                if (i32 == this.starSlash[1] && i31 == this.starSlash[0]) {
                    return nextToken();
                }
                if (i32 == 13) {
                    this.LINENO++;
                    i31 = read();
                    if (i31 == 10) {
                        i31 = read();
                    }
                } else if (i32 == 10) {
                    this.LINENO++;
                    i31 = read();
                } else {
                    i31 = i32;
                }
            } while (i31 >= 0);
            this.endpos = this.readpos;
            this.ttype = -1;
            return -1;
        }
        if (i30 == this.slashSlash[1] && this.slashSlashCommentsP) {
            int i4;
            do {
                i4 = read();
                if (i4 == 10 || i4 == 13) {
                    break;
                }
            } while (i4 >= 0);
            this.peekc = i4;
            return nextToken();
        }
        if ((bArr[this.slashSlash[0]] & CT_COMMENT) == 0) {
            this.peekc = i30;
            this.endpos = this.readpos - 2;
            char c = this.slashSlash[0];
            this.ttype = c;
            return c;
        }
        int i3;
        do {
            i3 = read();
            if (i3 == 10 || i3 == 13) {
                break;
            }
        } while (i3 >= 0);
        this.peekc = i3;
        return nextToken();
    }

    public void setSlashStarTokens(String slashStar, String starSlash) {
        if (slashStar.length() != starSlash.length()) {
            throw new IllegalArgumentException(new StringBuilder().append("SlashStar and StarSlash tokens must be of same length: '").append(slashStar).append(
                    "' '").append(starSlash).append("'").toString());
        }
        if (slashStar.length() < 1 || slashStar.length() > 2) {
            throw new IllegalArgumentException(new StringBuilder().append("SlashStar and StarSlash tokens must be of length 1 or 2: '").append(
                    slashStar).append("' '").append(starSlash).append("'").toString());
        }
        this.slashStar = slashStar.toCharArray();
        this.starSlash = starSlash.toCharArray();
        commentChar(this.slashStar[0]);
    }

    public void setSlashSlashToken(String slashSlash) {
        if (slashSlash.length() < 1 || slashSlash.length() > 2) {
            throw new IllegalArgumentException(new StringBuilder().append("SlashSlash token must be of length 1 or 2: '").append(slashSlash).append(
                    "'").toString());
        }
        this.slashSlash = slashSlash.toCharArray();
        commentChar(this.slashSlash[0]);
    }

    public void pushBack() {
        if (this.ttype != TT_NOTHING) {
            this.pushedBack = true;
        }
    }

    public int lineno() {
        return this.LINENO;
    }

    public int getStartPosition() {
        return this.startpos;
    }

    public void setStartPosition(int i) {
        this.startpos = i;
    }

    public int getEndPosition() {
        return this.endpos;
    }

    @Override
    public String toString() {
        String str;
        switch (this.ttype) {
        case TT_NOTHING: /* -4 */
            str = "NOTHING";
            break;
        case TT_WORD: /* -3 */
            str = this.sval;
            break;
        case TT_NUMBER: /* -2 */
            str = "n=" + this.nval;
            break;
        case -1:
            str = "EOF";
            break;
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case ScriptParser.B: /* 5 */
        case ScriptParser.Ri: /* 6 */
        case ScriptParser.Ui: /* 7 */
        case 8:
        case ScriptParser.Li: /* 9 */
        default:
            str = new String(new char[]{'\'', (char) this.ttype, '\''});
            break;
        case 10:
            str = "EOL";
            break;
        }
        return new StringBuilder().append("Token[").append(str).append("], line ").append(this.LINENO).toString();
    }
}
