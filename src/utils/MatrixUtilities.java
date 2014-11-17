package utils;

import javolution.util.Index;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;
import org.jscience.mathematics.vector.SparseVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MatrixUtilities {

    /**
     * Combine two matrices by place them next to each others
     * @param matrix1
     * @param matrix2
     * @return combined matrix
     * @throws IllegalArgumentException
     */
    public static DenseMatrix<Real> combineMatrix(DenseMatrix<Real> matrix1, DenseMatrix<Real> matrix2) throws IllegalArgumentException {
        if (matrix1.getNumberOfRows() != matrix2.getNumberOfRows()) {
            // If two matrix do not have same rows
            // throw an exception
            throw new IllegalArgumentException("Two matrix do not have same number of rows");
        } else {

            // get the dimension of new matrix
            int numRows = matrix1.getNumberOfRows();
            int numColumns = matrix1.getNumberOfColumns() + matrix2.getNumberOfColumns();

            // init data
            Real w[][] = new Real[numRows][numColumns];

            // set data
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < matrix1.getNumberOfColumns(); j++) {
                    w[i][j] = matrix1.get(i, j);
                }
                for (int j = 0; j < matrix2.getNumberOfColumns(); j++) {
                    w[i][j + matrix1.getNumberOfColumns()] = matrix2.get(i, j);
                }
            }

            // finish
            return DenseMatrix.valueOf(w);
        }
    }


    /**
     * Normalize a matrix
     * @param matrix
     * @return normalized matrix
     */
    public static DenseMatrix<Real> normalizeMatrix(DenseMatrix<Real> matrix) {

        // get the dimension of the matrix
        int numRows = matrix.getNumberOfRows();
        int numColumns = matrix.getNumberOfColumns();

        // init the vectors

        List<DenseVector<Real>> list = new ArrayList<DenseVector<Real>>();
        for (int i = 0; i < numRows; i++) {

            // get the vector of row ith
            DenseVector<Real> denseVector = matrix.getRow(i);

            // calculate the sum of all vector's elements
            Real temp = Real.ZERO;
            for (int j = 0; j < numColumns; j++) {
                temp = temp.plus(denseVector.get(j));
            }

            // normalize
            if (temp.compareTo(Real.ZERO) != 0) {

                // if sum !=0 => normalize
                denseVector = denseVector.times(Real.ONE.divide(temp));

            } else {

                // if sum=0 we set all elements is equal =1/ numcolumns
                List<Real> vector = new ArrayList<Real>();
                Real value = Real.ONE.divide(numColumns);
                for (int j = 0; j < numColumns; j++) {
                    vector.add(value);
                }
                denseVector = DenseVector.valueOf(vector);
            }

            // add new vector to the list
            list.add(denseVector);
        }

        // finish
        return DenseMatrix.valueOf(list);
    }


    /**
     * Normalize vector
     * @param vector
     * @return
     */
    public static DenseVector<Real> normalizeVector(DenseVector<Real> vector) {
        Real w[] = new Real[vector.getDimension()];
        Real sum = Real.ZERO;
        for (int i = 0; i < vector.getDimension(); i++) {
            sum = vector.get(i).plus(sum);
        }
        for (int i = 0; i < vector.getDimension(); i++) {
            w[i] = vector.get(i).divide(sum);
        }

        return DenseVector.valueOf(w);
    }


    /**
     * Print a matrix
     * @param matrix
     */
    public static void printMatrix(DenseMatrix<Real> matrix) {
        System.out.println(matrix.toText());
    }


    /**
     * Export an matrix to file
     * @param name
     * @param matrix
     */
    public static void toFile(String name, DenseMatrix<Real> matrix) {
        try {
            File file = new File(name);

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(matrix.toText().toString());
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
    public static Real divergenceVector(DenseVector<Real> denseVector1, DenseVector<Real> denseVector2) throws IllegalArgumentException {
        if (denseVector1.getDimension() != denseVector2.getDimension()) {
            throw new IllegalArgumentException("2 vector khong cung chieu");
        } else {
            denseVector1 = MatrixUtilities.normalizeVector(denseVector1);
            denseVector2 = MatrixUtilities.normalizeVector(denseVector2);

            int dimension = denseVector1.getDimension();
            Real result = Real.ZERO;

            for (int i = 0; i < dimension; i++) {
                Real temp1 = denseVector1.get(i);
                Real temp2 = denseVector2.get(i);


                Real value = temp1.times(Real.valueOf(Math.log(temp1.divide(temp2).doubleValue())));

                result = result.plus(value);
            }
            return result;
        }
    }


    /**
     * Compute the divergence between all pair of vectors in an matrix
     * @param matrix
     * @return
     */
    public static Real divergenceMatrix(DenseMatrix<Real> matrix) {
        int numRows = matrix.getNumberOfRows();
        Real result = Real.ZERO;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numRows; j++) {
                if (i != j) {
                    Real value = divergenceVector(matrix.getRow(i), matrix.getRow(j));
                    result = result.plus(value);

                }
            }
        }
        return result.divide(numRows).divide(numRows - 1);
    }


    public static DenseMatrix<Real> convertArrayToMatrix(double w[][], int numRows, int numColumns) {
        List<DenseVector<Real>> list = new ArrayList<DenseVector<Real>>();
        for (int i = 0; i < numRows; i++) {
            List<Real> temp=new ArrayList<Real>();
            for (int j=0; j<numColumns; j++){
                temp.add(Real.valueOf(w[i][j]));
            }
            list.add(DenseVector.valueOf(temp));
        }
        return DenseMatrix.valueOf(list);
    }


    public static Real distanceBetweenVector(DenseVector<Real> vector1, DenseVector<Real> vector2) {
        Real result = Real.ZERO;
        if (vector1.getDimension() != vector2.getDimension()) {
            return null;
        } else {
            for (int i = 0; i < vector1.getDimension(); i++) {
                result = result.plus(vector1.get(i).minus(vector2.get(i)).pow(2));
            }
            return result.sqrt();
        }
    }
}
