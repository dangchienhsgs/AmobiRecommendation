package sql;

import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;
import utils.NumberUtils;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class AdReader {
    // adv_id
    public static final String ID = "id";

    // adv_area: 3 attributes: {1, 4, 7}
    public static final  String ADV_AREA = "adv_area";

    public static final String NAME="name";

    // adv_category: many attributes
    public static final String ADV_CATEGORY = "adv_category";

    public static final long CATEGORY_COEFF=5;

    // device
    public static final  String DEVICE = "device";


    // adv_type: 5 attributes
    public static final  String ADV_TYPE = "adv_type";

    public static final long ADV_TYPE_COEFF=3;

    public static final  int MAX_ADV_TYPE = 5;

    // gender: 2 attributes;
    public static final  String GENDER = "gender";

    public static final long GENDER_TYPE_COEFF=2;

    // adv_size: int
    public static final  String ADV_SIZE = "adv_size";
    public static final  int ADV_SIZE_MAX = 69;

    // Doi tuong quang cao muon nham den
    // Gia tri tu 1 -> 8
    // Gia tru 7, 8 la nam-nu, co the bo qua
    public static final  String ADV_AUDIENCE = "adv_audience";

    public static final long ADV_AUDIENCE_COEFF=5;

    // screensize
    public static final  String SCREEN_SIZE = "screensize";


    public static final  int MAX_SCREEN_SIZE = 3;

    //index => adv_id
    public List<Integer> idList;

    static final int NUMBER_AREA = 3;
    private List<Integer> listArea;
    private List<Integer> listCategory;
    private List<Integer> listDevice;
    private List<Integer> listGender;
    private List<Integer> listAudiences;

    public AdReader() {
        initData();
    }

    private void initData(){
        // Prepare List gender
        listGender = new ArrayList<Integer>();
        listGender.add(0);
        listGender.add(1);
        // Prepare Advertisement Area list
        listArea = new ArrayList<Integer>();
        listArea.add(1);
        listArea.add(4);
        listArea.add(5);

        // Prepare Advertisement Category
        listCategory = CategoryReader.getListCategory();

        // Prepare advertisement device list
        listDevice = new ArrayList<Integer>();
        listDevice.add(1);
        listDevice.add(2);

        // Prepare audiences list;
        listAudiences = new ArrayList<Integer>();
        listAudiences.add(1);
        listAudiences.add(2);
        listAudiences.add(3);
        listAudiences.add(4);
        listAudiences.add(5);
        listAudiences.add(6);
        listAudiences.add(7);
        listAudiences.add(8);
    }

    public DenseMatrix<Real> read() {
        try {
            Class.forName(Config.DB_DRIVER);
            Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASSWORD);

            String sql = "SELECT * FROM advertistment";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);

            List<DenseVector<Real>> denseVectors = new ArrayList<DenseVector<Real>>();

            idList = new ArrayList<Integer>();

            int indexColumns = 0;
            while (resultSet.next()) {
                indexColumns = 0;
                idList.add(resultSet.getInt(ID));
                List<Real> list = new ArrayList<Real>();

                // adv area 3 thuoc tinh

                list = NumberUtils.addZero(list, 3);
                String areaRaw = resultSet.getString(ADV_AREA);
                if (areaRaw == null) {
                    areaRaw = "1 4 5";
                } else {
                    areaRaw = areaRaw.replace(",", " ").trim();
                }

                if (areaRaw.isEmpty()) {
                    areaRaw = "1 4 5";
                }

                String[] area = areaRaw.split(" ");
                Real areaValue = Real.ONE.divide(area.length);
                for (String temp : area) {
                    int position = listArea.indexOf(Integer.parseInt(temp));
                    list.set(indexColumns + position, areaValue);
                }
                indexColumns = indexColumns + listArea.size();


                // Category
                list = NumberUtils.addZero(list, listCategory.size());
                String categoryRaw = resultSet.getString(ADV_CATEGORY).replace(",", " ").trim();

                if (categoryRaw.isEmpty()) {
                    for (int i = 0; i < listCategory.size(); i++) {
                        Real value=Real.ONE.divide(Real.valueOf(listCategory.size())).times(CATEGORY_COEFF);
                        list.set(indexColumns + i, value);
                    }
                } else {
                    String category[] = categoryRaw.split(" ");
                    try {
                        for (String value : category) {
                            int position = listCategory.indexOf(Integer.parseInt(value));
                            list.set(indexColumns + position, Real.ONE.divide(Real.valueOf(category.length)).times(CATEGORY_COEFF));
                        }
                    } catch (NumberFormatException e) {
                        for (int i = 0; i < listCategory.size(); i++) {
                            list.set(indexColumns + i, Real.ONE.divide(Real.valueOf(listCategory.size())).times(CATEGORY_COEFF));
                        }
                    }
                }
                indexColumns = indexColumns + listCategory.size();

                // Device
                list = NumberUtils.addZero(list, 1);
                String deviceRaw = resultSet.getString(DEVICE).replace(",", " ").trim();
                if (deviceRaw.isEmpty()) {
                    list.set(indexColumns, Real.valueOf(0.5));
                } else {
                    String[] device = deviceRaw.split(" ");
                    if (device.length == 2) {
                        list.set(indexColumns, Real.valueOf(0.5));
                    } else if (device.length == 1) {
                        int position = listDevice.indexOf(Integer.parseInt(device[0]));
                        list.set(indexColumns, Real.valueOf(position));
                    }
                }
                indexColumns = indexColumns + 1;


                // ADV_TYPE
                list = NumberUtils.addZero(list, 1);
                int typeRaw = resultSet.getInt(ADV_TYPE);
                list.set(indexColumns, Real.valueOf(MAX_ADV_TYPE - typeRaw).divide(Real.valueOf(MAX_ADV_TYPE)).times(ADV_TYPE_COEFF));
                indexColumns = indexColumns + 1;

                //GENDER
                list = NumberUtils.addZero(list, 1);
                String genderRaw = resultSet.getString(GENDER);
                if (genderRaw == null) {
                    list.set(indexColumns, Real.valueOf(0.5));
                } else if (genderRaw.isEmpty()) {
                    list.set(indexColumns, Real.valueOf(0.5));
                } else {
                    genderRaw = genderRaw.replace(",", " ").trim();
                    String[] gender = genderRaw.split(" ");
                    if (gender.length == 2) {
                        list.set(indexColumns, Real.valueOf(0.5));
                    } else if (gender.length == 1) {
                        int position = listGender.indexOf(Integer.parseInt(gender[0]));
                        list.set(indexColumns, Real.valueOf(position));
                    }
                }
                list.set(indexColumns, list.get(indexColumns).times(GENDER_TYPE_COEFF));
                indexColumns = indexColumns + 1;

                // ADV_SIZE
                list = NumberUtils.addZero(list, 1);
                int sizeRaw = resultSet.getInt(ADV_SIZE);
                list.set(indexColumns, Real.valueOf(ADV_SIZE_MAX - sizeRaw).divide(Real.valueOf(ADV_SIZE_MAX)));
                indexColumns = indexColumns + 1;

                // ADV_AUDIENCES
                list = NumberUtils.addZero(list, listAudiences.size());
                String audiencesRaw = resultSet.getString(ADV_AUDIENCE);
                if (audiencesRaw == null) {
                    // if null => all attribute is equal =1/6
                    for (int i = 0; i < listAudiences.size(); i++) {
                        list.set(indexColumns + i, Real.ONE.divide(listAudiences.size()).times(ADV_AUDIENCE_COEFF));
                    }
                } else if (audiencesRaw.isEmpty()) {
                    // if empty => all attribute is equal =1/6
                    for (int i = 0; i < listAudiences.size(); i++) {
                        list.set(indexColumns + i, Real.ONE.divide(listAudiences.size()).times(ADV_AUDIENCE_COEFF));
                    }
                } else {
                    audiencesRaw = audiencesRaw.replaceAll(",", " ").trim();
                    String[] audience = audiencesRaw.split(" ");
                    int value = 1 / audience.length;
                    // if not empty
                    for (String au : audience) {
                        // Khoi tao cho tat ca bang 0
                        for (int i = 0; i < listAudiences.size(); i++) {
                            list.set(indexColumns + i, Real.valueOf(0));
                        }

                        try {
                            int temp = Integer.parseInt(au);
                            list.set(temp + indexColumns - 1, Real.valueOf(value).times(ADV_AUDIENCE_COEFF));
                        } catch (NumberFormatException e) {
                            System.out.println (au);
                            e.printStackTrace();
                        }

                    }
                }
                indexColumns = indexColumns + listAudiences.size();

                // ADV_SCREEN_SIZE
                list = NumberUtils.addZero(list, 1);
                int screenSizeRaw = resultSet.getInt(SCREEN_SIZE);
                list.set(indexColumns, Real.valueOf(MAX_SCREEN_SIZE - screenSizeRaw).divide(MAX_SCREEN_SIZE));


                DenseVector<Real> vector = DenseVector.valueOf(list);
                denseVectors.add(vector);
            }

            DenseMatrix<Real> matrix = DenseMatrix.valueOf(denseVectors);
            connection.close();
            return matrix;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
