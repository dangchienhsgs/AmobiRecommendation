package com.dangchienhsgs.utils;


import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;


public class KMeans {
    private Matrix data;
    private int numRows;
    private int K;
    private int dimension;
    private Vector center[];
    private Vector x[];
    private double w[][];


    public KMeans(int K, Matrix data) {
        this.K = K;
        this.data = data;
    }

    public void init() {

        numRows = data.rows();
        dimension = data.columns();

        x = new Vector[numRows];
        for (int i = 0; i < numRows; i++) {
            x[i] = data.getRow(i);
        }


        // Init center
        center = new Vector[K];

        w = new double[numRows][K];
        for (int i = 0; i < numRows; i++) {
            w[i] = new NumberUtils().randomLine(K);
        }
    }

    public void eStep() {
        for (int i = 0; i < K; i++) {
            Vector sum = x[0].multiply(w[0][i]);
            double temp = w[0][i];
            for (int j = 1; j < numRows; j++) {
                sum = sum.add(x[j].multiply(w[j][i]));
                temp = temp + w[j][i];
            }
            if (temp != 0) {
                center[i] = sum.divide(temp);
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
            Double min = MatrixUtilities.distanceBetweenVector(x[i], center[0]);
            int index = 0;
            for (int j = 1; j < K; j++) {
                Double temp = MatrixUtilities.distanceBetweenVector(x[i], center[j]);
                if (temp.compareTo(min) < 0) {
                    min = temp;
                    index = j;
                }
            }
            w[i][index] = 1;
        }

    }


    public Matrix execute(int times) {
        init();

        for (int i = 0; i < times; i++) {
            System.out.println("KMeans has been in time " + i);
            eStep();
            mStep();
        }

        return MatrixUtilities.convertArrayToMatrix(w, numRows, K);
    }



}
