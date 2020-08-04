/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package femi;

import java.io.File;
import java.util.StringTokenizer;

/**
 *
 * @author grahman
 * 26/05/2011
 */
public class AverageImputation
{

    public void averageImputationInt(String dFile, int noOfAttr, int []attrType, int sPos)
    {
        double []mu;
        mu=computeMean(dFile,noOfAttr,attrType,sPos);
        imputeByMean(dFile,noOfAttr,attrType,sPos,mu);
        
    }
    public void averageImputation(String dFile, int noOfAttr, int []attrType, int sPos)
    {
        double []mu;
        mu=computeMean(dFile,noOfAttr,attrType,sPos);
        ImputeByMean(dFile,noOfAttr,attrType,sPos,mu);

    }
    private double []computeMean(String dFile, int noOfAttr, int []attrType, int sPos)
    {
        double []mu=new double[noOfAttr];
        double []sum=new double[noOfAttr];
        double []total=new double[noOfAttr];
        for(int c=sPos;c<noOfAttr;c++)
        {
            mu[c]=0.0;sum[c]=0.0;total[c]=0.0;
        }
        String val;
        FileManager fileManager=new FileManager();
        StringTokenizer tokenizer;
        String [] dataFile = fileManager.readFileAsArray(new File(dFile));
        try{
            int noOfRecords=dataFile.length;
            for(int i=0; i<noOfRecords;i++)
            {
                tokenizer = new StringTokenizer(dataFile[i], " ,\t\n\r\f");
                for(int c=sPos;c<noOfAttr;c++)
                {
                    val=tokenizer.nextToken();
                    if(attrType[c]==1&&!val.equals("")&&!val.equals("?"))
                    {
                      sum[c]+=Double.parseDouble(val);
                      total[c]++;

                    }
                }
            }
        }
        catch(Exception e)
        {
            //do nothing
        }
        for(int c=sPos;c<noOfAttr;c++)
            {
                if(attrType[c]==1&&total[c]>0)
                {
                  mu[c]=sum[c]/total[c];
                 }
            }
        return mu;
    }
    private void ImputeByMean(String dFile, int noOfAttr, int []attrType, int sPos, double []mu)
    {
        String val;
        FileManager fileManager=new FileManager();
        File outF=new File(dFile);
        StringTokenizer tokenizer;
        String [] dataFile = fileManager.readFileAsArray(new File(dFile));
       try{
        int noOfRecords=dataFile.length;
        for(int i=0; i<noOfRecords;i++)
        {
            tokenizer = new StringTokenizer(dataFile[i], " ,\t\n\r\f");
            String rec="";
            for(int c=sPos;c<noOfAttr;c++)
            {
                val=tokenizer.nextToken();
                if(attrType[c]==1&&(val.equals("")||val.equals("?")))
                    val="0";
                if(c==0)
                    rec=val;
                else
                    rec=rec+", "+val;
            }
            if(i<=noOfRecords-1)
                rec=rec+"\n";
            if(i==0)
                fileManager.writeToFile(outF, rec);
            else
                fileManager.appendToFile(outF, rec);
        }
        }
       catch(Exception e)
        {
            //do nothing
        }

    }
    private void imputeByMean(String dFile, int noOfAttr, int []attrType, int sPos, double []mu)
    {
        String val;
        FileManager fileManager=new FileManager();
        File outF=new File(dFile);
        StringTokenizer tokenizer;
        String [] dataFile = fileManager.readFileAsArray(new File(dFile));
       try{
        int noOfRecords=dataFile.length;
        for(int i=0; i<noOfRecords;i++)
        {
            tokenizer = new StringTokenizer(dataFile[i], " ,\t\n\r\f");
            String rec="";
            for(int c=sPos;c<noOfAttr;c++)
            {
                val=tokenizer.nextToken();
                if(attrType[c]==1&&(val.equals("")||val.equals("?")))
                    val=""+mu[c];
                if(c==0)
                    rec=val;
                else
                    rec=rec+", "+val;
            }
            if(i<=noOfRecords-1)
                rec=rec+"\n";
            if(i==0)
                fileManager.writeToFile(outF, rec);
            else
                fileManager.appendToFile(outF, rec);
        }
        }
       catch(Exception e)
        {
            //do nothing
        }
        
    }
}
