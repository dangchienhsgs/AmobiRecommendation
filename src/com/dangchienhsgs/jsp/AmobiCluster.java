package com.dangchienhsgs.jsp;

import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import sql.AdReader;
import sql.AppReader;
import sql.LinkConverter;
import sql.LinkReader;
import utils.KMeans;
import utils.MatrixUtilities;
import utils.NumberUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Arrays;

public class AmobiCluster {

    private AdReader adReader;
    private AppReader appReader;
    private LinkReader linkReader;

    private DenseMatrix<Real> adMatrix;
    private DenseMatrix<Real> appMatrix;
    private DenseMatrix<Real> adAppMatrix;
    private DenseMatrix<Real> appAdMatrix;

    private DenseMatrix<Real> AGU, AGUA;
    private DenseMatrix<Real> UGA, UGAU;

    private DenseMatrix<Real> GU, GA;

    private DenseMatrix<Real> GAGU;
    private DenseMatrix<Real> GUGA;

    private int numAdGroup, numAppGroup;


    private int kTimes = 100;
    private int amobiTimes=200;

    public AmobiCluster(int numAdGroup, int numAppGroup) {
        this.numAdGroup = numAdGroup;
        this.numAppGroup = numAppGroup;

        initData();

        initConfiguration();
    }

    public void initConfiguration() {
        try {
            File xmlConfiguration = new File("configuration.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(xmlConfiguration);

            doc.getDocumentElement().normalize();

            // Read common configuration

            Node conAlgorithm = doc.getElementsByTagName("algorithm").item(0);

            Element element = (Element) conAlgorithm;

            kTimes = Integer.parseInt(
                    element.getElementsByTagName("kmeans-times").item(0).getTextContent().trim()
            );


            amobiTimes = Integer.parseInt(
                    element.getElementsByTagName("amobi-times").item(0).getTextContent().trim()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initData(){
        adReader = new AdReader();
        appReader = new AppReader();

        System.out.println("Reading Advertisement Matrix.....");
        adMatrix = adReader.read();
        adMatrix = MatrixUtilities.normalizeMatrix(adMatrix);

        System.out.println("Reading Application Matrix.....");
        appMatrix = appReader.read();
        appMatrix = MatrixUtilities.normalizeMatrix(appMatrix);

        linkReader = new LinkReader(adReader.idList, appReader.idList, new LinkConverter().getLinkIDAdvIDMap());

        System.out.println("Reading Adv-App Relation Matrix...");
        adAppMatrix = linkReader.read();
        appAdMatrix = adAppMatrix.transpose();

        adAppMatrix = MatrixUtilities.normalizeMatrix(adAppMatrix);
        appAdMatrix = MatrixUtilities.normalizeMatrix(appAdMatrix);
    }

    public void executeICCA() {
        System.out.println("Cluster Adv Matrix to GA...");
        GA = new KMeans(numAdGroup, adMatrix).execute(this.kTimes);


        System.out.println("Cluster App Matrix to GU...");
        GU = new KMeans(numAppGroup, appMatrix).execute(this.kTimes);

        System.out.println("Progress Cluster AGUA and UGAU start....");
        for (int i = 0; i < amobiTimes; i++) {

            System.out.println("Cluster AGUA to update GA times " + i + "...");
            AGU = MatrixUtilities.normalizeMatrix(computeRelation(adMatrix, GU, appAdMatrix));
            System.out.println(AGU);
            AGUA = MatrixUtilities.normalizeMatrix(MatrixUtilities.combineMatrix(AGU, adMatrix));
            System.out.println(AGUA);
            GA = new KMeans(numAdGroup, AGUA).execute(this.kTimes);

            System.out.println("Cluster UGAU to update GU times " + i + "...");
            UGA = MatrixUtilities.normalizeMatrix(computeRelation(appMatrix, GA, adAppMatrix));
            System.out.println(UGA);
            UGAU = MatrixUtilities.normalizeMatrix(MatrixUtilities.combineMatrix(UGA, appMatrix));
            System.out.println(UGAU);
            GU = new KMeans(numAppGroup, UGAU).execute(this.kTimes);
        }

        MatrixUtilities.toFile("GA"+ numAdGroup +"_"+ numAppGroup +".txt", GA);
        MatrixUtilities.toFile("GU"+ numAdGroup +"_"+ numAppGroup +".txt", GU);
        MatrixUtilities.toFile("AGU"+ numAdGroup +"_"+ numAppGroup +".txt", AGU);
        MatrixUtilities.toFile("UGA"+ numAdGroup +"_"+ numAppGroup +".txt", UGA);

        GAGU = computeGAGU();
        GUGA = GAGU.transpose();

        Real divergenceAdv = MatrixUtilities.divergenceMatrix(GAGU);
        Real divergenceApp = MatrixUtilities.divergenceMatrix(GUGA);

        System.out.println(divergenceAdv + "   " + divergenceApp + "  " + (divergenceAdv.plus(divergenceApp)));
        NumberUtils.toFile("KQ"+new Integer(numAdGroup).toString()
                +new Integer(numAppGroup).toString()+".txt",divergenceAdv.toString()+" "
                +divergenceApp.toString()+" "
                +divergenceAdv.plus(divergenceApp).toString());

        NumberUtils.printList("ADV_ID"+ numAdGroup +"_"+ numAppGroup +".txt", adReader.idList);
        NumberUtils.printList("APP_ID"+ numAdGroup +"_"+ numAppGroup +".txt", appReader.idList);
    }

    public DenseMatrix<Real> computeRelation(DenseMatrix<Real> U, DenseMatrix<Real> GA, DenseMatrix<Real> link) {
        int numRows = U.getNumberOfRows();
        int numColumns = GA.getNumberOfColumns();

        Real w[][] = new Real[numRows][numColumns];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                // UGA[i, j]= tong tat ca cac L[k, i] sao cho GA[k][j]=1;

                Real value = Real.ZERO;
                for (int k = 0; k < GA.getNumberOfRows(); k++) {
                    if (GA.get(k, j).compareTo(Real.ONE) == 0) {
                        value = value.plus(link.get(k, i));
                    }
                }
                w[i][j] = value;
            }
        }
        return DenseMatrix.valueOf(w);
    }
    private DenseMatrix<Real> computeGAGU() {
        int numRows = numAdGroup;
        int numColumns = numAppGroup;

        Real w[][] = new Real[numRows][numColumns];
        for (int i = 0; i < numRows; i++) {
            Arrays.fill(w[i], Real.ZERO);
        }

        for (int i = 0; i < GA.getNumberOfRows(); i++) {
            for (int j = 0; j < GU.getNumberOfRows(); j++) {
                // xac dinh xem i thuoc nhom Ad nao
                int groupI = 0;
                for (int m = 0; m < GA.getNumberOfColumns(); m++) {
                    if (GA.get(i, m).compareTo(Real.ONE) == 0) {
                        groupI = m;
                    }
                }
                // xac dinh xem j thuoc nhom app nao
                int groupJ = 0;
                for (int m = 0; m < GU.getNumberOfColumns(); m++) {
                    if (GU.get(j, m).compareTo(Real.ONE) == 0) {
                        groupJ = m;
                    }
                }
                w[groupI][groupJ] = w[groupI][groupJ].plus(adAppMatrix.get(i, j));
            }
        }

        return DenseMatrix.valueOf(w);
    }

    public static void main(String args[]){
        new AmobiCluster(3, 5).executeICCA();
    }
}
