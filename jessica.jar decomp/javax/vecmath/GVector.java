/*
 * Decompiled with CFR 0.152.
 */
package javax.vecmath;

import java.io.Serializable;
import javax.vecmath.GMatrix;
import javax.vecmath.MismatchedSizeException;
import javax.vecmath.Tuple2f;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4d;
import javax.vecmath.Tuple4f;
import javax.vecmath.VecMathI18N;
import javax.vecmath.VecMathUtil;

public class GVector
implements Serializable,
Cloneable {
    private int length;
    double[] values;
    static final long serialVersionUID = 1398850036893875112L;

    public GVector(int length) {
        this.length = length;
        this.values = new double[length];
        int i = 0;
        while (i < length) {
            this.values[i] = 0.0;
            ++i;
        }
    }

    public GVector(double[] vector) {
        this.length = vector.length;
        this.values = new double[vector.length];
        int i = 0;
        while (i < this.length) {
            this.values[i] = vector[i];
            ++i;
        }
    }

    public GVector(GVector vector) {
        this.values = new double[vector.length];
        this.length = vector.length;
        int i = 0;
        while (i < this.length) {
            this.values[i] = vector.values[i];
            ++i;
        }
    }

    public GVector(Tuple2f tuple) {
        this.values = new double[2];
        this.values[0] = tuple.x;
        this.values[1] = tuple.y;
        this.length = 2;
    }

    public GVector(Tuple3f tuple) {
        this.values = new double[3];
        this.values[0] = tuple.x;
        this.values[1] = tuple.y;
        this.values[2] = tuple.z;
        this.length = 3;
    }

    public GVector(Tuple3d tuple) {
        this.values = new double[3];
        this.values[0] = tuple.x;
        this.values[1] = tuple.y;
        this.values[2] = tuple.z;
        this.length = 3;
    }

    public GVector(Tuple4f tuple) {
        this.values = new double[4];
        this.values[0] = tuple.x;
        this.values[1] = tuple.y;
        this.values[2] = tuple.z;
        this.values[3] = tuple.w;
        this.length = 4;
    }

    public GVector(Tuple4d tuple) {
        this.values = new double[4];
        this.values[0] = tuple.x;
        this.values[1] = tuple.y;
        this.values[2] = tuple.z;
        this.values[3] = tuple.w;
        this.length = 4;
    }

    public GVector(double[] vector, int length) {
        this.length = length;
        this.values = new double[length];
        int i = 0;
        while (i < length) {
            this.values[i] = vector[i];
            ++i;
        }
    }

    public final double norm() {
        double sq = 0.0;
        int i = 0;
        while (i < this.length) {
            sq += this.values[i] * this.values[i];
            ++i;
        }
        return Math.sqrt(sq);
    }

    public final double normSquared() {
        double sq = 0.0;
        int i = 0;
        while (i < this.length) {
            sq += this.values[i] * this.values[i];
            ++i;
        }
        return sq;
    }

    public final void normalize(GVector v1) {
        double sq = 0.0;
        if (this.length != v1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector0"));
        }
        int i = 0;
        while (i < this.length) {
            sq += v1.values[i] * v1.values[i];
            ++i;
        }
        double invMag = 1.0 / Math.sqrt(sq);
        i = 0;
        while (i < this.length) {
            this.values[i] = v1.values[i] * invMag;
            ++i;
        }
    }

    public final void normalize() {
        double sq = 0.0;
        int i = 0;
        while (i < this.length) {
            sq += this.values[i] * this.values[i];
            ++i;
        }
        double invMag = 1.0 / Math.sqrt(sq);
        i = 0;
        while (i < this.length) {
            this.values[i] = this.values[i] * invMag;
            ++i;
        }
    }

    public final void scale(double s, GVector v1) {
        if (this.length != v1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector1"));
        }
        int i = 0;
        while (i < this.length) {
            this.values[i] = v1.values[i] * s;
            ++i;
        }
    }

    public final void scale(double s) {
        int i = 0;
        while (i < this.length) {
            this.values[i] = this.values[i] * s;
            ++i;
        }
    }

    public final void scaleAdd(double s, GVector v1, GVector v2) {
        if (v2.length != v1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector2"));
        }
        if (this.length != v1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector3"));
        }
        int i = 0;
        while (i < this.length) {
            this.values[i] = v1.values[i] * s + v2.values[i];
            ++i;
        }
    }

    public final void add(GVector vector) {
        if (this.length != vector.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector4"));
        }
        int i = 0;
        while (i < this.length) {
            int n = i;
            this.values[n] = this.values[n] + vector.values[i];
            ++i;
        }
    }

    public final void add(GVector vector1, GVector vector2) {
        if (vector1.length != vector2.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector5"));
        }
        if (this.length != vector1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector6"));
        }
        int i = 0;
        while (i < this.length) {
            this.values[i] = vector1.values[i] + vector2.values[i];
            ++i;
        }
    }

    public final void sub(GVector vector) {
        if (this.length != vector.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector7"));
        }
        int i = 0;
        while (i < this.length) {
            int n = i;
            this.values[n] = this.values[n] - vector.values[i];
            ++i;
        }
    }

    public final void sub(GVector vector1, GVector vector2) {
        if (vector1.length != vector2.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector8"));
        }
        if (this.length != vector1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector9"));
        }
        int i = 0;
        while (i < this.length) {
            this.values[i] = vector1.values[i] - vector2.values[i];
            ++i;
        }
    }

    public final void mul(GMatrix m1, GVector v1) {
        if (m1.getNumCol() != v1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector10"));
        }
        if (this.length != m1.getNumRow()) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector11"));
        }
        double[] v = v1 != this ? v1.values : (double[])this.values.clone();
        int j = this.length - 1;
        while (j >= 0) {
            this.values[j] = 0.0;
            int i = v1.length - 1;
            while (i >= 0) {
                int n = j;
                this.values[n] = this.values[n] + m1.values[j][i] * v[i];
                --i;
            }
            --j;
        }
    }

    public final void mul(GVector v1, GMatrix m1) {
        if (m1.getNumRow() != v1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector12"));
        }
        if (this.length != m1.getNumCol()) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector13"));
        }
        double[] v = v1 != this ? v1.values : (double[])this.values.clone();
        int j = this.length - 1;
        while (j >= 0) {
            this.values[j] = 0.0;
            int i = v1.length - 1;
            while (i >= 0) {
                int n = j;
                this.values[n] = this.values[n] + m1.values[i][j] * v[i];
                --i;
            }
            --j;
        }
    }

    public final void negate() {
        int i = this.length - 1;
        while (i >= 0) {
            int n = i--;
            this.values[n] = this.values[n] * -1.0;
        }
    }

    public final void zero() {
        int i = 0;
        while (i < this.length) {
            this.values[i] = 0.0;
            ++i;
        }
    }

    public final void setSize(int length) {
        double[] tmp = new double[length];
        int max = this.length < length ? this.length : length;
        int i = 0;
        while (i < max) {
            tmp[i] = this.values[i];
            ++i;
        }
        this.length = length;
        this.values = tmp;
    }

    public final void set(double[] vector) {
        int i = this.length - 1;
        while (i >= 0) {
            this.values[i] = vector[i];
            --i;
        }
    }

    public final void set(GVector vector) {
        if (this.length < vector.length) {
            this.length = vector.length;
            this.values = new double[this.length];
            int i = 0;
            while (i < this.length) {
                this.values[i] = vector.values[i];
                ++i;
            }
        } else {
            int i = 0;
            while (i < vector.length) {
                this.values[i] = vector.values[i];
                ++i;
            }
            i = vector.length;
            while (i < this.length) {
                this.values[i] = 0.0;
                ++i;
            }
        }
    }

    public final void set(Tuple2f tuple) {
        if (this.length < 2) {
            this.length = 2;
            this.values = new double[2];
        }
        this.values[0] = tuple.x;
        this.values[1] = tuple.y;
        int i = 2;
        while (i < this.length) {
            this.values[i] = 0.0;
            ++i;
        }
    }

    public final void set(Tuple3f tuple) {
        if (this.length < 3) {
            this.length = 3;
            this.values = new double[3];
        }
        this.values[0] = tuple.x;
        this.values[1] = tuple.y;
        this.values[2] = tuple.z;
        int i = 3;
        while (i < this.length) {
            this.values[i] = 0.0;
            ++i;
        }
    }

    public final void set(Tuple3d tuple) {
        if (this.length < 3) {
            this.length = 3;
            this.values = new double[3];
        }
        this.values[0] = tuple.x;
        this.values[1] = tuple.y;
        this.values[2] = tuple.z;
        int i = 3;
        while (i < this.length) {
            this.values[i] = 0.0;
            ++i;
        }
    }

    public final void set(Tuple4f tuple) {
        if (this.length < 4) {
            this.length = 4;
            this.values = new double[4];
        }
        this.values[0] = tuple.x;
        this.values[1] = tuple.y;
        this.values[2] = tuple.z;
        this.values[3] = tuple.w;
        int i = 4;
        while (i < this.length) {
            this.values[i] = 0.0;
            ++i;
        }
    }

    public final void set(Tuple4d tuple) {
        if (this.length < 4) {
            this.length = 4;
            this.values = new double[4];
        }
        this.values[0] = tuple.x;
        this.values[1] = tuple.y;
        this.values[2] = tuple.z;
        this.values[3] = tuple.w;
        int i = 4;
        while (i < this.length) {
            this.values[i] = 0.0;
            ++i;
        }
    }

    public final int getSize() {
        return this.values.length;
    }

    public final double getElement(int index) {
        return this.values[index];
    }

    public final void setElement(int index, double value) {
        this.values[index] = value;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(this.length * 8);
        int i = 0;
        while (i < this.length) {
            buffer.append(this.values[i]).append(" ");
            ++i;
        }
        return buffer.toString();
    }

    public int hashCode() {
        long bits = 1L;
        int i = 0;
        while (i < this.length) {
            bits = VecMathUtil.hashDoubleBits(bits, this.values[i]);
            ++i;
        }
        return VecMathUtil.hashFinish(bits);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean equals(GVector vector1) {
        try {
            if (this.length != vector1.length) {
                return false;
            }
            int i = 0;
            while (true) {
                if (i >= this.length) {
                    return true;
                }
                if (this.values[i] != vector1.values[i]) {
                    return false;
                }
                ++i;
            }
        }
        catch (NullPointerException e2) {
            return false;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean equals(Object o1) {
        try {
            GVector v2 = (GVector)o1;
            if (this.length != v2.length) {
                return false;
            }
            int i = 0;
            while (true) {
                if (i >= this.length) {
                    return true;
                }
                if (this.values[i] != v2.values[i]) {
                    return false;
                }
                ++i;
            }
        }
        catch (ClassCastException e1) {
            return false;
        }
        catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean epsilonEquals(GVector v1, double epsilon) {
        if (this.length != v1.length) {
            return false;
        }
        int i = 0;
        while (i < this.length) {
            double diff = this.values[i] - v1.values[i];
            double d = diff < 0.0 ? -diff : diff;
            if (d > epsilon) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public final double dot(GVector v1) {
        if (this.length != v1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector14"));
        }
        double result = 0.0;
        int i = 0;
        while (i < this.length) {
            result += this.values[i] * v1.values[i];
            ++i;
        }
        return result;
    }

    public final void SVDBackSolve(GMatrix U, GMatrix W, GMatrix V, GVector b) {
        if (U.nRow != b.getSize() || U.nRow != U.nCol || U.nRow != W.nRow) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector15"));
        }
        if (W.nCol != this.values.length || W.nCol != V.nCol || W.nCol != V.nRow) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector23"));
        }
        GMatrix tmp = new GMatrix(U.nRow, W.nCol);
        tmp.mul(U, V);
        tmp.mulTransposeRight(U, W);
        tmp.invert();
        this.mul(tmp, b);
    }

    public final void LUDBackSolve(GMatrix LU, GVector b, GVector permutation) {
        int size = LU.nRow * LU.nCol;
        double[] temp = new double[size];
        double[] result = new double[size];
        int[] row_perm = new int[b.getSize()];
        if (LU.nRow != b.getSize()) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector16"));
        }
        if (LU.nRow != permutation.getSize()) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector24"));
        }
        if (LU.nRow != LU.nCol) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector25"));
        }
        int i = 0;
        while (i < LU.nRow) {
            int j = 0;
            while (j < LU.nCol) {
                temp[i * LU.nCol + j] = LU.values[i][j];
                ++j;
            }
            ++i;
        }
        i = 0;
        while (i < size) {
            result[i] = 0.0;
            ++i;
        }
        i = 0;
        while (i < LU.nRow) {
            result[i * LU.nCol] = b.values[i];
            ++i;
        }
        i = 0;
        while (i < LU.nCol) {
            row_perm[i] = (int)permutation.values[i];
            ++i;
        }
        GMatrix.luBacksubstitution(LU.nRow, temp, row_perm, result);
        i = 0;
        while (i < LU.nRow) {
            this.values[i] = result[i * LU.nCol];
            ++i;
        }
    }

    public final double angle(GVector v1) {
        return Math.acos(this.dot(v1) / (this.norm() * v1.norm()));
    }

    public final void interpolate(GVector v1, GVector v2, float alpha) {
        this.interpolate(v1, v2, (double)alpha);
    }

    public final void interpolate(GVector v1, float alpha) {
        this.interpolate(v1, (double)alpha);
    }

    public final void interpolate(GVector v1, GVector v2, double alpha) {
        if (v2.length != v1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector20"));
        }
        if (this.length != v1.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector21"));
        }
        int i = 0;
        while (i < this.length) {
            this.values[i] = (1.0 - alpha) * v1.values[i] + alpha * v2.values[i];
            ++i;
        }
    }

    public final void interpolate(GVector v1, double alpha) {
        if (v1.length != this.length) {
            throw new MismatchedSizeException(VecMathI18N.getString("GVector22"));
        }
        int i = 0;
        while (i < this.length) {
            this.values[i] = (1.0 - alpha) * this.values[i] + alpha * v1.values[i];
            ++i;
        }
    }

    public Object clone() {
        GVector v1 = null;
        try {
            v1 = (GVector)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        v1.values = new double[this.length];
        int i = 0;
        while (i < this.length) {
            v1.values[i] = this.values[i];
            ++i;
        }
        return v1;
    }
}

