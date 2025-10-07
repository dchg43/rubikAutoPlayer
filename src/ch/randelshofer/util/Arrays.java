package ch.randelshofer.util;

public class Arrays {
    private Arrays() {
    }

    @SuppressWarnings("unused")
    private static void sort1(int[] iArr, int i, int i2) {
        if (i2 < 7) {
            for (int i3 = i; i3 < i2 + i; i3++) {
                for (int i4 = i3; i4 > i && iArr[i4 - 1] > iArr[i4]; i4--) {
                    swap(iArr, i4, i4 - 1);
                }
            }
            return;
        }
        int iMed3 = i + (i2 / 2);
        if (i2 > 7) {
            int iMed32 = i;
            int iMed33 = (i + i2) - 1;
            if (i2 > 40) {
                int i5 = i2 / 8;
                iMed32 = med3(iArr, iMed32, iMed32 + i5, iMed32 + (2 * i5));
                iMed3 = med3(iArr, iMed3 - i5, iMed3, iMed3 + i5);
                iMed33 = med3(iArr, iMed33 - (2 * i5), iMed33 - i5, iMed33);
            }
            iMed3 = med3(iArr, iMed32, iMed3, iMed33);
        }
        int i6 = iArr[iMed3];
        int i7 = i;
        int i8 = i7;
        int i9 = (i + i2) - 1;
        int i10 = i9;
        while (true) {
            if (i8 > i9 || iArr[i8] > i6) {
                while (i9 >= i8 && iArr[i9] >= i6) {
                    if (iArr[i9] == i6) {
                        int i11 = i10;
                        i10 = i11 - 1;
                        swap(iArr, i9, i11);
                    }
                    i9--;
                }
                if (i8 > i9) {
                    break;
                }
                int i12 = i8;
                i8++;
                int i13 = i9;
                i9 = i13 - 1;
                swap(iArr, i12, i13);
            } else {
                if (iArr[i8] == i6) {
                    int i14 = i7;
                    i7++;
                    swap(iArr, i14, i8);
                }
                i8++;
            }
        }
        int i15 = i + i2;
        int iMin = Math.min(i7 - i, i8 - i7);
        vecswap(iArr, i, i8 - iMin, iMin);
        int iMin2 = Math.min(i10 - i9, (i15 - i10) - 1);
        vecswap(iArr, i8, i15 - iMin2, iMin2);
        int i16 = i8 - i7;
        if (i16 > 1) {
            sort1(iArr, i, i16);
        }
        int i17 = i10 - i9;
        if (i17 > 1) {
            sort1(iArr, i15 - i17, i17);
        }
    }

    private static void swap(int[] iArr, int i, int i2) {
        int i3 = iArr[i];
        iArr[i] = iArr[i2];
        iArr[i2] = i3;
    }

    private static void vecswap(int[] iArr, int i, int i2, int i3) {
        int i4 = 0;
        while (i4 < i3) {
            swap(iArr, i, i2);
            i4++;
            i++;
            i2++;
        }
    }

    private static int med3(int[] iArr, int i, int i2, int i3) {
        return iArr[i] < iArr[i2] ? iArr[i2] < iArr[i3] ? i2 : iArr[i] < iArr[i3] ? i3 : i : iArr[i2] > iArr[i3] ? i2 : iArr[i] > iArr[i3] ? i3 : i;
    }

    public static void sort(Object[] objArr) {
        mergeSort(objArr.clone(), objArr, 0, objArr.length);
    }

    public static void sort(Object[] objArr, int i, int i2) {
        rangeCheck(objArr.length, i, i2);
        mergeSort(objArr.clone(), objArr, i, i2);
    }

    private static void mergeSort(Object[] objArr, Object[] objArr2, int i, int i2) {
        int i3 = i2 - i;
        if (i3 < 7) {
            for (int i4 = i; i4 < i2; i4++) {
                for (int i5 = i4; i5 > i && ((Comparable) objArr2[i5 - 1]).compareTo(objArr2[i5]) > 0; i5--) {
                    swap(objArr2, i5, i5 - 1);
                }
            }
            return;
        }
        int i6 = (i + i2) / 2;
        mergeSort(objArr2, objArr, i, i6);
        mergeSort(objArr2, objArr, i6, i2);
        if (((Comparable) objArr[i6 - 1]).compareTo(objArr[i6]) <= 0) {
            System.arraycopy(objArr, i, objArr2, i, i3);
            return;
        }
        int i7 = i;
        int i8 = i6;
        for (int i9 = i; i9 < i2; i9++) {
            if (i8 >= i2 || (i7 < i6 && ((Comparable) objArr[i7]).compareTo(objArr[i8]) <= 0)) {
                int i10 = i7;
                i7++;
                objArr2[i9] = objArr[i10];
            } else {
                int i11 = i8;
                i8++;
                objArr2[i9] = objArr[i11];
            }
        }
    }

    private static void swap(Object[] objArr, int i, int i2) {
        Object obj = objArr[i];
        objArr[i] = objArr[i2];
        objArr[i2] = obj;
    }

    private static void rangeCheck(int i, int i2, int i3) {
        if (i2 > i3) {
            throw new IllegalArgumentException(new StringBuffer().append("fromIndex(").append(i2).append(") > toIndex(").append(i3).append(")").toString());
        }
        if (i2 < 0) {
            throw new ArrayIndexOutOfBoundsException(i2);
        }
        if (i3 > i) {
            throw new ArrayIndexOutOfBoundsException(i3);
        }
    }

    public static boolean equals(int[] iArr, int[] iArr2) {
        int length;
        if (iArr == iArr2) {
            return true;
        }
        if (iArr == null || iArr2 == null || iArr2.length != (length = iArr.length)) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (iArr[i] != iArr2[i]) {
                return false;
            }
        }
        return true;
    }
}
