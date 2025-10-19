package ch.randelshofer.rubik;

import java.util.Arrays;
import java.util.List;

import ch.randelshofer.gui.event.EventListenerList;
import ch.randelshofer.gui.event.EventListenerList.ListenerNode;
import ch.randelshofer.rubik.parserAWT.ScriptParser;

public class RubiksCubeCore implements Cloneable {
    // 8个角块位置信息
    // 8个角块形成的正方体中4个竖直棱中，从左下开始，每个棱从上往下，顺序分别为0 1 2 3, 4 5 6 7
    private int[] cornerLoc = new int[8];

    // 8个角块的方向
    // 8个角块，每个角块有3个面，其中一定有一个面在顶面或底面上，角块初始在顶面或底面的面编号0，顺时针编为1 2
    // Orient中记录移动过程中在顶面或底面的编号
    private int[] cornerOrient = new int[8];

    // 12个棱块位置信息
    private int[] edgeLoc = new int[12];

    private int[] edgeOrient = new int[12];

    private int[] sideLoc = new int[6];

    private int[] sideOrient = new int[6];

    EventListenerList listenerList = new EventListenerList();

    private boolean quiet;

    //    private static final int[] SIDE_TRANSLATION = {0, 1, 2, 3, 4, 5};

    private static final int[][] EDGE_TRANSLATION = {{0, 1, 5, 7}, {4, 5, 0, 3}, {0, 7, 2, 1}, {5, 5, 1, 1}, {1, 3, 0, 5}, {2, 5, 1, 7}, {3, 1, 5, 1},
            {1, 5, 3, 3}, {3, 7, 2, 7}, {5, 3, 4, 1}, {4, 3, 3, 5}, {2, 3, 4, 7}};

    private static final int[][] CORNER_TRANSLATION = {{5, 6, 0, 0, 4, 2}, {2, 0, 4, 8, 0, 6}, {5, 8, 1, 0, 0, 2}, {2, 2, 0, 8, 1, 6}, {5, 2, 3, 0, 1, 2},
            {2, 8, 1, 8, 3, 6}, {5, 0, 4, 0, 3, 2}, {2, 6, 3, 8, 4, 6}};

    private static final int[][] EDGE_SIDE_MAP = {{4, 1}, {5, 2}, {1, 4}, {3, 0}, {2, 5}, {0, 3}, {1, 4}, {5, 2}, {4, 1}, {0, 3}, {2, 5}, {3, 0}};

    public RubiksCubeCore() {
        reset();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RubiksCubeCore)) {
            return false;
        }
        RubiksCubeCore rubiksCubeCore = (RubiksCubeCore) obj;
        return Arrays.equals(rubiksCubeCore.cornerLoc, this.cornerLoc) && Arrays.equals(rubiksCubeCore.cornerOrient, this.cornerOrient) // corner
               && Arrays.equals(rubiksCubeCore.edgeLoc, this.edgeLoc) && Arrays.equals(rubiksCubeCore.edgeOrient, this.edgeOrient) // edge
               && Arrays.equals(rubiksCubeCore.sideLoc, this.sideLoc) && Arrays.equals(rubiksCubeCore.sideOrient, this.sideOrient); // side
    }

    @Override
    public int hashCode() {
        int i = 0;
        for (int element : this.cornerLoc) {
            i <<= 1 + element;
        }
        for (int element : this.edgeLoc) {
            i <<= 1 + element;
        }
        return i;
    }

    public void reset() {
        for (int i = 0; i < 8; i++) {
            this.cornerLoc[i] = i;
            this.cornerOrient[i] = 0;
        }
        for (int i = 0; i < 12; i++) {
            this.edgeLoc[i] = i;
            this.edgeOrient[i] = 0;
        }
        for (int i = 0; i < 6; i++) {
            this.sideLoc[i] = i;
            this.sideOrient[i] = 0;
        }
        fireRubikChanged(new RubikEvent(this, 0, 0, 0));
    }

    public boolean isSolved() {
        for (int i = 0; i < 8; i++) {
            if (this.cornerLoc[i] != i || this.cornerOrient[i] != 0) {
                return false;
            }
        }
        for (int i2 = 0; i2 < 12; i2++) {
            if (this.edgeLoc[i2] != i2 || this.edgeOrient[i2] != 0) {
                return false;
            }
        }
        for (int i3 = 0; i3 < 6; i3++) {
            if (this.sideLoc[i3] != i3 || this.sideOrient[i3] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 旋转面
     *
     * @param face 面序号
     * @param clockwise true逆时针 false顺时针
     */
    public void twistSide(int face, boolean clockwise) {
        switch (face) {
        case ScriptParser.R: /* 0 */
            transform(2, 4, clockwise ? -1 : 1);
            break;
        case ScriptParser.U: /* 1 */
            transform(0, 4, clockwise ? -1 : 1);
            break;
        case ScriptParser.F: /* 2 */
            transform(1, 1, clockwise ? 1 : -1);
            break;
        case ScriptParser.L: /* 3 */
            transform(2, 1, clockwise ? 1 : -1);
            break;
        case ScriptParser.D: /* 4 */
            transform(0, 1, clockwise ? 1 : -1);
            break;
        case ScriptParser.B: /* 5 */
            transform(1, 4, clockwise ? -1 : 1);
            break;
        }
    }

    /**
     * 旋转棱
     *
     * @param face 面序号
     * @param clockwise true逆时针 false顺时针
     */
    public void twistEdge(int face, boolean clockwise) {
        switch (face) {
        case ScriptParser.R: /* 0 */
            transform(2, 2, clockwise ? -1 : 1);
            break;
        case ScriptParser.U: /* 1 */
            transform(0, 2, clockwise ? -1 : 1);
            break;
        case ScriptParser.F: /* 2 */
            transform(1, 2, clockwise ? 1 : -1);
            break;
        case ScriptParser.L: /* 3 */
            transform(2, 2, clockwise ? 1 : -1);
            break;
        case ScriptParser.D: /* 4 */
            transform(0, 2, clockwise ? 1 : -1);
            break;
        case ScriptParser.B: /* 5 */
            transform(1, 2, clockwise ? -1 : 1);
            break;
        }
    }

    /**
     * @param axis
     * @param layerMask
     * @param times 旋转次数，负数表示逆时针
     */
    public void transform(int axis, int layerMask, int times) {
        if (axis < 0 || axis > 2) {
            throw new IllegalArgumentException("axis: " + axis);
        }
        if (layerMask < 0 || layerMask > 7) {
            throw new IllegalArgumentException("layerMask: " + layerMask);
        }
        if (times < -2 || times > 2) {
            throw new IllegalArgumentException("angle: " + times);
        }
        if (times == 0) {
            return;
        }

        synchronized (this) {
            int i4 = times == -2 ? 2 : times;
            if ((layerMask & 0x1) != 0) {
                switch (axis) {
                case 0:
                    switch (i4) {
                    case -1:
                        twistLeftClockwise();
                        break;
                    case 1:
                        twistLeftCounterClockwise();
                        break;
                    case 2:
                        twistLeftDouble();
                        break;
                    }
                    break;
                case 1:
                    switch (i4) {
                    case -1:
                        twistBottomClockwise();
                        break;
                    case 1:
                        twistBottomCounterClockwise();
                        break;
                    case 2:
                        twistBottomDouble();
                        break;
                    }
                    break;
                case 2:
                    switch (i4) {
                    case -1:
                        twistBackClockwise();
                        break;
                    case 1:
                        twistBackCounterClockwise();
                        break;
                    case 2:
                        twistBackDouble();
                        break;
                    }
                    break;
                }
            }
            if ((layerMask & 0x2) != 0) {
                switch (axis) {
                case 0:
                    switch (i4) {
                    case -1:
                        twistMiddleLeftClockwise();
                        break;
                    case 1:
                        twistMiddleLeftCounterClockwise();
                        break;
                    case 2:
                        twistMiddleLeftDouble();
                        break;
                    }
                    break;
                case 1:
                    switch (i4) {
                    case -1:
                        twistMiddleBottomClockwise();
                        break;
                    case 1:
                        twistMiddleBottomCounterClockwise();
                        break;
                    case 2:
                        twistMiddleBottomDouble();
                        break;
                    }
                    break;
                case 2:
                    switch (i4) {
                    case -1:
                        twistMiddleBackClockwise();
                        break;
                    case 1:
                        twistMiddleBackCounterClockwise();
                        break;
                    case 2:
                        twistMiddleBackDouble();
                        break;
                    }
                    break;
                }
            }
            if ((layerMask & 0x4) != 0) {
                switch (axis) {
                case 0:
                    switch (i4) {
                    case -1:
                        twistRightCounterClockwise();
                        break;
                    case 1:
                        twistRightClockwise();
                        break;
                    case 2:
                        twistRightDouble();
                        break;
                    }
                    break;
                case 1:
                    switch (i4) {
                    case -1:
                        twistTopCounterClockwise();
                        break;
                    case 1:
                        twistTopClockwise();
                        break;
                    case 2:
                        twistTopDouble();
                        break;
                    }
                    break;
                case 2:
                    switch (i4) {
                    case -1:
                        twistFrontCounterClockwise();
                        break;
                    case 1:
                        twistFrontClockwise();
                        break;
                    case 2:
                        twistFrontDouble();
                        break;
                    }
                    break;
                }
            }
        }
        fireRubikTwisted(new RubikEvent(this, axis, layerMask, times));
    }

    public void transform(RubiksCubeCore rubiksCubeCore) {
        int[] cornerLoc = this.cornerLoc;
        int[] cornerOrient = this.cornerOrient;
        for (int i = 0; i < rubiksCubeCore.cornerLoc.length; i++) {
            this.cornerLoc[i] = cornerLoc[rubiksCubeCore.cornerLoc[i]];
            this.cornerOrient[i] = (cornerOrient[rubiksCubeCore.cornerLoc[i]] + rubiksCubeCore.cornerOrient[i]) % 3;
        }
        int[] edgeLoc = this.edgeLoc;
        int[] edgeOrient = this.edgeOrient;
        for (int i2 = 0; i2 < rubiksCubeCore.edgeLoc.length; i2++) {
            this.edgeLoc[i2] = edgeLoc[rubiksCubeCore.edgeLoc[i2]];
            this.edgeOrient[i2] = (edgeOrient[rubiksCubeCore.edgeLoc[i2]] + rubiksCubeCore.edgeOrient[i2]) % 2;
        }
        int[] sideLoc = this.sideLoc;
        int[] sideOrient = this.sideOrient;
        for (int i3 = 0; i3 < rubiksCubeCore.sideLoc.length; i3++) {
            this.sideLoc[i3] = sideLoc[rubiksCubeCore.sideLoc[i3]];
            this.sideOrient[i3] = (sideOrient[rubiksCubeCore.sideLoc[i3]] + rubiksCubeCore.sideOrient[i3]) % 4;
        }
        fireRubikChanged(new RubikEvent(this, 0, 0, 0));
    }

    public void setTo(RubiksCubeCore rubiksCubeCore) {
        System.arraycopy(rubiksCubeCore.sideLoc, 0, this.sideLoc, 0, this.sideLoc.length);
        System.arraycopy(rubiksCubeCore.sideOrient, 0, this.sideOrient, 0, this.sideOrient.length);
        System.arraycopy(rubiksCubeCore.edgeLoc, 0, this.edgeLoc, 0, this.edgeLoc.length);
        System.arraycopy(rubiksCubeCore.edgeOrient, 0, this.edgeOrient, 0, this.edgeOrient.length);
        System.arraycopy(rubiksCubeCore.cornerLoc, 0, this.cornerLoc, 0, this.cornerLoc.length);
        System.arraycopy(rubiksCubeCore.cornerOrient, 0, this.cornerOrient, 0, this.cornerOrient.length);
        fireRubikChanged(new RubikEvent(this, 0, 0, 0));
    }

    private void twistFrontDouble() {
        twistFrontClockwise();
        twistFrontClockwise();
    }

    private void twistFrontCounterClockwise() {
        twistFrontClockwise();
        twistFrontClockwise();
        twistFrontClockwise();
    }

    private void twistFrontClockwise() {
        int i = this.cornerLoc[0];
        this.cornerLoc[0] = this.cornerLoc[1];
        this.cornerLoc[1] = this.cornerLoc[3];
        this.cornerLoc[3] = this.cornerLoc[2];
        this.cornerLoc[2] = i;
        int i2 = this.cornerOrient[0];
        this.cornerOrient[0] = (this.cornerOrient[1] + 1) % 3;
        this.cornerOrient[1] = (this.cornerOrient[3] + 2) % 3;
        this.cornerOrient[3] = (this.cornerOrient[2] + 1) % 3;
        this.cornerOrient[2] = (i2 + 2) % 3;
        int i3 = this.edgeLoc[0];
        this.edgeLoc[0] = this.edgeLoc[1];
        this.edgeLoc[1] = this.edgeLoc[2];
        this.edgeLoc[2] = this.edgeLoc[4];
        this.edgeLoc[4] = i3;
        int i4 = this.edgeOrient[0];
        this.edgeOrient[0] = (this.edgeOrient[1] + 1) % 2;
        this.edgeOrient[1] = (this.edgeOrient[2] + 1) % 2;
        this.edgeOrient[2] = (this.edgeOrient[4] + 1) % 2;
        this.edgeOrient[4] = (i4 + 1) % 2;
        this.sideOrient[0] = (this.sideOrient[0] + 3) % 4;
    }

    private void twistRightDouble() {
        twistRightClockwise();
        twistRightClockwise();
    }

    private void twistRightCounterClockwise() {
        twistRightClockwise();
        twistRightClockwise();
        twistRightClockwise();
    }

    private void twistRightClockwise() {
        int i = this.cornerLoc[2];
        this.cornerLoc[2] = this.cornerLoc[3];
        this.cornerLoc[3] = this.cornerLoc[5];
        this.cornerLoc[5] = this.cornerLoc[4];
        this.cornerLoc[4] = i;
        int i2 = this.cornerOrient[2];
        this.cornerOrient[2] = (this.cornerOrient[3] + 1) % 3;
        this.cornerOrient[3] = (this.cornerOrient[5] + 2) % 3;
        this.cornerOrient[5] = (this.cornerOrient[4] + 1) % 3;
        this.cornerOrient[4] = (i2 + 2) % 3;
        int i3 = this.edgeLoc[3];
        this.edgeLoc[3] = this.edgeLoc[4];
        this.edgeLoc[4] = this.edgeLoc[5];
        this.edgeLoc[5] = this.edgeLoc[7];
        this.edgeLoc[7] = i3;
        int i4 = this.edgeOrient[3];
        this.edgeOrient[3] = (this.edgeOrient[4] + 1) % 2;
        this.edgeOrient[4] = (this.edgeOrient[5] + 1) % 2;
        this.edgeOrient[5] = (this.edgeOrient[7] + 1) % 2;
        this.edgeOrient[7] = (i4 + 1) % 2;
        this.sideOrient[1] = (this.sideOrient[1] + 3) % 4;
    }

    private void twistLeftDouble() {
        twistLeftClockwise();
        twistLeftClockwise();
    }

    private void twistLeftCounterClockwise() {
        twistLeftClockwise();
        twistLeftClockwise();
        twistLeftClockwise();
    }

    private void twistLeftClockwise() {
        int i = this.cornerLoc[0];
        this.cornerLoc[0] = this.cornerLoc[6];
        this.cornerLoc[6] = this.cornerLoc[7];
        this.cornerLoc[7] = this.cornerLoc[1];
        this.cornerLoc[1] = i;
        int i2 = this.cornerOrient[0];
        this.cornerOrient[0] = (this.cornerOrient[6] + 2) % 3;
        this.cornerOrient[6] = (this.cornerOrient[7] + 1) % 3;
        this.cornerOrient[7] = (this.cornerOrient[1] + 2) % 3;
        this.cornerOrient[1] = (i2 + 1) % 3;
        int i3 = this.edgeLoc[1];
        this.edgeLoc[1] = this.edgeLoc[9];
        this.edgeLoc[9] = this.edgeLoc[10];
        this.edgeLoc[10] = this.edgeLoc[11];
        this.edgeLoc[11] = i3;
        int i4 = this.edgeOrient[1];
        this.edgeOrient[1] = (this.edgeOrient[9] + 1) % 2;
        this.edgeOrient[9] = (this.edgeOrient[10] + 1) % 2;
        this.edgeOrient[10] = (this.edgeOrient[11] + 1) % 2;
        this.edgeOrient[11] = (i4 + 1) % 2;
        this.sideOrient[4] = (this.sideOrient[4] + 3) % 4;
    }

    private void twistTopDouble() {
        twistTopClockwise();
        twistTopClockwise();
    }

    private void twistTopCounterClockwise() {
        twistTopClockwise();
        twistTopClockwise();
        twistTopClockwise();
    }

    private void twistTopClockwise() {
        int i = this.cornerLoc[0];
        this.cornerLoc[0] = this.cornerLoc[2];
        this.cornerLoc[2] = this.cornerLoc[4];
        this.cornerLoc[4] = this.cornerLoc[6];
        this.cornerLoc[6] = i;
        int i2 = this.cornerOrient[0];
        this.cornerOrient[0] = this.cornerOrient[2];
        this.cornerOrient[2] = this.cornerOrient[4];
        this.cornerOrient[4] = this.cornerOrient[6];
        this.cornerOrient[6] = i2;
        int i3 = this.edgeLoc[0];
        this.edgeLoc[0] = this.edgeLoc[3];
        this.edgeLoc[3] = this.edgeLoc[6];
        this.edgeLoc[6] = this.edgeLoc[9];
        this.edgeLoc[9] = i3;
        int i4 = this.edgeOrient[0];
        this.edgeOrient[0] = (this.edgeOrient[3] + 1) % 2;
        this.edgeOrient[3] = (this.edgeOrient[6] + 1) % 2;
        this.edgeOrient[6] = (this.edgeOrient[9] + 1) % 2;
        this.edgeOrient[9] = (i4 + 1) % 2;
        this.sideOrient[5] = (this.sideOrient[5] + 3) % 4;
    }

    private void twistBottomDouble() {
        twistBottomClockwise();
        twistBottomClockwise();
    }

    private void twistBottomCounterClockwise() {
        twistBottomClockwise();
        twistBottomClockwise();
        twistBottomClockwise();
    }

    private void twistBottomClockwise() {
        int i = this.cornerLoc[1];
        this.cornerLoc[1] = this.cornerLoc[7];
        this.cornerLoc[7] = this.cornerLoc[5];
        this.cornerLoc[5] = this.cornerLoc[3];
        this.cornerLoc[3] = i;
        int i2 = this.cornerOrient[1];
        this.cornerOrient[1] = this.cornerOrient[7];
        this.cornerOrient[7] = this.cornerOrient[5];
        this.cornerOrient[5] = this.cornerOrient[3];
        this.cornerOrient[3] = i2;
        int i3 = this.edgeLoc[2];
        this.edgeLoc[2] = this.edgeLoc[11];
        this.edgeLoc[11] = this.edgeLoc[8];
        this.edgeLoc[8] = this.edgeLoc[5];
        this.edgeLoc[5] = i3;
        int i4 = this.edgeOrient[2];
        this.edgeOrient[2] = (this.edgeOrient[11] + 1) % 2;
        this.edgeOrient[11] = (this.edgeOrient[8] + 1) % 2;
        this.edgeOrient[8] = (this.edgeOrient[5] + 1) % 2;
        this.edgeOrient[5] = (i4 + 1) % 2;
        this.sideOrient[2] = (this.sideOrient[2] + 3) % 4;
    }

    private void twistMiddleBackDouble() {
        twistMiddleFrontDouble();
    }

    private void twistMiddleBackCounterClockwise() {
        twistMiddleFrontClockwise();
    }

    private void twistMiddleBackClockwise() {
        twistMiddleFrontCounterClockwise();
    }

    private void twistMiddleFrontDouble() {
        twistMiddleFrontClockwise();
        twistMiddleFrontClockwise();
    }

    private void twistMiddleFrontCounterClockwise() {
        twistMiddleFrontClockwise();
        twistMiddleFrontClockwise();
        twistMiddleFrontClockwise();
    }

    private void twistMiddleFrontClockwise() {
        int i = this.edgeLoc[3];
        this.edgeLoc[3] = this.edgeLoc[9];
        this.edgeLoc[9] = this.edgeLoc[11];
        this.edgeLoc[11] = this.edgeLoc[5];
        this.edgeLoc[5] = i;
        int i2 = this.edgeOrient[3];
        this.edgeOrient[3] = (this.edgeOrient[9] + 1) % 2;
        this.edgeOrient[9] = (this.edgeOrient[11] + 1) % 2;
        this.edgeOrient[11] = (this.edgeOrient[5] + 1) % 2;
        this.edgeOrient[5] = (i2 + 1) % 2;
        int i3 = this.sideLoc[1];
        this.sideLoc[1] = this.sideLoc[5];
        this.sideLoc[5] = this.sideLoc[4];
        this.sideLoc[4] = this.sideLoc[2];
        this.sideLoc[2] = i3;
        int i4 = this.sideOrient[1];
        this.sideOrient[1] = (this.sideOrient[5] + 1) % 4;
        this.sideOrient[5] = (this.sideOrient[4] + 3) % 4;
        this.sideOrient[4] = (this.sideOrient[2] + 1) % 4;
        this.sideOrient[2] = (i4 + 3) % 4;
    }

    private void twistMiddleBottomDouble() {
        twistMiddleTopDouble();
    }

    private void twistMiddleBottomCounterClockwise() {
        twistMiddleTopClockwise();
    }

    private void twistMiddleBottomClockwise() {
        twistMiddleTopCounterClockwise();
    }

    private void twistMiddleTopDouble() {
        twistMiddleTopClockwise();
        twistMiddleTopClockwise();
    }

    private void twistMiddleTopCounterClockwise() {
        twistMiddleTopClockwise();
        twistMiddleTopClockwise();
        twistMiddleTopClockwise();
    }

    private void twistMiddleTopClockwise() {
        int i = this.edgeLoc[1];
        this.edgeLoc[1] = this.edgeLoc[4];
        this.edgeLoc[4] = this.edgeLoc[7];
        this.edgeLoc[7] = this.edgeLoc[10];
        this.edgeLoc[10] = i;
        int i2 = this.edgeOrient[1];
        this.edgeOrient[1] = (this.edgeOrient[4] + 1) % 2;
        this.edgeOrient[4] = (this.edgeOrient[7] + 1) % 2;
        this.edgeOrient[7] = (this.edgeOrient[10] + 1) % 2;
        this.edgeOrient[10] = (i2 + 1) % 2;
        int i3 = this.sideLoc[0];
        this.sideLoc[0] = this.sideLoc[1];
        this.sideLoc[1] = this.sideLoc[3];
        this.sideLoc[3] = this.sideLoc[4];
        this.sideLoc[4] = i3;
        int i4 = this.sideOrient[0];
        this.sideOrient[0] = (this.sideOrient[1] + 1) % 4;
        this.sideOrient[1] = (this.sideOrient[3] + 1) % 4;
        this.sideOrient[3] = (this.sideOrient[4] + 1) % 4;
        this.sideOrient[4] = (i4 + 1) % 4;
    }

    private void twistMiddleLeftDouble() {
        twistMiddleRightDouble();
    }

    private void twistMiddleLeftCounterClockwise() {
        twistMiddleRightClockwise();
    }

    private void twistMiddleLeftClockwise() {
        twistMiddleRightCounterClockwise();
    }

    private void twistMiddleRightDouble() {
        twistMiddleRightClockwise();
        twistMiddleRightClockwise();
    }

    private void twistMiddleRightCounterClockwise() {
        twistMiddleRightClockwise();
        twistMiddleRightClockwise();
        twistMiddleRightClockwise();
    }

    private void twistMiddleRightClockwise() {
        int i = this.edgeLoc[0];
        this.edgeLoc[0] = this.edgeLoc[2];
        this.edgeLoc[2] = this.edgeLoc[8];
        this.edgeLoc[8] = this.edgeLoc[6];
        this.edgeLoc[6] = i;
        int i2 = this.edgeOrient[0];
        this.edgeOrient[0] = (this.edgeOrient[2] + 1) % 2;
        this.edgeOrient[2] = (this.edgeOrient[8] + 1) % 2;
        this.edgeOrient[8] = (this.edgeOrient[6] + 1) % 2;
        this.edgeOrient[6] = (i2 + 1) % 2;
        int i3 = this.sideLoc[0];
        this.sideLoc[0] = this.sideLoc[2];
        this.sideLoc[2] = this.sideLoc[3];
        this.sideLoc[3] = this.sideLoc[5];
        this.sideLoc[5] = i3;
        int i4 = this.sideOrient[0];
        this.sideOrient[0] = (this.sideOrient[2] + 1) % 4;
        this.sideOrient[2] = (this.sideOrient[3] + 1) % 4;
        this.sideOrient[3] = (this.sideOrient[5] + 1) % 4;
        this.sideOrient[5] = (i4 + 1) % 4;
    }

    private void twistBackDouble() {
        twistBackClockwise();
        twistBackClockwise();
    }

    private void twistBackCounterClockwise() {
        int i = this.cornerLoc[6];
        this.cornerLoc[6] = this.cornerLoc[7];
        this.cornerLoc[7] = this.cornerLoc[5];
        this.cornerLoc[5] = this.cornerLoc[4];
        this.cornerLoc[4] = i;
        int i2 = this.cornerOrient[6];
        this.cornerOrient[6] = (this.cornerOrient[7] + 2) % 3;
        this.cornerOrient[7] = (this.cornerOrient[5] + 1) % 3;
        this.cornerOrient[5] = (this.cornerOrient[4] + 2) % 3;
        this.cornerOrient[4] = (i2 + 1) % 3;
        int i3 = this.edgeLoc[10];
        this.edgeLoc[10] = this.edgeLoc[8];
        this.edgeLoc[8] = this.edgeLoc[7];
        this.edgeLoc[7] = this.edgeLoc[6];
        this.edgeLoc[6] = i3;
        int i4 = this.edgeOrient[10];
        this.edgeOrient[10] = (this.edgeOrient[8] + 1) % 2;
        this.edgeOrient[8] = (this.edgeOrient[7] + 1) % 2;
        this.edgeOrient[7] = (this.edgeOrient[6] + 1) % 2;
        this.edgeOrient[6] = (i4 + 1) % 2;
        this.sideOrient[3] = (this.sideOrient[3] + 1) % 4;
    }

    private void twistBackClockwise() {
        int i = this.cornerLoc[4];
        this.cornerLoc[4] = this.cornerLoc[5];
        this.cornerLoc[5] = this.cornerLoc[7];
        this.cornerLoc[7] = this.cornerLoc[6];
        this.cornerLoc[6] = i;
        int i2 = this.cornerOrient[4];
        this.cornerOrient[4] = (this.cornerOrient[5] + 1) % 3;
        this.cornerOrient[5] = (this.cornerOrient[7] + 2) % 3;
        this.cornerOrient[7] = (this.cornerOrient[6] + 1) % 3;
        this.cornerOrient[6] = (i2 + 2) % 3;
        int i3 = this.edgeLoc[6];
        this.edgeLoc[6] = this.edgeLoc[7];
        this.edgeLoc[7] = this.edgeLoc[8];
        this.edgeLoc[8] = this.edgeLoc[10];
        this.edgeLoc[10] = i3;
        int i4 = this.edgeOrient[6];
        this.edgeOrient[6] = (this.edgeOrient[7] + 1) % 2;
        this.edgeOrient[7] = (this.edgeOrient[8] + 1) % 2;
        this.edgeOrient[8] = (this.edgeOrient[10] + 1) % 2;
        this.edgeOrient[10] = (i4 + 1) % 2;
        this.sideOrient[3] = (this.sideOrient[3] + 3) % 4;
    }

    private void rotateFrontClockwise() {
        twistFrontClockwise();
        twistMiddleFrontClockwise();
        twistBackCounterClockwise();
    }

    private void rotateFrontCounterClockwise() {
        twistFrontCounterClockwise();
        twistMiddleFrontCounterClockwise();
        twistBackClockwise();
    }

    private void rotateFrontDouble() {
        twistFrontDouble();
        twistMiddleFrontDouble();
        twistBackDouble();
    }

    @SuppressWarnings("unused")
    private void rotateBackClockwise() {
        rotateFrontCounterClockwise();
    }

    @SuppressWarnings("unused")
    private void rotateBackCounterClockwise() {
        rotateFrontClockwise();
    }

    @SuppressWarnings("unused")
    private void rotateBackDouble() {
        rotateFrontDouble();
    }

    private void rotateTopClockwise() {
        twistTopClockwise();
        twistMiddleTopClockwise();
        twistBottomCounterClockwise();
    }

    private void rotateTopCounterClockwise() {
        twistTopCounterClockwise();
        twistMiddleTopCounterClockwise();
        twistBottomClockwise();
    }

    private void rotateTopDouble() {
        twistTopDouble();
        twistMiddleTopDouble();
        twistBottomDouble();
    }

    @SuppressWarnings("unused")
    private void rotateBottomClockwise() {
        rotateTopCounterClockwise();
    }

    @SuppressWarnings("unused")
    private void rotateBottomCounterClockwise() {
        rotateTopClockwise();
    }

    @SuppressWarnings("unused")
    private void rotateBottomDouble() {
        rotateTopDouble();
    }

    private void rotateRightClockwise() {
        twistRightClockwise();
        twistMiddleRightClockwise();
        twistLeftCounterClockwise();
    }

    private void rotateRightCounterClockwise() {
        twistRightCounterClockwise();
        twistMiddleRightCounterClockwise();
        twistLeftClockwise();
    }

    private void rotateRightDouble() {
        twistRightDouble();
        twistMiddleRightDouble();
        twistLeftDouble();
    }

    @SuppressWarnings("unused")
    private void rotateLeftClockwise() {
        rotateRightCounterClockwise();
    }

    @SuppressWarnings("unused")
    private void rotateLeftCounterClockwise() {
        rotateRightClockwise();
    }

    @SuppressWarnings("unused")
    private void rotateLeftDouble() {
        rotateRightDouble();
    }

    public int[] getCornerLocations() {
        return this.cornerLoc;
    }

    public int[] getCornerOrientations() {
        return this.cornerOrient;
    }

    public void setCorners(int[] cornerLoc, int[] cornerOrient) {
        this.cornerLoc = cornerLoc;
        this.cornerOrient = cornerOrient;
        fireRubikChanged(new RubikEvent(this, 0, 0, 0));
    }

    public int getCornerAt(int index) {
        return this.cornerLoc[index];
    }

    public int getCornerLocation(int index) {
        if (this.cornerLoc[index] == index) {
            return index;
        }
        int length = this.cornerLoc.length - 1;
        while (length >= 0 && this.cornerLoc[length] != index) {
            length--;
        }
        return length;
    }

    public int getCornerOrientation(int index) {
        return this.cornerOrient[getCornerLocation(index)];
    }

    public int[] getEdgeLocations() {
        return this.edgeLoc;
    }

    public int[] getEdgeOrientations() {
        return this.edgeOrient;
    }

    public void setEdges(int[] edgeLoc, int[] edgeOrient) {
        this.edgeLoc = edgeLoc;
        this.edgeOrient = edgeOrient;
        fireRubikChanged(new RubikEvent(this, 0, 0, 0));
    }

    public int getEdgeAt(int index) {
        return this.edgeLoc[index];
    }

    public int getEdgeLocation(int index) {
        if (this.edgeLoc[index] == index) {
            return index;
        }
        int length = this.edgeLoc.length - 1;
        while (length >= 0 && this.edgeLoc[length] != index) {
            length--;
        }
        return length;
    }

    public int getEdgeOrientation(int index) {
        return this.edgeOrient[getEdgeLocation(index)];
    }

    public int[] getSideLocations() {
        return this.sideLoc;
    }

    public int[] getSideOrientations() {
        return this.sideOrient;
    }

    public void setSides(int[] sideLoc, int[] sideOrient) {
        this.sideLoc = sideLoc;
        this.sideOrient = sideOrient;
        fireRubikChanged(new RubikEvent(this, 0, 0, 0));
    }

    public int getSideAt(int index) {
        return this.sideLoc[index];
    }

    public int getSideLocation(int index) {
        if (this.sideLoc[index] == index) {
            return index;
        }
        int length = this.sideLoc.length - 1;
        while (length >= 0 && this.sideLoc[length] != index) {
            length--;
        }
        return length;
    }

    public int getSideOrientation(int index) {
        return this.sideOrient[getSideLocation(index)];
    }

    public int getCubeOrientation() {
        switch ((this.sideLoc[0] * 6) + this.sideLoc[1]) {
        case ScriptParser.U: /* 1 */
            return ScriptParser.R; /* 0 */
        case ScriptParser.F: /* 2 */
            return ScriptParser.Li; /* 9 */
        case ScriptParser.D: /* 4 */
            return ScriptParser.Fi; /* 8 */
        case ScriptParser.B: /* 5 */
            return ScriptParser.Ui; /* 7 */
        case ScriptParser.Ri: /* 6 */
            return ScriptParser.U2; /* 13 */
        case ScriptParser.Fi: /* 8 */
            return ScriptParser.L2; /* 15 */
        case ScriptParser.Li: /* 9 */
            return ScriptParser.D; /* 4 */
        case ScriptParser.Bi: /* 11 */
            return ScriptParser.Di; /* 10 */
        case ScriptParser.R2: /* 12 */
            return ScriptParser.R2i; /* 18 */
        case ScriptParser.U2: /* 13 */
            return ScriptParser.U; /* 1 */
        case ScriptParser.L2: /* 15 */
            return ScriptParser.U2i; /* 19 */
        case ScriptParser.D2: /* 16 */
            return ScriptParser.D2; /* 16 */
        case ScriptParser.U2i: /* 19 */
            return ScriptParser.F; /* 2 */
        case ScriptParser.F2i: /* 20 */
            return ScriptParser.F2i; /* 20 */
        case ScriptParser.D2i: /* 22 */
            return ScriptParser.B; /* 5 */
        case ScriptParser.B2i: /* 23 */
            return ScriptParser.L2i; /* 21 */
        case ScriptParser.TR: /* 24 */
            return ScriptParser.Ri; /* 6 */
        case ScriptParser.TF: /* 26 */
            return ScriptParser.R2; /* 12 */
        case ScriptParser.TL: /* 27 */
            return ScriptParser.F2; /* 14 */
        case ScriptParser.TB: /* 29 */
            return ScriptParser.B2; /* 17 */
        case ScriptParser.TRi: /* 30 */
            return ScriptParser.B2i; /* 23 */
        case ScriptParser.TUi: /* 31 */
            return ScriptParser.L; /* 3 */
        case ScriptParser.TLi: /* 33 */
            return ScriptParser.D2i; /* 22 */
        case ScriptParser.TDi: /* 34 */
            return ScriptParser.Bi; /* 11 */
        case ScriptParser.L: /* 3 */
        case ScriptParser.Ui: /* 7 */
        case ScriptParser.Di: /* 10 */
        case ScriptParser.F2: /* 14 */
        case ScriptParser.B2: /* 17 */
        case ScriptParser.R2i: /* 18 */
        case ScriptParser.L2i: /* 21 */
        case ScriptParser.TU: /* 25 */
        case ScriptParser.TD: /* 28 */
        case ScriptParser.TFi: /* 32 */
        default:
            return ScriptParser.POSITION_UNSUPPORTED;
        }
    }

    public int getPartSide(int index, int orient) {
        return index < 8 ? getCornerSide(index, orient) : index < 20 ? getEdgeSide(index - 8, orient) : index < 26 ? getSideLocation(index - 20) : orient;
    }

    public int getCornerSide(int index, int orient) {
        int cornerLocation = getCornerLocation(index);
        return CORNER_TRANSLATION[cornerLocation][((6 + (orient * 2)) - (this.cornerOrient[cornerLocation] * 2)) % 6];
    }

    public int getEdgeSide(int index, int orient) {
        int edgeLocation = getEdgeLocation(index);
        switch (orient) {
        case 0:
            return EDGE_TRANSLATION[edgeLocation][(4 - (this.edgeOrient[edgeLocation] * 2)) % 4];
        case 1:
            return EDGE_TRANSLATION[edgeLocation][(6 - (this.edgeOrient[edgeLocation] * 2)) % 4];
        default:
            throw new IllegalArgumentException("invalid orientation:" + orient);
        }
    }

    public int getEdgeLayerSide(int index, int orient) {
        int edgeLocation = getEdgeLocation(index);
        return EDGE_SIDE_MAP[edgeLocation][(orient + this.edgeOrient[edgeLocation]) % 2];
    }

    public void addRubikListener(RubikListener rubikListener) {
        this.listenerList.add(RubikListener.class, rubikListener);
    }

    public void removeRubikListener(RubikListener rubikListener) {
        this.listenerList.remove(RubikListener.class, rubikListener);
    }

    protected void fireRubikTwisted(RubikEvent rubikEvent) {
        if (this.quiet) {
            return;
        }
        List<ListenerNode> listenerList = this.listenerList.getListenerList();
        for (ListenerNode node : listenerList) {
            if (node.getClazz() == RubikListener.class) {
                ((RubikListener) node.getListener()).rubikTwisting(rubikEvent);
                ((RubikListener) node.getListener()).rubikTwisted(rubikEvent);
            }
        }
    }

    protected void fireRubikChanged(RubikEvent rubikEvent) {
        if (this.quiet) {
            return;
        }
        List<ListenerNode> listenerList = this.listenerList.getListenerList();
        for (ListenerNode node : listenerList) {
            if (node.getClazz() == RubikListener.class) {
                ((RubikListener) node.getListener()).rubikChanged(rubikEvent);
            }
        }
    }

    public void setQuiet(boolean quiet) {
        if (quiet != this.quiet) {
            this.quiet = quiet;
            if (this.quiet) {
                return;
            }
            fireRubikChanged(new RubikEvent(this, 0, 0, 0));
        }
    }

    @Override
    public Object clone() {
        try {
            RubiksCubeCore rubiksCubeCore = (RubiksCubeCore) super.clone();
            rubiksCubeCore.cornerLoc = this.cornerLoc.clone();
            rubiksCubeCore.cornerOrient = this.cornerOrient.clone();
            rubiksCubeCore.edgeLoc = this.edgeLoc.clone();
            rubiksCubeCore.edgeOrient = this.edgeOrient.clone();
            rubiksCubeCore.sideLoc = this.sideLoc.clone();
            rubiksCubeCore.sideOrient = this.sideOrient.clone();
            rubiksCubeCore.listenerList = new EventListenerList();
            return rubiksCubeCore;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

}
