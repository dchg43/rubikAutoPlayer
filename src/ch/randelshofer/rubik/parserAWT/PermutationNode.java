package ch.randelshofer.rubik.parserAWT;


import java.util.Enumeration;
import java.util.Vector;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;
import ch.randelshofer.rubik.RubiksCubeCore;
import ch.randelshofer.util.SingletonEnumeration;


public class PermutationNode extends ScriptNode
{
    private static final long serialVersionUID = -1979158632899871940L;

    private Vector<PermutationItem> sequence = new Vector<>();

    private int sign = -1;

    private int type = -1;

    public static final int SIDE_PERMUTATION = 1;

    public static final int EDGE_PERMUTATION = 2;

    public static final int CORNER_PERMUTATION = 3;

    public static final int UNDEFINED = -1;

    // private static final int[][] SIDE_SYMBOLS = {{87}, {85}, {89}, {90}, {88}, {86}};

    // private static final int[][] EDGE_SYMBOLS = {{87, 86}, {88, 87}, {87, 89}, {86, 85}, {85, 87}, {89, 85}, {90,
    // 86},
    // {85, 90}, {90, 89}, {86, 88}, {88, 90}, {89, 88}};

    // private static final int[][] CORNER_SYMBOLS = {{86, 87, 88}, {89, 88, 87}, {86, 85, 87}, {89, 87, 85}, {86, 90,
    // 85},
    // {89, 85, 90}, {86, 88, 90}, {89, 90, 88}};

    private static class PermutationItem implements Cloneable
    {
        public int orientation;

        public int location;

        private PermutationItem()
        {}

        @Override
        public Object clone()
        {
            try
            {
                return super.clone();
            }
            catch (CloneNotSupportedException e)
            {
                throw new InternalError(e.getMessage());
            }
        }
    }

    public PermutationNode()
    {
        setAllowsChildren(false);
    }

    public PermutationNode(int i, int i2)
    {
        super(i, i2);
        setAllowsChildren(false);
    }

    @Override
    public int getSymbol()
    {
        return ScriptParser.PERMUTATION_EXPRESSION;
    }

    @Override
    public int getFullTurnCount()
    {
        return 0;
    }

    @Override
    public int getQuarterTurnCount()
    {
        return 0;
    }

    public void addPermItem(int i, int i2, int[] iArr)
    {
        int i3;
        int i4;
        int i5;
        int i6;
        boolean z;
        if (this.type == -1)
        {
            this.type = i;
        }
        if (this.type != i)
        {
            throw new IllegalArgumentException("Permutation of different part types is not supported.");
        }
        switch (i2)
        {
            case ScriptParser.PPLUS: /* 91 */
                i3 = 3;
                break;
            case ScriptParser.PMINUS: /* 92 */
                i3 = 1;
                break;
            case ScriptParser.PPLUSPLUS: /* 93 */
                i3 = 2;
                break;
            default:
                i3 = 0;
                break;
        }
        if (i3 == 3)
        {
            if (i == 3)
            {
                i3 = 2;
            }
            else if (i == 2)
            {
                i3 = 1;
            }
        }
        if (this.sequence.size() == 0)
        {
            this.sign = i3;
        }
        else if (i != 1 && i3 != 0)
        {
            throw new IllegalArgumentException("Illegal sign.");
        }
        PermutationItem permutationItem = new PermutationItem();
        int i7 = -1;
        switch (i)
        {
            case 1:
                switch (iArr[0])
                {
                    case ScriptParser.PR: /* 85 */
                        i7 = 1;
                        break;
                    case ScriptParser.PU: /* 86 */
                        i7 = 5;
                        break;
                    case ScriptParser.PF: /* 87 */
                        i7 = 0;
                        break;
                    case ScriptParser.PL: /* 88 */
                        i7 = 4;
                        break;
                    case ScriptParser.PD: /* 89 */
                        i7 = 2;
                        break;
                    case ScriptParser.PB: /* 90 */
                        i7 = 3;
                        break;
                }
                permutationItem.location = i7;
                permutationItem.orientation = this.sequence.size() == 0 ? 0 : i3;
                break;
            case 2:
                if (i2 != 0 && i2 != 91)
                {
                    throw new IllegalArgumentException("Illegal sign for edge part.");
                }
                int iMin = Math.min(iArr[0], iArr[1]);
                int iMax = Math.max(iArr[0], iArr[1]);
                int i8 = iArr[0];
                if (iMin == 86 && iMax == 87)
                {
                    i6 = 0;
                    z = i8 == 86;
                }
                else if (iMin == 87 && iMax == 88)
                {
                    i6 = 1;
                    z = i8 == 87;
                }
                else if (iMin == 87 && iMax == 89)
                {
                    i6 = 2;
                    z = i8 == 89;
                }
                else if (iMin == 85 && iMax == 86)
                {
                    i6 = 3;
                    z = i8 == 85;
                }
                else if (iMin == 85 && iMax == 87)
                {
                    i6 = 4;
                    z = i8 == 87;
                }
                else if (iMin == 85 && iMax == 89)
                {
                    i6 = 5;
                    z = i8 == 85;
                }
                else if (iMin == 86 && iMax == 90)
                {
                    i6 = 6;
                    z = i8 == 86;
                }
                else if (iMin == 85 && iMax == 90)
                {
                    i6 = 7;
                    z = i8 == 90;
                }
                else if (iMin == 89 && iMax == 90)
                {
                    i6 = 8;
                    z = i8 == 89;
                }
                else if (iMin == 86 && iMax == 88)
                {
                    i6 = 9;
                    z = i8 == 88;
                }
                else if (iMin == 88 && iMax == 90)
                {
                    i6 = 10;
                    z = i8 == 90;
                }
                else if (iMin == 88 && iMax == 89)
                {
                    i6 = 11;
                    z = i8 == 88;
                }
                else
                {
                    throw new IllegalArgumentException("Impossible edge part.");
                }
                permutationItem.location = i6;
                permutationItem.orientation = z ? 1 : 0;
                break;
            case 3:
                if (i2 == 93)
                {
                    throw new IllegalArgumentException("Illegal sign for corner part.");
                }
                int i9 = iArr[0];
                int i10 = iArr[1];
                int i11 = iArr[2];
                if (i9 > i10)
                {
                    i10 = i9;
                    i9 = i10;
                }
                if (i9 > i11)
                {
                    i11 = i9;
                    i9 = i11;
                }
                if (i10 > i11)
                {
                    int i12 = i11;
                    i11 = i10;
                    i10 = i12;
                }
                if (i9 == 86 && i10 == 87 && i11 == 88)
                {
                    i4 = 0;
                    if (iArr[0] == 86)
                    {
                        i5 = iArr[1] == 87 ? 0 : 3;
                    }
                    else if (iArr[0] == 87)
                    {
                        i5 = iArr[1] == 88 ? 2 : 5;
                    }
                    else
                    {
                        i5 = iArr[1] == 86 ? 1 : 4;
                    }
                }
                else if (i9 == 87 && i10 == 88 && i11 == 89)
                {
                    i4 = 1;
                    if (iArr[0] == 89)
                    {
                        i5 = iArr[1] == 88 ? 0 : 3;
                    }
                    else if (iArr[0] == 88)
                    {
                        i5 = iArr[1] == 87 ? 2 : 5;
                    }
                    else
                    {
                        i5 = iArr[1] == 89 ? 1 : 4;
                    }
                }
                else if (i9 == 85 && i10 == 86 && i11 == 87)
                {
                    i4 = 2;
                    if (iArr[0] == 86)
                    {
                        i5 = iArr[1] == 85 ? 0 : 3;
                    }
                    else if (iArr[0] == 85)
                    {
                        i5 = iArr[1] == 87 ? 2 : 5;
                    }
                    else
                    {
                        i5 = iArr[1] == 86 ? 1 : 4;
                    }
                }
                else if (i9 == 85 && i10 == 87 && i11 == 89)
                {
                    i4 = 3;
                    if (iArr[0] == 89)
                    {
                        i5 = iArr[1] == 87 ? 0 : 3;
                    }
                    else if (iArr[0] == 87)
                    {
                        i5 = iArr[1] == 85 ? 2 : 5;
                    }
                    else
                    {
                        i5 = iArr[1] == 89 ? 1 : 4;
                    }
                }
                else if (i9 == 85 && i10 == 86 && i11 == 90)
                {
                    if (iArr[0] == 86)
                    {
                        i5 = iArr[1] == 90 ? 0 : 3;
                    }
                    else if (iArr[0] == 90)
                    {
                        i5 = iArr[1] == 85 ? 2 : 5;
                    }
                    else
                    {
                        i5 = iArr[1] == 86 ? 1 : 4;
                    }
                    i4 = 4;
                }
                else if (i9 == 85 && i10 == 89 && i11 == 90)
                {
                    i4 = 5;
                    if (iArr[0] == 89)
                    {
                        i5 = iArr[1] == 85 ? 0 : 3;
                    }
                    else if (iArr[0] == 85)
                    {
                        i5 = iArr[1] == 90 ? 2 : 5;
                    }
                    else
                    {
                        i5 = iArr[1] == 89 ? 1 : 4;
                    }
                }
                else if (i9 == 86 && i10 == 88 && i11 == 90)
                {
                    i4 = 6;
                    if (iArr[0] == 86)
                    {
                        i5 = iArr[1] == 88 ? 0 : 3;
                    }
                    else if (iArr[0] == 88)
                    {
                        i5 = iArr[1] == 90 ? 2 : 5;
                    }
                    else
                    {
                        i5 = iArr[1] == 86 ? 1 : 4;
                    }
                }
                else if (i9 == 88 && i10 == 89 && i11 == 90)
                {
                    i4 = 7;
                    if (iArr[0] == 89)
                    {
                        i5 = iArr[1] == 90 ? 0 : 3;
                    }
                    else if (iArr[0] == 90)
                    {
                        i5 = iArr[1] == 88 ? 2 : 5;
                    }
                    else
                    {
                        i5 = iArr[1] == 89 ? 1 : 4;
                    }
                }
                else
                {
                    throw new IllegalArgumentException("Impossible corner part.");
                }
                permutationItem.location = i4;
                permutationItem.orientation = i5;
                for (int i13 = 0; i13 < this.sequence.size(); i13++)
                {
                    if (this.sequence.elementAt(i13).orientation / 3 != permutationItem.orientation / 3)
                    {
                        throw new IllegalArgumentException(
                            "Corner permutation cannot be clockwise and anticlockwise at the same time.");
                    }
                }
                break;
        }
        for (int i14 = 0; i14 < this.sequence.size(); i14++)
        {
            if (this.sequence.elementAt(i14).location == permutationItem.location)
            {
                throw new IllegalArgumentException("Illegal multiple occurence of same part.");
            }
        }
        this.sequence.addElement(permutationItem);
    }

    public int getPermItemCount()
    {
        return this.sequence.size();
    }

    public void applyTo(RubiksCubeCore rubiksCubeCore, boolean z)
    {
        if (z)
        {
            applyInverseTo(rubiksCubeCore);
        }
        else
        {
            applyTo(rubiksCubeCore);
        }
    }

    @Override
    public void applyTo(RubiksCubeCore rubiksCubeCore)
    {
        int[] edgeLocations = null;
        int[] edgeOrientations = null;
        PermutationItem[] permutationItemArr = new PermutationItem[this.sequence.size()];
        for (int i = 0; i < permutationItemArr.length; i++)
        {
            permutationItemArr[i] = this.sequence.elementAt(i);
        }
        int i2 = 0;
        switch (this.type)
        {
            case 1:
                i2 = 4;
                edgeLocations = rubiksCubeCore.getSideLocations();
                edgeOrientations = rubiksCubeCore.getSideOrientations();
                break;
            case 2:
                i2 = 2;
                edgeLocations = rubiksCubeCore.getEdgeLocations();
                edgeOrientations = rubiksCubeCore.getEdgeOrientations();
                break;
            case 3:
                i2 = 3;
                edgeLocations = rubiksCubeCore.getCornerLocations();
                edgeOrientations = rubiksCubeCore.getCornerOrientations();
                break;
            default:
                return; // never reach
        }
        int i3 = 0;
        while (i3 < permutationItemArr.length - 1)
        {
            int i4 = ((permutationItemArr[i3 + 1].orientation - permutationItemArr[i3].orientation)
                      + edgeOrientations[permutationItemArr[i3].location])
                     % i2;
            edgeOrientations[permutationItemArr[i3].location] = i4 < 0 ? i2 + i4 : i4;
            i3++;
        }
        int i5 = (((this.sign + permutationItemArr[0].orientation) - permutationItemArr[i3].orientation)
                  + edgeOrientations[permutationItemArr[i3].location])
                 % i2;
        edgeOrientations[permutationItemArr[i3].location] = i5 < 0 ? i2 + i5 : i5;
        int i6 = edgeLocations[permutationItemArr[permutationItemArr.length - 1].location];
        int i7 = edgeOrientations[permutationItemArr[permutationItemArr.length - 1].location];
        for (int length = permutationItemArr.length - 1; length > 0; length--)
        {
            edgeLocations[permutationItemArr[length].location] = edgeLocations[permutationItemArr[length - 1].location];
            edgeOrientations[permutationItemArr[length].location] = edgeOrientations[permutationItemArr[length
                                                                                                        - 1].location];
        }
        edgeLocations[permutationItemArr[0].location] = i6;
        edgeOrientations[permutationItemArr[0].location] = i7;
        switch (this.type)
        {
            case 1:
                rubiksCubeCore.setSides(edgeLocations, edgeOrientations);
                break;
            case 2:
                rubiksCubeCore.setEdges(edgeLocations, edgeOrientations);
                break;
            case 3:
                rubiksCubeCore.setCorners(edgeLocations, edgeOrientations);
                break;
        }
    }

    @Override
    public void applyInverseTo(RubiksCubeCore rubiksCubeCore)
    {
        int[] edgeLocations = null;
        int[] edgeOrientations = null;
        PermutationItem[] permutationItemArr = new PermutationItem[this.sequence.size()];
        for (int i = 0; i < permutationItemArr.length; i++)
        {
            permutationItemArr[i] = this.sequence.elementAt(i);
        }
        int i2 = 0;
        switch (this.type)
        {
            case 1:
                i2 = 4;
                edgeLocations = rubiksCubeCore.getSideLocations();
                edgeOrientations = rubiksCubeCore.getSideOrientations();
                break;
            case 2:
                i2 = 2;
                edgeLocations = rubiksCubeCore.getEdgeLocations();
                edgeOrientations = rubiksCubeCore.getEdgeOrientations();
                break;
            case 3:
                i2 = 3;
                edgeLocations = rubiksCubeCore.getCornerLocations();
                edgeOrientations = rubiksCubeCore.getCornerOrientations();
                break;
            default:
                return; // never reach
        }
        int length = permutationItemArr.length - 1;
        while (length > 0)
        {
            int i3 = ((permutationItemArr[length - 1].orientation - permutationItemArr[length].orientation)
                      + edgeOrientations[permutationItemArr[length].location])
                     % i2;
            edgeOrientations[permutationItemArr[length].location] = i3 < 0 ? i2 + i3 : i3;
            length--;
        }
        int i4 = ((((-this.sign) + permutationItemArr[permutationItemArr.length - 1].orientation)
                   - permutationItemArr[length].orientation)
                  + edgeOrientations[permutationItemArr[length].location])
                 % i2;
        edgeOrientations[permutationItemArr[length].location] = i4 < 0 ? i2 + i4 : i4;
        int i5 = edgeLocations[permutationItemArr[0].location];
        int i6 = edgeOrientations[permutationItemArr[0].location];
        for (int i7 = 1; i7 < permutationItemArr.length; i7++)
        {
            edgeLocations[permutationItemArr[i7 - 1].location] = edgeLocations[permutationItemArr[i7].location];
            edgeOrientations[permutationItemArr[i7 - 1].location] = edgeOrientations[permutationItemArr[i7].location];
        }
        edgeLocations[permutationItemArr[permutationItemArr.length - 1].location] = i5;
        edgeOrientations[permutationItemArr[permutationItemArr.length - 1].location] = i6;
        switch (this.type)
        {
            case 1:
                rubiksCubeCore.setSides(edgeLocations, edgeOrientations);
                break;
            case 2:
                rubiksCubeCore.setEdges(edgeLocations, edgeOrientations);
                break;
            case 3:
                rubiksCubeCore.setCorners(edgeLocations, edgeOrientations);
                break;
        }
    }

    @Override
    public void inverse()
    {
        Vector<PermutationItem> vector = this.sequence;
        this.sequence = new Vector<>(vector.size());
        if (vector.size() > 0)
        {
            PermutationItem permutationItem = vector.elementAt(0);
            PermutationItem permutationItem2 = new PermutationItem();
            permutationItem2.orientation = permutationItem.orientation;
            permutationItem2.location = permutationItem.location;
            this.sequence.addElement(permutationItem2);
        }
        for (int size = vector.size() - 1; size >= 1; size--)
        {
            PermutationItem permutationItem3 = vector.elementAt(size);
            PermutationItem permutationItem4 = new PermutationItem();
            permutationItem4.orientation = permutationItem3.orientation;
            permutationItem4.location = permutationItem3.location;
            this.sequence.addElement(permutationItem4);
        }
        switch (this.type)
        {
            case 1:
                if (this.sign != 0)
                {
                    this.sign = 4 - this.sign;
                    for (int i = 1; i < this.sequence.size(); i++)
                    {
                        PermutationItem permutationItem5 = this.sequence.elementAt(i);
                        permutationItem5.orientation = (this.sign + permutationItem5.orientation) % 4;
                    }
                }
                break;
            case 2:
                if (this.sign != 0)
                {
                    for (int i2 = 1; i2 < this.sequence.size(); i2++)
                    {
                        PermutationItem permutationItem6 = this.sequence.elementAt(i2);
                        permutationItem6.orientation = this.sign ^ permutationItem6.orientation;
                    }
                }
                break;
            case 3:
                if (this.sign != 0)
                {
                    this.sign = 3 - this.sign;
                    for (int i3 = 1; i3 < this.sequence.size(); i3++)
                    {
                        PermutationItem permutationItem7 = this.sequence.elementAt(i3);
                        permutationItem7.orientation = (this.sign + permutationItem7.orientation) % 3;
                    }
                }
                break;
        }
    }

    @Override
    public void reflect()
    {}

    @Override
    public Enumeration<DefaultMutableTreeNode> resolvedEnumeration(boolean z)
    {
        if (z)
        {
            PermutationNode permutationNode = (PermutationNode)clone();
            permutationNode.inverse();
            return new SingletonEnumeration(permutationNode);
        }
        return new SingletonEnumeration(this);
    }

    @Override
    public void transform(int i)
    {
        RubiksCubeCore rubiksCubeCore = new RubiksCubeCore();
        int axis = ScriptParser.getAxis(i);
        int layerMask = ScriptParser.getLayerMask(i);
        int angle = ScriptParser.getAngle(i);
        if (axis == -1 || angle == 0 || layerMask == -1)
        {
            return;
        }
        rubiksCubeCore.transform(axis, layerMask, angle);
        applyTo(rubiksCubeCore);
        rubiksCubeCore.transform(axis, layerMask, -angle);
        int[] sideLocations = null;
        int[] sideOrientations = null;
        int i2 = 0;
        switch (this.type)
        {
            case 1:
                i2 = 4;
                sideLocations = rubiksCubeCore.getSideLocations();
                sideOrientations = rubiksCubeCore.getSideOrientations();
                break;
            case 2:
                i2 = 2;
                sideLocations = rubiksCubeCore.getEdgeLocations();
                sideOrientations = rubiksCubeCore.getEdgeOrientations();
                break;
            case 3:
                i2 = 3;
                sideLocations = rubiksCubeCore.getCornerLocations();
                sideOrientations = rubiksCubeCore.getCornerOrientations();
                break;
            default:
                return; // never reach
        }
        this.sequence.removeAllElements();
        boolean[] zArr = new boolean[sideLocations.length];
        int i3 = 0;
        while (i3 < sideLocations.length && sideLocations[i3] == i3 && sideOrientations[i3] == 0)
        {
            i3++;
        }
        PermutationItem permutationItem = new PermutationItem();
        permutationItem.location = i3;
        permutationItem.orientation = 0;
        this.sequence.addElement(permutationItem);
        zArr[i3] = true;
        int i4 = 0;
        int i5 = 0;
        while (sideLocations[i5] != i3)
        {
            i5++;
        }
        while (!zArr[i5])
        {
            zArr[i5] = true;
            i4 = ((i2 + i4) + sideOrientations[i5]) % i2;
            PermutationItem permutationItem2 = new PermutationItem();
            permutationItem2.location = i5;
            permutationItem2.orientation = i4;
            this.sequence.addElement(permutationItem2);
            int i6 = 0;
            while (sideLocations[i6] != i5)
            {
                i6++;
            }
            i5 = i6;
        }
        this.sign = ((i2 + i4) + sideOrientations[i3]) % i2;
    }

    @Override
    public Object clone()
    {
        PermutationNode permutationNode = (PermutationNode)super.clone();
        permutationNode.sequence = new Vector<>();
        Enumeration<PermutationItem> enumerationElements = this.sequence.elements();
        while (enumerationElements.hasMoreElements())
        {
            permutationNode.sequence.addElement((PermutationItem)enumerationElements.nextElement().clone());
        }
        return permutationNode;
    }
}
