package com.dangchienhsgs.utils;


import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class MatrixUtilities {

    /**
     * Combine two matrices by place them next to each others
     * @param matrix1
     * @param matrix2
     * @return combined matrix
     * @throws IllegalArgumentException
     */
    public static Matrix combineMatrix(Matrix matrix1, Matrix matrix2) throws IllegalArgumentException {
        if (matrix1.rows() != matrix2.rows()) {
            // If two matrix do not have same rows
            // throw an exception
            throw new IllegalArgumentException("Two matrix do not have same number of rows");
        } else {

            // get the dimension of new matrix
            int numRows = matrix1.rows();
            int numColumns = matrix1.rows() + matrix2.rows();

            // init data
            double w[][] = new double[numRows][numColumns];

            // set data
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < matrix1.columns(); j++) {
                    w[i][j] = matrix1.get(i, j);
                }
                for (int j = 0; j < matrix2.columns(); j++) {
                    w[i][j + matrix1.columns()] = matrix2.get(i, j);
                }
            }

            // finish
            return new Basic2DMatrix(w);
        }
    }


    /**
     * Normalize a matrix
     * @param matrix
     * @return normalized matrix
     */
    public static Matrix normalizeMatrix(Matrix matrix) {

        // get the dimension of the matrix
        int numRows = matrix.rows();
        int numColumns = matrix.columns();

        // init the vectors

        for (int i=0; i<numRows;i++){
            double sum=0;
            
            for (int j=0; j<numColumns; j++){
                sum=sum+matrix.get(i, j);
            }

            if (sum!=0){
                for (int j=0; j<numColumns; j++){
                    matrix.set(i, j, matrix.get(i, j)/sum);
                }
            } else {
                for (int j=0; j<numColumns; j++){
                    matrix.set(i, j, Double.valueOf(1)/numColumns);
                }
            }

        }
        
        return matrix;
    }


    /**
     * Normalize vector
     * @param vector
     * @return
     */
    public static Vector normalizeVector(Vector vector) {
        Double sum = Double.valueOf(0);
        for (int i = 0; i < vector.length(); i++) {
            sum = vector.get(i)+sum;
        }
        for (int i = 0; i < vector.length(); i++) {
            vector.set(i, vector.get(i)/sum);
        }

        return vector;
    }


    /**
     * Print a matrix
     * @param matrix
     */
    public static void printMatrix(Matrix matrix) {
        System.out.println(matrix.toString());
    }


    /**
     * Export an matrix to file
     * @param name
     * @param matrix
     */
    public static void toFile(String name, Matrix matrix) {
        try {
            File file = new File(name);

            if (!file.exists()) {
                file.createNewFile();
            }
            
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(matrix.toString().toString());
            fileWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Compute the divergence between two vector (the difference between two vector)
     * @param denseVector1
     * @param denseVector2
     * @return
     * @throws IllegalArgumentException
     */
    public static Double divergenceVector(Vector denseVector1, Vector denseVector2) throws IllegalArgumentException {
        if (denseVector1.length() != denseVector2.length()) {
            throw new IllegalArgumentException("2 vector khong cung chieu");
        } else {
            denseVector1 = MatrixUtilities.normalizeVector(denseVector1);
            denseVector2 = MatrixUtilities.normalizeVector(denseVector2);

            int dimension = denseVector1.length();
            Double result = Double.valueOf(0);

            for (int i = 0; i < dimension; i++) {
                Double temp1 = denseVector1.get(i);
                Double temp2 = denseVector2.get(i);


                Double value = temp1*(Double.valueOf(Math.log(temp1/temp2)));
                
                result = result+(value);
            }
            return result;
        }
    }


    /**
     * Compute the divergence between all pair of vectors in an matrix
     * @param matrix
     * @return
     */
    public static Double divergenceMatrix(Matrix matrix) {
        int numRows = matrix.rows();
        Double result = Double.valueOf(0);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numRows; j++) {
                if (i != j) {
                    Double value = divergenceVector(matrix.getRow(i), matrix.getRow(j));
                    result = result+(value);

                }
            }
        }
        return result/(numRows)/(numRows - 1);
    }


    public static Matrix convertArrayToMatrix(double w[][], int numRows, int numColumns) {
        return new Basic2DMatrix(w);
    }


    public static Double distanceBetweenVector(Vector vector1, Vector vector2) {
        Double result = Double.valueOf(0);
        if (vector1.length() != vector2.length()) {
            return null;
        } else {
            for (int i = 0; i < vector1.length(); i++) {
                result = result+Math.pow(vector1.get(i) - (vector2.get(i)), 2);
            }
            return Math.sqrt(result);
        }
    }
}
