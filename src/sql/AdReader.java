package sql;

import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.NumberUtils;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdReader {

    // adv_id
    public static final String ID = "id";

    // adv_area: 3 attributes: {1, 4, 7}
    public static final  String ADV_AREA = "adv_area";

    public static final String NAME="name";

    // adv_category: many attributes
    public static final String ADV_CATEGORY = "adv_category";

    public static long CATEGORY_COEFF=5;
    public static long ADV_TYPE_COEFF=3;
    public static long GENDER_TYPE_COEFF=2;
    public static long ADV_AUDIENCE_COEFF=5;
    public static long AD_SCREEN_SIZE_COEFF;
    public static long AD_DEVICE_COEFF=1;
    public static long AD_AREA_COEFF=1;


    // device
    public static final  String ADV_DEVICE = "device";


    // adv_type: 5 attributes
    public static final  String ADV_TYPE = "adv_type";



    public static final  int MAX_ADV_TYPE = 5;

    // gender: 2 attributes;
    public static final  String GENDER = "gender";



    // adv_size: int
    public static final  String ADV_SIZE = "adv_size";
    public static final  int ADV_SIZE_MAX = 69;

    // Doi tuong quang cao muon nham den
    // Gia tri tu 1 -> 8
    // Gia tru 7, 8 la nam-nu, co the bo qua
    public static final  String ADV_AUDIENCE = "adv_audience";



    // screensize
    public static final  String SCREEN_SIZE = "screensize";


    public static final  int MAX_SCREEN_SIZE = 3;

    //index => adv_id
    public List<Integer> idList;


    private static Integer[] arrayArea={
            1, 4, 5
    };

    private static Integer[] arrayGender={
            0, 1
    };

    private static Integer[] arrayAudiences={
            1, 2, 3, 4, 5, 6, 7, 8
    };

    private static Integer[] arrayDevice={
            1, 2
    };

    private static List<Integer> listArea=Arrays.asList(arrayArea);
    private static List<Integer> listCategory = CategoryReader.getListCategory();
    private static List<Integer> listDevice=Arrays.asList(arrayDevice);
    private static List<Integer> listGender=Arrays.asList(arrayGender);
    private static List<Integer> listAudiences=Arrays.asList(arrayAudiences);

    public AdReader() {
        readConfiguration();
    }

    private void readConfiguration(){
        try{
            File xmlConfiguration=new File("configuration.xml");

            DocumentBuilderFactory dbFactory=DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder=dbFactory.newDocumentBuilder();

            Document doc=dBuilder.parse(xmlConfiguration);

            doc.getDocumentElement().normalize();

            // Read Advertisement

            Element advertisement=(Element) doc.getElementsByTagName("advertisement").item(0);


            Element coefficient=(Element) advertisement.getElementsByTagName("coefficient").item(0);

            CATEGORY_COEFF=Integer.parseInt(
                    coefficient.getElementsByTagName("category").item(0).getTextContent().trim()
            );


            ADV_TYPE_COEFF=Integer.parseInt(
                    coefficient.getElementsByTagName("type").item(0).getTextContent().trim()
            );


            ADV_AUDIENCE_COEFF=Integer.parseInt(
                    coefficient.getElementsByTagName("audiences").item(0).getTextContent().trim()
            );

            GENDER_TYPE_COEFF=Integer.parseInt(
                    coefficient.getElementsByTagName("gender").item(0).getTextContent().trim()
            );

            AD_AREA_COEFF=Integer.parseInt(
                    coefficient.getElementsByTagName("area").item(0).getTextContent().trim()
            );

            AD_SCREEN_SIZE_COEFF=Integer.parseInt(
                    coefficient.getElementsByTagName("screensize").item(0).getTextContent().trim()
            );
        } catch (Exception e){
            e.printStackTrace();
        }
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


                // analyze area
                String areaRaw=resultSet.getString(ADV_AREA);
                List<Real> listArea=analyzeArea(areaRaw, AD_AREA_COEFF);
                NumberUtils.addToList(listArea, list);

                indexColumns = indexColumns + listArea.size();


                // analyze categories
                String categoryRaw=resultSet.getString(ADV_CATEGORY);
                List<Real> listCategory=analyzeCategories(categoryRaw, CATEGORY_COEFF);
                NumberUtils.addToList(listCategory, list);
                indexColumns = indexColumns + listCategory.size();

                // Device
                String deviceRaw=resultSet.getString(ADV_DEVICE);
                List<Real> listDevice=analyzeDevice(deviceRaw, AD_DEVICE_COEFF);
                NumberUtils.addToList(listDevice, list);
                indexColumns = indexColumns + listDevice.size();

                // ADV_TYPE
                list = NumberUtils.addZero(list, 1);
                int typeRaw = resultSet.getInt(ADV_TYPE);
                list.set(indexColumns, Real.valueOf(MAX_ADV_TYPE - typeRaw).divide(Real.valueOf(MAX_ADV_TYPE)).times(ADV_TYPE_COEFF));
                indexColumns = indexColumns + 1;

                // GENDER
                String genderRaw=resultSet.getString(GENDER);
                List<Real> listGender=analyzeGender(genderRaw, GENDER_TYPE_COEFF);
                NumberUtils.addToList(listGender, list);
                indexColumns = indexColumns + 1;

                // ADV_SIZE
                list = NumberUtils.addZero(list, 1);
                int sizeRaw = resultSet.getInt(ADV_SIZE);
                list.set(indexColumns, Real.valueOf(ADV_SIZE_MAX - sizeRaw).divide(Real.valueOf(ADV_SIZE_MAX)));
                indexColumns = indexColumns + 1;

                // ADV_AUDIENCES

                String audienceRaw=resultSet.getString(ADV_AUDIENCE);
                List<Real> listAudiences=analyzeAudiences(audienceRaw, ADV_AUDIENCE_COEFF);
                NumberUtils.addToList(listAudiences, list);
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

    public static List<Real> analyzeArea(String areaRaw, long coeff){
        List<Real> list=new ArrayList<Real>();
        list = NumberUtils.addZero(list, 3);

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
            list.set(position, areaValue.times(coeff));
        }

        return list;
    }

    public static List<Real> analyzeCategories(String categoryRaw, long coeff){
        List<Real> list=new ArrayList<Real>();
        list = NumberUtils.addZero(list, listCategory.size());

        if (categoryRaw.isEmpty()) {
            for (int i = 0; i < listCategory.size(); i++) {
                Real value=Real.ONE.divide(Real.valueOf(listCategory.size())).times(coeff);
                list.set(i, value);
            }
        } else {
            String category[] = categoryRaw.split(" ");
            try {
                for (String value : category) {
                    int position = listCategory.indexOf(Integer.parseInt(value));
                    list.set(position, Real.ONE.divide(Real.valueOf(category.length)).times(coeff));
                }
            } catch (NumberFormatException e) {
                for (int i = 0; i < listCategory.size(); i++) {
                    list.set(i, Real.ONE.divide(Real.valueOf(listCategory.size())).times(coeff));
                }
            }
        }

        return list;
    }

    public static List<Real> analyzeDevice(String deviceRaw, long coeff){
        List<Real> list=new ArrayList<Real>();
        list = NumberUtils.addZero(list, listDevice.size());
        if (deviceRaw.isEmpty()) {
            list.set(0, Real.valueOf(0.5).times(coeff));
            list.set(1, Real.valueOf(0.5).times(coeff));
        } else {
            deviceRaw=deviceRaw.replace(","," ");
            String[] device = deviceRaw.split(" ");
            if (device.length == 2) {
                list.set(0, Real.valueOf(0.5).times(coeff));
                list.set(1, Real.valueOf(0.5).times(coeff));
            } else if (device.length == 1) {
                int position = listDevice.indexOf(Integer.parseInt(device[0]));
                list.set(position, Real.valueOf(coeff));
            }
        }

        return list;
    }

    public static List<Real> analyzeAudiences(String audiencesRaw, long coeff){
        List<Real> list=new ArrayList<Real>();
        list = NumberUtils.addZero(list, listAudiences.size());
        if (audiencesRaw == null) {
            // if null => all attribute is equal =1/6
            for (int i = 0; i < listAudiences.size(); i++) {
                list.set(i, Real.ONE.divide(listAudiences.size()).times(coeff));
            }
        } else if (audiencesRaw.isEmpty()) {
            // if empty => all attribute is equal =1/6
            for (int i = 0; i < listAudiences.size(); i++) {
                list.set(i, Real.ONE.divide(listAudiences.size()).times(coeff));
            }
        } else {

            audiencesRaw = audiencesRaw.replaceAll(",", " ").trim();
            String[] audience = audiencesRaw.split(" ");

            Real value = Real.ONE.divide(audience.length);


            for (String au : audience) {

                try {

                    int temp = Integer.parseInt(au);
                    list.set(temp - 1, value.times(coeff));

                } catch (NumberFormatException e) {
                    System.out.println (au);
                    e.printStackTrace();
                }

            }
        }

        return list;
    }

    public static List<Real> analyzeGender(String genderRaw, long coeff){
        List<Real> list=new ArrayList<Real>();
        list = NumberUtils.addZero(list, 1);
        if (genderRaw == null) {
            list.set(0, Real.valueOf(0.5));
        } else if (genderRaw.isEmpty()) {
            list.set(0, Real.valueOf(0.5));
        } else {
            genderRaw = genderRaw.replace(",", " ").trim();
            String[] gender = genderRaw.split(" ");
            if (gender.length == 2) {
                list.set(0, Real.valueOf(0.5));
            } else if (gender.length == 1) {
                int position = listGender.indexOf(Integer.parseInt(gender[0]));
                list.set(0, Real.valueOf(position));
            }
        }
        list.set(0, list.get(0).times(coeff));

        return list;
    }


    public static void main(String args[]){
        System.out.println (new AdReader().read().toString());
    }
}

