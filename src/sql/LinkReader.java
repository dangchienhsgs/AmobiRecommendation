package sql;

import javolution.util.Index;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LinkReader {
    public static final String CLICKS="clicks";
    public static final String VIEWS="views";
    public static final String WIDGET_CODE="widget_code";
    public static final String LINK_ID="link_id";

    private List<Integer> listAdvIndex;
    private List<String> listAppIndex;
    private HashMap<Integer, Integer> linkIdConverter;

    public LinkReader(List<Integer> listAdvIndex, List<String> listAppIndex, HashMap<Integer, Integer> linkIdConverter) {
        this.listAdvIndex = listAdvIndex;
        this.listAppIndex = listAppIndex;
        this.linkIdConverter = linkIdConverter;
    }

    public DenseMatrix<Real> read(){
        try{
            Class.forName(Config.DB_DRIVER);
            Connection connection= DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASSWORD);

            String sql="SELECT * FROM widget_publisher_code";

            ResultSet resultSet=connection.createStatement().executeQuery(sql);

            int numRows=listAdvIndex.size();
            int numColumns=listAppIndex.size();

            System.out.println (numRows +" "+numColumns);
            Real[][] array=new Real[numRows][numColumns];
            for (int i=0; i<numRows; i++){
                Arrays.fill(array[i], Real.ZERO);
            }

            while (resultSet.next()){
                int clicks=resultSet.getInt(CLICKS);
                int views=resultSet.getInt(VIEWS);

                // choose value=clicks+views;
                int value=clicks+views;

                String code=resultSet.getString(WIDGET_CODE);
                int linkID=resultSet.getInt(LINK_ID);

                int appIndex=listAppIndex.indexOf(code);
                int advIndex=listAdvIndex.indexOf(linkIdConverter.get(linkID));

                if (appIndex>=0 & advIndex>=0){
                    array[advIndex][appIndex]=array[advIndex][appIndex].plus(Real.valueOf(value));
                }
            }

            DenseMatrix matrix=DenseMatrix.valueOf(array);

            connection.close();
            return matrix;

        } catch (ClassNotFoundException e){
            e.printStackTrace();
            return null;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

}
