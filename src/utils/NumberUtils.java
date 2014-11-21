package utils;


import org.jscience.mathematics.number.Real;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NumberUtils {
    public double [] randomLine(int n){
        double[] result=new double[n];
        double sum=1;
        for (int i=0; i<n-1; i++){
            double temp=Math.random();
            if (temp>sum) {
                int x=(int) (temp/sum);
                temp=temp-x*sum;
            }
            sum=sum-temp;
            result[i]=temp;
        }
        result[n-1]=sum;
        return result;
    }
    public double [] random(int n){
        double[] result=new double[n];
        for (int i=0; i<n; i++){
            double temp=Math.random();
        }
        return result;
    }

    public static void addToList(List<Real> element, List<Real> host){
        for (Real str:element){
            host.add(str);
        }
    }

    public static List<Real> addZero(List<Real> list, int number){
        for (int i=0; i<number; i++){
            list.add(Real.ZERO);
        }
        return list;
    }
    public static void main(String args[]){
        List<Real> a=new ArrayList<Real>();
        NumberUtils.addZero(a, 3);

        System.out.println (a.get(2));
    }

    public static void printList(String fileName, List<?> list){
        try {
            File file = new File(fileName);

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file);
            for (int i=0; i<list.size(); i++){
                fileWriter.write(i+" "+list.get(i)+"\n");
            }
            fileWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void toFile (String filename, String data){
        try {
            File file = new File(filename);

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(data);
            fileWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
