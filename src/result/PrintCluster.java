package result;

import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;
import sql.AdReader;
import sql.AppReader;
import sql.CategoryReader;
import sql.Config;
import utils.NumberUtils;

import java.io.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PrintCluster {

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

                String name="";
                while (resultSet.next()){
                    name=resultSet.getString(AppReader.NAME)+GAP
                            +resultSet.getString(AppReader.CODE) +GAP
                            +resultSet.getString(AppReader.APP_CATEGORY)+GAP
                            +resultSet.getString(AppReader.APP_AUDIENCES)+GAP
                            +resultSet.getString(AppReader.APP_DOWNLOAD)+GAP
                            +resultSet.getString(AppReader.APP_GENDER)+GAP
                            +resultSet.getString(AppReader.APP_STATUS)+GAP
                            +resultSet.getString(AppReader.APP_DOWNLOAD)+GAP
                            +resultSet.getString(AppReader.APP_LIKE);
                }
                System.out.println ("Cluster: "+cluster);
                result.get(cluster).add(name);
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

                String name="";
                while (resultSet.next()){
                    name=resultSet.getString(AdReader.NAME)+GAP
                            +resultSet.getString(AdReader.ADV_CATEGORY)+GAP
                            +resultSet.getString(AdReader.ADV_AUDIENCE)+GAP
                            +resultSet.getString(AdReader.ADV_AREA)+GAP
                            +resultSet.getString(AdReader.ADV_SIZE)+GAP
                            +resultSet.getString(AdReader.ADV_TYPE)+GAP
                            +resultSet.getString(AdReader.GENDER)+GAP
                            +resultSet.getString(AdReader.SCREEN_SIZE)+GAP
                            +resultSet.getString(AdReader.DEVICE);
                }
                System.out.println ("Cluster: "+cluster);
                result.get(cluster).add(name);
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
        //System.out.println (new PrintCluster().readClusterFile("GA65.csv"));
        /*for (int i=4; i<7; i++){
            for (int j=4; j<7; j++){*/
                int i=6; int j=5;
                System.out.println ("Is extracting "+i+" "+j);
                String name="GU"+i+"_"+j+".txt";
                String id="APP_ID"+i+"_"+j+".txt";
                String out="APP_CLUSTER"+i+j+".csv";
                new PrintCluster().printAppCluster(
                        name, id, out, i
                );
            /*}
        }*/
    }
}
