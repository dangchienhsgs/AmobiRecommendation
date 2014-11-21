package result;

import org.jscience.mathematics.number.Real;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import sql.AdReader;
import sql.AppReader;
import sql.Config;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



public class PrintCluster {
    public static long CATEGORY_COEFF=5;
    public static long ADV_TYPE_COEFF=3;
    public static long GENDER_TYPE_COEFF=2;
    public static long ADV_AUDIENCE_COEFF=5;
    public static long AD_SCREEN_SIZE_COEFF;
    public static long AD_DEVICE_COEFF=1;
    public static long AD_AREA_COEFF=1;



    public static long APP_DEVICE_COEFF=1;
    public static long APP_CATEGORY_COEFF=1;
    public static long APP_AUDIENCES_COEFF=1;
    public static long APP_GENDER_COEFF=1;
    public static long APP_SMART_STATUS_COEFF=1;
    public static long APP_RATE_COEFF=1;
    public static long APP_DOWNLOAD_COEFF=1;
    public static long APP_LIKE_COEFF=1;


    public static final String GAP=",";

    public List<Integer> getIdList(String input){
        try{
            FileInputStream fileInputStream=new FileInputStream(input);
            Scanner scanner=new Scanner(fileInputStream);

            int count=0;
            List<Integer> list=new ArrayList<Integer>();
            while (scanner.hasNextLine()){
                String line=scanner.nextLine();
                String row[]=line.trim().split(" ");
                list.add(Integer.parseInt(row[1]));
            }
            return list;
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    private void readConfiguration(){
        try{
            File xmlConfiguration=new File("configuration.xml");

            DocumentBuilderFactory dbFactory=DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder=dbFactory.newDocumentBuilder();

            Document doc=dBuilder.parse(xmlConfiguration);

            doc.getDocumentElement().normalize();

            // Read common configuration

            Node conAlgorithm=doc.getElementsByTagName("algorithm").item(0);

            Element element=(Element) conAlgorithm;

            int kTimes=Integer.parseInt(
                    element.getElementsByTagName("kmeans-times").item(0).getTextContent().trim()
            );

            int amobiTimes=Integer.parseInt(
                    element.getElementsByTagName("amobi-times").item(0).getTextContent().trim()
            );

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

            // Read application

            Element application = (Element) doc.getElementsByTagName("application").item(0);

            APP_CATEGORY_COEFF = Long.parseLong(
                    application.getElementsByTagName("category").item(0).getTextContent().trim()
            );

            APP_AUDIENCES_COEFF=Long.parseLong(
                    application.getElementsByTagName("audiences").item(0).getTextContent().trim()
            );

            APP_DOWNLOAD_COEFF=Long.parseLong(
                    application.getElementsByTagName("download").item(0).getTextContent().trim()
            );
            APP_GENDER_COEFF=Long.parseLong(
                    application.getElementsByTagName("gender").item(0).getTextContent().trim()
            );
            APP_LIKE_COEFF=Long.parseLong(
                    application.getElementsByTagName("like").item(0).getTextContent().trim()
            );

            APP_RATE_COEFF=Long.parseLong(
                    application.getElementsByTagName("rate").item(0).getTextContent().trim()
            );
            APP_SMART_STATUS_COEFF=Long.parseLong(
                    application.getElementsByTagName("smartstatus").item(0).getTextContent().trim()
            );

            APP_DEVICE_COEFF=Long.parseLong(
                    application.getElementsByTagName("device").item(0).getTextContent().trim()
            );

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public List<String> getCodeList(String input){
        try{
            FileInputStream fileInputStream=new FileInputStream(input);
            Scanner scanner=new Scanner(fileInputStream);

            int count=0;
            List<String> list=new ArrayList<String>();
            while (scanner.hasNextLine()){
                String line=scanner.nextLine();
                String row[]=line.split(" ");
                list.add(row[1].trim());
            }
            return list;
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }


    public List<Integer> readClusterFile(String clusterFile){
        try{
            FileInputStream fileInputStream=new FileInputStream(clusterFile);
            Scanner scanner=new Scanner(fileInputStream);

            int count=0;

            List<Integer> list=new ArrayList<Integer>();
            while (scanner.hasNextLine()){
                String line=scanner.nextLine().trim();
                line=line.replace(","," ");
                String row[]=line.trim().split("  ");
                //System.out.println (row.length);
                int cluster=0;
                for (int i=0; i<row.length; i++){
                    if (Float.parseFloat(row[i])==1){
                        cluster=i;
                    }
                }
                list.add(cluster);
            }
            return list;
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }


    public void printAppCluster(String clusterFile, String indexFiles,  String output, int K) {
        try {
            Class.forName(Config.DB_DRIVER);
            Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASSWORD);




            List<ArrayList<String>> result=new ArrayList<ArrayList<String>>();
            for (int i=0; i<K; i++){
                result.add(new ArrayList<String>());
            }

            // init idList
            List<String> codeList=getCodeList(indexFiles);
            List<Integer> clusterList=readClusterFile(clusterFile);

            for (int i=0; i<clusterList.size(); i++){
                String widget_code=codeList.get(i);
                int cluster=clusterList.get(i);

                String sql = "SELECT * FROM widget_app WHERE code='"+widget_code.trim()+"'";
                System.out.println (sql);
                ResultSet resultSet = connection.createStatement().executeQuery(sql);

                String detail="";
                while (resultSet.next()){

                    List<Real> listAudiences=AppReader.analyzeAudiences(resultSet.getString(AppReader.APP_AUDIENCES), APP_AUDIENCES_COEFF);
                    List<Real> listCategories=AppReader.analyzeCategories(resultSet.getString(AppReader.APP_CATEGORY), APP_CATEGORY_COEFF);
                    List<Real> listGender=AppReader.analyzeGender(resultSet.getString(AppReader.APP_GENDER), APP_GENDER_COEFF);

                    detail=resultSet.getString(AppReader.NAME)+GAP;

                    for (int j=0; j<listAudiences.size(); j++){
                        detail=detail+listAudiences.get(j).doubleValue()+GAP;
                    }

                    for (int j=0; j<listCategories.size(); j++){
                        detail=detail+listCategories.get(j).doubleValue()+GAP;
                    }
                    for (int j=0; j<listGender.size(); j++){
                        detail=detail+listGender.get(j).doubleValue()+GAP;
                    }

                    detail=detail+resultSet.getString(AppReader.CODE) +GAP
                            +resultSet.getString(AppReader.APP_AUDIENCES)+GAP
                            +resultSet.getString(AppReader.APP_DOWNLOAD)+GAP
                            +resultSet.getString(AppReader.APP_GENDER)+GAP
                            +resultSet.getString(AppReader.APP_STATUS)+GAP
                            +resultSet.getString(AppReader.APP_DOWNLOAD)+GAP
                            +resultSet.getString(AppReader.APP_LIKE);
                }
                System.out.println("Cluster: " + cluster);
                result.get(cluster).add(detail);
            }

            connection.close();


            // Print name to file

            try{
                File file = new File(output);

                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fileWriter = new FileWriter(file);

                for (int i=0; i<result.size(); i++){
                    fileWriter.write("Cluster "+i+": \n");
                    for (int j=0; j<result.get(i).size(); j++){
                        fileWriter.write(result.get(i).get(j)+"\n");
                    }
                    fileWriter.write("\n");
                }

                fileWriter.close();
            } catch (IOException e){
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void printAdCluster(String clusterFile, String indexFiles,  String output, int K) {
        try {
            Class.forName(Config.DB_DRIVER);
            Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASSWORD);




            List<ArrayList<String>> result=new ArrayList<ArrayList<String>>();
            for (int i=0; i<K; i++){
                result.add(new ArrayList<String>());
            }

            // init idList
            List<Integer> IdList=getIdList(indexFiles);
            List<Integer> clusterList=readClusterFile(clusterFile);

            for (int i=0; i<clusterList.size(); i++){
                int id=IdList.get(i);
                int cluster=clusterList.get(i);

                String sql = "SELECT * FROM advertistment WHERE id='"+id+"'";
                System.out.println (sql);
                ResultSet resultSet = connection.createStatement().executeQuery(sql);

                String detail="";
                while (resultSet.next()){

                    List<Real> category=AdReader.analyzeCategories(resultSet.getString(AdReader.ADV_CATEGORY), CATEGORY_COEFF);
                    List<Real> audiences=AdReader.analyzeAudiences(resultSet.getString(AdReader.ADV_AUDIENCE), CATEGORY_COEFF);
                    List<Real> area=AdReader.analyzeArea(resultSet.getString(AdReader.ADV_AREA), CATEGORY_COEFF);

                    System.out.println ("Category"+resultSet.getString(AdReader.ADV_CATEGORY));
                    System.out.println (category.toString());

                    System.out.println ("Audiences"+resultSet.getString(AdReader.ADV_AUDIENCE));
                    System.out.println (audiences.toString());

                    System.out.println ("Area"+resultSet.getString(AdReader.ADV_AREA));
                    System.out.println (area.toString());

                    detail=resultSet.getString(AdReader.NAME)+GAP+resultSet.getString(AdReader.ID)+GAP;

                    for (int j=0; j<category.size(); j++){
                        detail=detail+category.get(j).doubleValue()+GAP;
                    }

                    for (int j=0; j<audiences.size(); j++){
                        detail=detail+audiences.get(j).doubleValue()+GAP;
                    }

                    for (int j=0; j<area.size(); j++){
                        detail=detail+area.get(j).doubleValue()+GAP;
                    }

                    detail=detail+resultSet.getString(AdReader.ADV_SIZE)+GAP
                            +resultSet.getString(AdReader.ADV_TYPE)+GAP
                            +resultSet.getString(AdReader.GENDER)+GAP
                            +resultSet.getString(AdReader.SCREEN_SIZE) + GAP
                            +resultSet.getString(AdReader.ADV_DEVICE);
                }
                System.out.println("Cluster: " + cluster);
                result.get(cluster).add(detail);
            }

            connection.close();


            // Print name to file

            try{
                File file = new File(output);

                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fileWriter = new FileWriter(file);

                for (int i=0; i<result.size(); i++){
                    fileWriter.write("Cluster "+i+": \n");
                    for (int j=0; j<result.get(i).size(); j++){
                        fileWriter.write(result.get(i).get(j)+"\n");
                    }
                    fileWriter.write("\n");
                }

                fileWriter.close();
            } catch (IOException e){
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]){
                int i=5; int j=6;
                System.out.println ("Is extracting "+i+" "+j);
                String name="GU"+i+"_"+j+".txt";
                String id="APP_ID"+i+"_"+j+".txt";
                String out="APP_CLUSTER"+i+j+".csv";
                new PrintCluster().printAppCluster(
                        name, id, out, j
                );

    }
}
