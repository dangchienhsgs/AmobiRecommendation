package utils;

import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;

import java.util.ArrayList;
import java.util.List;


public class KMeans {
    private DenseMatrix<Real> data;
    private int numRows;
    private int K;
    private int dimension;
    private DenseVector<Real> center[];
    private DenseVector<Real> x[];
    private double w[][];


    public KMeans(int K, DenseMatrix<Real> data) {
        this.K = K;
        this.data = data;
    }

    public void init() {

        numRows = data.getNumberOfRows();
        dimension = data.getNumberOfColumns();

        x = new DenseVector[numRows];
        for (int i = 0; i < numRows; i++) {
            x[i] = data.getRow(i);
        }


        // Init center
        center = new DenseVector[K];

        w = new double[numRows][K];
        for (int i = 0; i < numRows; i++) {
            w[i] = new NumberUtils().randomLine(K);
        }
    }

    public void eStep() {
        for (int i = 0; i < K; i++) {
            DenseVector<Real> sum = x[0].times(Real.valueOf(w[0][i]));
            double temp = w[0][i];
            for (int j = 1; j < numRows; j++) {
                sum = sum.plus(x[j].times(Real.valueOf(w[j][i])));
                temp = temp + w[j][i];
            }
            if (temp != 0) {
                center[i] = sum.times(Real.ONE.divide(Real.valueOf(temp)));
            }
        }
    }

    public void mStep() {
        //System.out.println ("mStep");
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < K; j++) {
                w[i][j] = 0;
            }
        }
        for (int i = 0; i < numRows; i++) {
            Real min = MatrixUtilities.distanceBetweenVector(x[i], center[0]);
            int index = 0;
            for (int j = 1; j < K; j++) {
                Real temp = MatrixUtilities.distanceBetweenVector(x[i], center[j]);
                if (temp.compareTo(min) < 0) {
                    min = temp;
                    index = j;
                }
            }
            w[i][index] = 1;
        }
        //printW();
    }

    public DenseMatrix<Real> execute(int times) {
        init();

        for (int i = 0; i < times; i++) {
            System.out.println("KMeans has been in time " + i);
            eStep();
            mStep();
        }

        return MatrixUtilities.convertArrayToMatrix(w, numRows, K);
    }



}
