package sql;

import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;
import utils.NumberUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppReader {
    public static final String NAME="name";
    public static final String CODE="code";
    public static final String APP_AUDIENCES="audiences";
    public static final int NUMBER_FIELD_AUDIENCES=8;
    public static final String APP_CATEGORY="category";
    //public static final String APP_TYPE="type";
    public static final String APP_STATUS="status";
    public static final int MAX_APP_STATUS=3;
    public static final String APP_GENDER="gender";
    public static final int MAX_APP_GENDER=2;

    public static final String APP_SMART_STATUS="smart_status";

    public static final String APP_RATE="rate";
    public static final int APP_MAX_RATE=5;

    public static final String APP_DOWNLOAD="download";
    public static final int APP_MAX_DOWNLOAD=6784;

    public static final String APP_LIKE="like";
    public static final int APP_MAX_LIKE=24;

    private List<Integer> list_category;
    private List<Integer> list_audiences;

    public List<String> idList;
    public AppReader() {
        list_category= CategoryReader.getListCategory();
        idList=new ArrayList<String>();

        list_audiences=new ArrayList<Integer>();

        list_audiences.add(1);
        list_audiences.add(2);
        list_audiences.add(3);
        list_audiences.add(4);
        list_audiences.add(5);
        list_audiences.add(6);
        list_audiences.add(7);
        list_audiences.add(8);
    }


    public DenseMatrix<Real> read(){

        try{
            Class.forName(Config.DB_DRIVER);
            Connection connection=DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASSWORD);

            String query="select * from widget_app";
            ResultSet resultSet=connection.createStatement().executeQuery(query);

            List<DenseVector<Real>> listDenseVector=new ArrayList<DenseVector<Real>>();

            int dimension=0;
            while (resultSet.next()){
                int index_columns=0;
                List<Real> list=new ArrayList<Real>();

                // Add to IdList
                idList.add(resultSet.getString(CODE));

                // Get audiences;
                list=NumberUtils.addZero(list, list_audiences.size());
                String audiences=resultSet.getString(APP_AUDIENCES);
                if (audiences==null){
                    for (int i=0; i<NUMBER_FIELD_AUDIENCES; i++){
                        list.set(index_columns + i, Real.ONE.divide(NUMBER_FIELD_AUDIENCES));
                    }
                } else {
                    audiences=audiences.replace(","," ").trim();

                    if (audiences.isEmpty()){
                        for (int i=0; i<NUMBER_FIELD_AUDIENCES; i++){
                            list.set(index_columns + i, Real.ONE.divide(NUMBER_FIELD_AUDIENCES));
                        }
                    } else {
                        String[] listAudiences=audiences.split(" ");
                        Real value=Real.ONE.divide(listAudiences.length);

                        for (String temp:listAudiences){
                            int position=Integer.parseInt(temp);
                            list.set(position + index_columns - 1, value);
                        }
                    }
                }
                index_columns=index_columns+NUMBER_FIELD_AUDIENCES;

                // get category
                NumberUtils.addZero(list, list_category.size());
                String categories=resultSet.getString(APP_CATEGORY);
                if (categories==null){
                    for (int i=0; i<list_category.size(); i++){
                        list.set(index_columns + i, Real.ONE.divide(list_category.size()));
                    }
                } else {
                    categories=categories.replace(",", " ").trim();
                    if (categories.isEmpty()){
                        for (int i=0; i<list_category.size(); i++){
                            list.set(index_columns + i, Real.ONE.divide(list_category.size()));
                        }
                    } else {
                        String[] app_categories=categories.split(" ");
                        Real value=Real.ONE.divide(app_categories.length);
                        for (String temp:app_categories){
                            int position=list_category.indexOf(Integer.parseInt(temp));
                            list.set(position + index_columns, value);
                        }
                    }
                }
                index_columns=index_columns+list_category.size();

                //get Status
                NumberUtils.addZero(list, 1);
                int status=resultSet.getInt(APP_STATUS);
                list.set(index_columns, Real.valueOf(MAX_APP_STATUS - status).divide(MAX_APP_STATUS));
                index_columns++;

                //get App gender
                NumberUtils.addZero(list, 1);
                String gender=resultSet.getString(APP_GENDER);
                if (gender==null){
                    list.set(index_columns, Real.valueOf(1.5));
                } else {
                    gender=gender.replace(","," ").trim();
                    if (gender.isEmpty()){
                        list.set(index_columns, Real.valueOf(0.25));
                    } else {
                        String[] app_gender=gender.split(" ");
                        if (app_gender.length==2){
                            list.set(index_columns, Real.valueOf(0.25));
                        } else {
                            Real value=Real.valueOf(MAX_APP_GENDER-Integer.parseInt(app_gender[0])).divide(MAX_APP_GENDER);
                            list.set(index_columns, value);
                        }
                    }
                }
                index_columns++;

                //get App Smart Status
                NumberUtils.addZero(list, 1);
                int appSmartStatus=resultSet.getInt(APP_SMART_STATUS);
                list.set(index_columns, Real.valueOf(appSmartStatus));
                index_columns++;

                // Get App rate;
                NumberUtils.addZero(list, 1);
                Integer rate=resultSet.getInt(APP_RATE);
                if (rate==null){
                    rate=0;
                }
                list.set(index_columns, Real.valueOf(APP_MAX_RATE - rate).divide(APP_MAX_RATE));
                index_columns++;

                // Get App Download
                NumberUtils.addZero(list, 1);
                Integer download=resultSet.getInt(APP_DOWNLOAD);
                if (download==null){
                    download=0;
                }
                list.set(index_columns, Real.valueOf(APP_MAX_DOWNLOAD - download).divide(APP_MAX_DOWNLOAD));
                index_columns++;

                //get App Like
                NumberUtils.addZero(list, 1);
                Integer like=resultSet.getInt(APP_LIKE);
                if (like==null){
                    like=0;
                }
                list.set(index_columns, Real.valueOf(APP_MAX_LIKE - like).divide(APP_MAX_LIKE));
                index_columns++;

                //
                NumberUtils.addZero(list, 1);
                dimension=index_columns;
                DenseVector<Real> vector=DenseVector.valueOf(list);
                listDenseVector.add(vector);
            }

            DenseMatrix<Real> matrix=DenseMatrix.valueOf(listDenseVector);
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
    public static void main(String args[]){
        AppReader reader=new AppReader();
        DenseMatrix<Real> matrix = reader.read();

        NumberUtils.printList("application_list.txt", reader.idList);
    }
}
