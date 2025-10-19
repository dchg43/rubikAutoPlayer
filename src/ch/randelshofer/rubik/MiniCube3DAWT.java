package ch.randelshofer.rubik;

import java.awt.Color;

import ch.randelshofer.geom3d.Shape3D;
import ch.randelshofer.rubik.parserAWT.ScriptParser;

public class MiniCube3DAWT extends AbstractCube3DAWT {
    // Applet默认：正面蓝色，右面橙色，底面白色，背面绿色，左面红色，顶面黄色
    // public static final Color[] STICKER_COLORS = {new Color(0, 51, 115), new Color(255, 70, 0),
    // new Color(248, 248, 248), new Color(0, 115, 47), new Color(140, 0, 15), new Color(255, 210, 0)};
    // Java默认：正面红色，右面蓝色，底面白色，背面橙色，左面绿色，顶面黄色
    // public static final Color[] STICKER_COLORS = {new Color(231, 16, 0), new Color(33, 33, 189),
    // new Color(247, 247, 247), new Color(255, 74, 0), new Color(49, 148, 0), new Color(247, 247, 0)};
    // 常用颜色定义：正面蓝色，右面红色，底面白色，背面绿色，左面橙色，顶面黄色
    // public static final Color[] STICKER_COLORS = {new Color(33, 33, 189), new Color(237, 0, 15),
    // new Color(248, 248, 248), new Color(0, 115, 47), new Color(255, 70, 0), new Color(248, 210, 0)};
    /**
     * 默认各面的颜色，顺序是 正面蓝色，右面红色，底面白色，背面绿色，左面橙色，顶面黄色
     */
    public static final Color[] STICKER_COLORS = {new Color(33, 33, 189), new Color(237, 0, 15), new Color(248, 248, 248), new Color(0, 115, 47),
            new Color(255, 70, 0), new Color(248, 210, 0)};

    private static final double[] fArr = {-8.0d, 8.0d, 9.0d, -8.0d, -8.0d, 9.0d, 8.0d, 8.0d, 9.0d, 8.0d, -8.0d, 9.0d, 8.0d, 8.0d, -9.0d, 8.0d, -8.0d, -9.0d,
            -8.0d, 8.0d, -9.0d, -8.0d, -8.0d, -9.0d, -9.0d, 8.0d, 8.0d, -9.0d, -8.0d, 8.0d, 9.0d, 8.0d, 8.0d, 9.0d, -8.0d, 8.0d, 9.0d, 8.0d, -8.0d, 9.0d, -8.0d,
            -8.0d, -9.0d, 8.0d, -8.0d, -9.0d, -8.0d, -8.0d, -8.0d, 9.0d, 8.0d, -8.0d, -9.0d, 8.0d, 8.0d, 9.0d, 8.0d, 8.0d, -9.0d, 8.0d, 8.0d, 9.0d, -8.0d, 8.0d,
            -9.0d, -8.0d, -8.0d, 9.0d, -8.0d, -8.0d, -9.0d, -8.0d};

    /**
     * 设置每块显示的颜色
     * face: 指定面
     * sticker: 指定块
     */
    @Override
    public void setStickerColor(int face, int sticker, Color color) {
        if (color == null) {
            return;
        }

        switch (face) {
        case 0:
            switch (sticker) {
            case 0:
                this.cornerShapes[0].setBackgroundColor(1, color);
                break;
            case 1:
                this.edgeShapes[0].setBackgroundColor(1, color);
                break;
            case 2:
                this.cornerShapes[2].setBackgroundColor(2, color);
                break;
            case 3:
                this.edgeShapes[1].setBackgroundColor(0, color);
                break;
            case ScriptParser.D: /* 4 */
                this.sideShapes[0].setBackgroundColor(0, color);
                break;
            case ScriptParser.B: /* 5 */
                this.edgeShapes[4].setBackgroundColor(0, color);
                break;
            case ScriptParser.Ri: /* 6 */
                this.cornerShapes[1].setBackgroundColor(2, color);
                break;
            case ScriptParser.Ui: /* 7 */
                this.edgeShapes[2].setBackgroundColor(1, color);
                break;
            case ScriptParser.Fi: /* 8 */
                this.cornerShapes[3].setBackgroundColor(1, color);
                break;
            }
            break;
        case 1:
            switch (sticker) {
            case 0:
                this.cornerShapes[2].setBackgroundColor(1, color);
                break;
            case 1:
                this.edgeShapes[3].setBackgroundColor(0, color);
                break;
            case 2:
                this.cornerShapes[4].setBackgroundColor(2, color);
                break;
            case 3:
                this.edgeShapes[4].setBackgroundColor(1, color);
                break;
            case ScriptParser.D: /* 4 */
                this.sideShapes[1].setBackgroundColor(0, color);
                break;
            case ScriptParser.B: /* 5 */
                this.edgeShapes[7].setBackgroundColor(1, color);
                break;
            case ScriptParser.Ri: /* 6 */
                this.cornerShapes[3].setBackgroundColor(2, color);
                break;
            case ScriptParser.Ui: /* 7 */
                this.edgeShapes[5].setBackgroundColor(0, color);
                break;
            case ScriptParser.Fi: /* 8 */
                this.cornerShapes[5].setBackgroundColor(1, color);
                break;
            }
            break;
        case 2:
            switch (sticker) {
            case 0:
                this.cornerShapes[1].setBackgroundColor(0, color);
                break;
            case 1:
                this.edgeShapes[2].setBackgroundColor(0, color);
                break;
            case 2:
                this.cornerShapes[3].setBackgroundColor(0, color);
                break;
            case 3:
                this.edgeShapes[11].setBackgroundColor(1, color);
                break;
            case ScriptParser.D: /* 4 */
                this.sideShapes[2].setBackgroundColor(0, color);
                break;
            case ScriptParser.B: /* 5 */
                this.edgeShapes[5].setBackgroundColor(1, color);
                break;
            case ScriptParser.Ri: /* 6 */
                this.cornerShapes[7].setBackgroundColor(0, color);
                break;
            case ScriptParser.Ui: /* 7 */
                this.edgeShapes[8].setBackgroundColor(0, color);
                break;
            case ScriptParser.Fi: /* 8 */
                this.cornerShapes[5].setBackgroundColor(0, color);
                break;
            }
            break;
        case 3:
            switch (sticker) {
            case 0:
                this.cornerShapes[4].setBackgroundColor(1, color);
                break;
            case 1:
                this.edgeShapes[6].setBackgroundColor(1, color);
                break;
            case 2:
                this.cornerShapes[6].setBackgroundColor(2, color);
                break;
            case 3:
                this.edgeShapes[7].setBackgroundColor(0, color);
                break;
            case ScriptParser.D: /* 4 */
                this.sideShapes[3].setBackgroundColor(0, color);
                break;
            case ScriptParser.B: /* 5 */
                this.edgeShapes[10].setBackgroundColor(0, color);
                break;
            case ScriptParser.Ri: /* 6 */
                this.cornerShapes[5].setBackgroundColor(2, color);
                break;
            case ScriptParser.Ui: /* 7 */
                this.edgeShapes[8].setBackgroundColor(1, color);
                break;
            case ScriptParser.Fi: /* 8 */
                this.cornerShapes[7].setBackgroundColor(1, color);
                break;
            }
            break;
        case ScriptParser.D: /* 4 */
            switch (sticker) {
            case 0:
                this.cornerShapes[6].setBackgroundColor(1, color);
                break;
            case 1:
                this.edgeShapes[9].setBackgroundColor(0, color);
                break;
            case 2:
                this.cornerShapes[0].setBackgroundColor(2, color);
                break;
            case 3:
                this.edgeShapes[10].setBackgroundColor(1, color);
                break;
            case ScriptParser.D: /* 4 */
                this.sideShapes[4].setBackgroundColor(0, color);
                break;
            case ScriptParser.B: /* 5 */
                this.edgeShapes[1].setBackgroundColor(1, color);
                break;
            case ScriptParser.Ri: /* 6 */
                this.cornerShapes[7].setBackgroundColor(2, color);
                break;
            case ScriptParser.Ui: /* 7 */
                this.edgeShapes[11].setBackgroundColor(0, color);
                break;
            case ScriptParser.Fi: /* 8 */
                this.cornerShapes[1].setBackgroundColor(1, color);
                break;
            }
            break;
        case ScriptParser.B: /* 5 */
            switch (sticker) {
            case 0:
                this.cornerShapes[6].setBackgroundColor(0, color);
                break;
            case 1:
                this.edgeShapes[6].setBackgroundColor(0, color);
                break;
            case 2:
                this.cornerShapes[4].setBackgroundColor(0, color);
                break;
            case 3:
                this.edgeShapes[9].setBackgroundColor(1, color);
                break;
            case ScriptParser.D: /* 4 */
                this.sideShapes[5].setBackgroundColor(0, color);
                break;
            case ScriptParser.B: /* 5 */
                this.edgeShapes[3].setBackgroundColor(1, color);
                break;
            case ScriptParser.Ri: /* 6 */
                this.cornerShapes[0].setBackgroundColor(0, color);
                break;
            case ScriptParser.Ui: /* 7 */
                this.edgeShapes[0].setBackgroundColor(0, color);
                break;
            case ScriptParser.Fi: /* 8 */
                this.cornerShapes[2].setBackgroundColor(0, color);
                break;
            }
            break;
        }
    }

    /**
     * 获取每块的颜色
     * face: 指定面
     * sticker: 指定块
     */
    @Override
    public Color getStickerColor(int face, int sticker) {
        switch (face) {
        case 0:
            switch (sticker) {
            case 0:
                return this.cornerShapes[0].getBackgroundColor(1);
            case 1:
                return this.edgeShapes[0].getBackgroundColor(1);
            case 2:
                return this.cornerShapes[2].getBackgroundColor(2);
            case 3:
                return this.edgeShapes[1].getBackgroundColor(0);
            case ScriptParser.D: /* 4 */
                return this.sideShapes[0].getBackgroundColor(0);
            case ScriptParser.B: /* 5 */
                return this.edgeShapes[4].getBackgroundColor(0);
            case ScriptParser.Ri: /* 6 */
                return this.cornerShapes[1].getBackgroundColor(2);
            case ScriptParser.Ui: /* 7 */
                return this.edgeShapes[2].getBackgroundColor(1);
            case ScriptParser.Fi: /* 8 */
                return this.cornerShapes[3].getBackgroundColor(1);
            }
        case 1:
            switch (sticker) {
            case 0:
                return this.cornerShapes[2].getBackgroundColor(1);
            case 1:
                return this.edgeShapes[3].getBackgroundColor(0);
            case 2:
                return this.cornerShapes[4].getBackgroundColor(2);
            case 3:
                return this.edgeShapes[4].getBackgroundColor(1);
            case ScriptParser.D: /* 4 */
                return this.sideShapes[1].getBackgroundColor(0);
            case ScriptParser.B: /* 5 */
                return this.edgeShapes[7].getBackgroundColor(1);
            case ScriptParser.Ri: /* 6 */
                return this.cornerShapes[3].getBackgroundColor(2);
            case ScriptParser.Ui: /* 7 */
                return this.edgeShapes[5].getBackgroundColor(0);
            case ScriptParser.Fi: /* 8 */
                return this.cornerShapes[5].getBackgroundColor(1);
            }
        case 2:
            switch (sticker) {
            case 0:
                return this.cornerShapes[1].getBackgroundColor(0);
            case 1:
                return this.edgeShapes[2].getBackgroundColor(0);
            case 2:
                return this.cornerShapes[3].getBackgroundColor(0);
            case 3:
                return this.edgeShapes[11].getBackgroundColor(1);
            case ScriptParser.D: /* 4 */
                return this.sideShapes[2].getBackgroundColor(0);
            case ScriptParser.B: /* 5 */
                return this.edgeShapes[5].getBackgroundColor(1);
            case ScriptParser.Ri: /* 6 */
                return this.cornerShapes[7].getBackgroundColor(0);
            case ScriptParser.Ui: /* 7 */
                return this.edgeShapes[8].getBackgroundColor(0);
            case ScriptParser.Fi: /* 8 */
                return this.cornerShapes[5].getBackgroundColor(0);
            }
        case 3:
            switch (sticker) {
            case 0:
                return this.cornerShapes[4].getBackgroundColor(1);
            case 1:
                return this.edgeShapes[6].getBackgroundColor(1);
            case 2:
                return this.cornerShapes[6].getBackgroundColor(2);
            case 3:
                return this.edgeShapes[7].getBackgroundColor(0);
            case ScriptParser.D: /* 4 */
                return this.sideShapes[3].getBackgroundColor(0);
            case ScriptParser.B: /* 5 */
                return this.edgeShapes[10].getBackgroundColor(0);
            case ScriptParser.Ri: /* 6 */
                return this.cornerShapes[5].getBackgroundColor(2);
            case ScriptParser.Ui: /* 7 */
                return this.edgeShapes[8].getBackgroundColor(1);
            case ScriptParser.Fi: /* 8 */
                return this.cornerShapes[7].getBackgroundColor(1);
            }
        case ScriptParser.D: /* 4 */
            switch (sticker) {
            case 0:
                return this.cornerShapes[6].getBackgroundColor(1);
            case 1:
                return this.edgeShapes[9].getBackgroundColor(0);
            case 2:
                return this.cornerShapes[0].getBackgroundColor(2);
            case 3:
                return this.edgeShapes[10].getBackgroundColor(1);
            case ScriptParser.D: /* 4 */
                return this.sideShapes[4].getBackgroundColor(0);
            case ScriptParser.B: /* 5 */
                return this.edgeShapes[1].getBackgroundColor(1);
            case ScriptParser.Ri: /* 6 */
                return this.cornerShapes[7].getBackgroundColor(2);
            case ScriptParser.Ui: /* 7 */
                return this.edgeShapes[11].getBackgroundColor(0);
            case ScriptParser.Fi: /* 8 */
                return this.cornerShapes[1].getBackgroundColor(1);
            }
        case ScriptParser.B: /* 5 */
            switch (sticker) {
            case 0:
                return this.cornerShapes[6].getBackgroundColor(0);
            case 1:
                return this.edgeShapes[6].getBackgroundColor(0);
            case 2:
                return this.cornerShapes[4].getBackgroundColor(0);
            case 3:
                return this.edgeShapes[9].getBackgroundColor(1);
            case ScriptParser.D: /* 4 */
                return this.sideShapes[5].getBackgroundColor(0);
            case ScriptParser.B: /* 5 */
                return this.edgeShapes[3].getBackgroundColor(1);
            case ScriptParser.Ri: /* 6 */
                return this.cornerShapes[0].getBackgroundColor(0);
            case ScriptParser.Ui: /* 7 */
                return this.edgeShapes[0].getBackgroundColor(0);
            case ScriptParser.Fi: /* 8 */
                return this.cornerShapes[2].getBackgroundColor(0);
            }
        }
        throw new IllegalArgumentException("Sticker not found: " + face + ":" + sticker);
    }

    /**
     * 初始化角块颜色
     */
    @Override
    protected void initCorners() {
        int[][] iArr = {{16, 22, 20, 18}, {0, 2, 3, 1}, {14, 8, 9, 15}, {12, 13, 11, 10}, {17, 19, 21, 23}, {4, 6, 7, 5}, {17, 9, 1}, {19, 3, 11}, {23, 7, 15},
                {16, 0, 8}, {18, 10, 2}, {22, 14, 6}, {20, 4, 12}, {16, 18, 2, 0}, {18, 20, 12, 10}, {20, 22, 6, 4}, {22, 16, 8, 14}, {19, 17, 1, 3},
                {21, 19, 11, 13}, {23, 21, 5, 7}, {17, 23, 15, 9}, {3, 2, 10, 11}, {0, 1, 9, 8}, {4, 5, 13, 12}, {7, 6, 14, 15}};
        Color[][][] colorArr = new Color[8][iArr.length][0];
        colorArr[0][0] = new Color[]{STICKER_COLORS[5], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[0][1] = new Color[]{STICKER_COLORS[0], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[0][2] = new Color[]{STICKER_COLORS[4], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[1][0] = new Color[]{STICKER_COLORS[2], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[1][1] = new Color[]{STICKER_COLORS[4], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[1][2] = new Color[]{STICKER_COLORS[0], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[2][0] = new Color[]{STICKER_COLORS[5], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[2][1] = new Color[]{STICKER_COLORS[1], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[2][2] = new Color[]{STICKER_COLORS[0], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[3][0] = new Color[]{STICKER_COLORS[2], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[3][1] = new Color[]{STICKER_COLORS[0], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[3][2] = new Color[]{STICKER_COLORS[1], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[4][0] = new Color[]{STICKER_COLORS[5], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[4][1] = new Color[]{STICKER_COLORS[3], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[4][2] = new Color[]{STICKER_COLORS[1], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[5][0] = new Color[]{STICKER_COLORS[2], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[5][1] = new Color[]{STICKER_COLORS[1], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[5][2] = new Color[]{STICKER_COLORS[3], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[6][0] = new Color[]{STICKER_COLORS[5], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[6][1] = new Color[]{STICKER_COLORS[4], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[6][2] = new Color[]{STICKER_COLORS[3], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[7][0] = new Color[]{STICKER_COLORS[2], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[7][1] = new Color[]{STICKER_COLORS[3], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[7][2] = new Color[]{STICKER_COLORS[4], AbstractCube3DAWT.PART_BORDER_COLOR};
        Color[] colorArr2 = {AbstractCube3DAWT.PART_FILL_COLOR, null};
        for (int i = 0; i < 8; i++) {
            for (int j = 3; j < iArr.length; j++) {
                colorArr[i][j] = colorArr2;
            }
            this.cornerShapes[i] = new Shape3D(fArr, iArr, colorArr[i]);
        }
    }

    /**
     * 初始化棱块颜色
     */
    @Override
    protected void initEdges() {
        int[][] iArr = {{0, 2, 3, 1}, {16, 22, 20, 18}, {14, 8, 9, 15}, {12, 13, 11, 10}, {17, 19, 21, 23}, {4, 6, 7, 5}, {17, 9, 1}, {19, 3, 11}, {16, 0, 8},
                {18, 10, 2}, {22, 14, 6}, {20, 4, 12}, {16, 18, 2, 0}, {18, 20, 12, 10}, {20, 22, 6, 4}, {22, 16, 8, 14}, {19, 17, 1, 3}, {21, 19, 11, 13},
                {17, 23, 15, 9}, {3, 2, 10, 11}, {0, 1, 9, 8}, {4, 5, 13, 12}, {7, 6, 14, 15}};
        Color[][][] colorArr = new Color[12][iArr.length][0];
        colorArr[0][0] = new Color[]{STICKER_COLORS[5], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[0][1] = new Color[]{STICKER_COLORS[0], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[1][0] = new Color[]{STICKER_COLORS[0], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[1][1] = new Color[]{STICKER_COLORS[4], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[2][0] = new Color[]{STICKER_COLORS[2], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[2][1] = new Color[]{STICKER_COLORS[0], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[3][0] = new Color[]{STICKER_COLORS[1], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[3][1] = new Color[]{STICKER_COLORS[5], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[4][0] = new Color[]{STICKER_COLORS[0], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[4][1] = new Color[]{STICKER_COLORS[1], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[5][0] = new Color[]{STICKER_COLORS[1], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[5][1] = new Color[]{STICKER_COLORS[2], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[6][0] = new Color[]{STICKER_COLORS[5], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[6][1] = new Color[]{STICKER_COLORS[3], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[7][0] = new Color[]{STICKER_COLORS[3], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[7][1] = new Color[]{STICKER_COLORS[1], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[8][0] = new Color[]{STICKER_COLORS[2], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[8][1] = new Color[]{STICKER_COLORS[3], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[9][0] = new Color[]{STICKER_COLORS[4], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[9][1] = new Color[]{STICKER_COLORS[5], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[10][0] = new Color[]{STICKER_COLORS[3], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[10][1] = new Color[]{STICKER_COLORS[4], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[11][0] = new Color[]{STICKER_COLORS[4], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[11][1] = new Color[]{STICKER_COLORS[2], AbstractCube3DAWT.PART_BORDER_COLOR};
        Color[] colorArr2 = {AbstractCube3DAWT.PART_FILL_COLOR, null};
        for (int i = 0; i < 12; i++) {
            for (int j = 2; j < iArr.length; j++) {
                colorArr[i][j] = colorArr2;
            }
            this.edgeShapes[i] = new Shape3D(fArr, iArr, colorArr[i]);
        }
    }

    /**
     * 初始化中心块颜色
     */
    @Override
    protected void initSides() {
        int[][] iArr = {{0, 2, 3, 1}, {16, 22, 20, 18}, {14, 8, 9, 15}, {12, 13, 11, 10}, {17, 19, 21, 23}, {17, 9, 1}, {19, 3, 11}, {16, 0, 8}, {18, 10, 2},
                {16, 18, 2, 0}, {18, 20, 12, 10}, {22, 16, 8, 14}, {19, 17, 1, 3}, {21, 19, 11, 13}, {17, 23, 15, 9}, {3, 2, 10, 11}, {0, 1, 9, 8}};
        Color[][][] colorArr = new Color[6][iArr.length][0];
        colorArr[0][0] = new Color[]{STICKER_COLORS[0], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[1][0] = new Color[]{STICKER_COLORS[1], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[2][0] = new Color[]{STICKER_COLORS[2], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[3][0] = new Color[]{STICKER_COLORS[3], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[4][0] = new Color[]{STICKER_COLORS[4], AbstractCube3DAWT.PART_BORDER_COLOR};
        colorArr[5][0] = new Color[]{STICKER_COLORS[5], AbstractCube3DAWT.PART_BORDER_COLOR};
        Color[] colorArr2 = {AbstractCube3DAWT.PART_FILL_COLOR, null};
        for (int i = 0; i < 6; i++) {
            for (int j = 1; j < iArr.length; j++) {
                colorArr[i][j] = colorArr2;
            }
            this.sideShapes[i] = new Shape3D(fArr, iArr, colorArr[i]);
        }
    }

    @Override
    protected void initCenter() {
        this.centerShape = new Shape3D(new double[0], new int[0][], new Color[0][]);
        this.centerShape.setVisible(false);
    }

    /**
     * 初始化每个方块的动作
     * 总共8个角，每个角3个角块，绑定动作CornerAction
     * 总共12个棱，每个棱2个棱块，绑定动作EdgeAction
     * 总共6个平面，每个面1个中心块，绑定动作SideAction
     */
    @Override
    protected void initActions() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 3; j++) {
                this.cornerShapes[i].setAction(j, new AbstractCube3DAWT.CornerAction(this, i, j));
            }
        }
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 2; j++) {
                this.edgeShapes[i].setAction(j, new AbstractCube3DAWT.EdgeAction(this, i, j));
            }
        }
        for (int i = 0; i < 6; i++) {
            this.sideShapes[i].setAction(0, new AbstractCube3DAWT.SideAction(this, i));
        }
    }

    @Override
    public String getName() {
        return "Rubik's Cube (simplified 3D model)";
    }
}
