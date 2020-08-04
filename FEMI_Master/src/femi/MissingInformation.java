/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package femi;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
/**
 *
 * @author Geaur Rahman
 */
public class MissingInformation {
    /** used for managing reading and writing to file */
    private String [] attrNames;
    private String [] attrType;
    private int [] missingAttrs;
    private int noOfAttrs;
    private int noOfNumericalAttrs;
    private int noOfRecords;
    private int totalMissingValues;
    private int noOfMissingAttrs; // total no. of missing attributes of the data file
    String str;
    int temp;

    //this method will check whether missing value found in the given dataset (dFile)
    //
    public int findMissingAttributeInfo(String dFile, String attrFile)
        {   int noa,ismiss;
//            double missingPercent;
            String rec;
            FileManager fileManager_g = new FileManager();
            /** read the two files and store as arrays */
            String [] nameFile_g = fileManager_g.readFileAsArray(new File(attrFile),2);
            String [] dataFile_g = fileManager_g.readFileAsArray(new File(dFile));

            StringTokenizer tokens = new StringTokenizer(nameFile_g[0], " ,\n\t"); //remove spaces, commas, tabs and newlines
            noa = tokens.countTokens();// no. of attributes of the datafile
            noOfAttrs=noa;
            noOfNumericalAttrs=0;
            attrType=new String[noOfAttrs];
            for(int i=0; i<noOfAttrs;i++)
            {
                rec=tokens.nextToken();
                if(rec.equals("1"))
                {
                    attrType[i] = "n";
                    noOfNumericalAttrs++;
                }
                else 
                    attrType[i]="c";
                        
            }
            tokens = new StringTokenizer(nameFile_g[1], " ,\n\t"); //remove spaces, commas, tabs and newlines
            attrNames=new String[noOfAttrs];
            for(int i=0; i<noOfAttrs;i++)
            {
                attrNames[i]=tokens.nextToken();

            }
            initilizeMissing(noa);
             try{
            noOfRecords=dataFile_g.length;
            for(int i=0; i<noOfRecords;i++)
            {
                StringTokenizer tokenizer = new StringTokenizer(dataFile_g[i], " ,\t\n\r\f");
                ismiss=findMissingAttr(tokenizer,noa);
                if(ismiss>0)
                {
                    totalMissingValues+=ismiss;
                    
                }
            }
            }
             catch(Exception e)
        {
            //do nothing
        }
//           missingPercent=(totalMissingValues*100.0)/(noOfRecords*noa);
//           if(totalMissingValues>0)
//           {
//           DecimalFormat df = new DecimalFormat("####0.000");
//           System.out.println("total missing found->"+totalMissingValues+" , Missing Percentage->"+df.format(missingPercent)+"%");
//            }
           return totalMissingValues;
        }

  public int getNoOfAttr()
    {
       return noOfAttrs;
    }
   public int getNoOfNumericalAttr()
    {
       return noOfNumericalAttrs;
    }
  
   public String [] getNamesOfAttr()
    {
        return attrNames;
    }
 
  public String [] getTypeOfAttr()
    {
        return attrType;
    }
  public void initilizeMissing(int noa)
    {
      missingAttrs=new int[noa];
      totalMissingValues=0;
      for(int i=0;i<noa;i++)
          missingAttrs[i]=0;
     }
   //This method set the attribute that value is missing in the data set.
   public void setMissingAttr(int cuIndex)
    {
       missingAttrs[cuIndex]=1;
     }
   //This method return the list of missing attributes found in the data set.
   public int [] getMissingAttr()
    {
       return missingAttrs;
    }
    //This method return the no of missing attributes found in the data set.
   public int getNoOfMissingAttr()
    {
       noOfMissingAttrs=0;
       for(int i=0;i<missingAttrs.length;i++)
           if(missingAttrs[i]==1)
               noOfMissingAttrs++;
       return noOfMissingAttrs;
    }
   //This method return the total no. of missing values found in the data set.
   public int getTotalMissingFound()
    {
       return totalMissingValues;
    }
   //This method return the total no. of records found in the original data set.
   public int getTotalNoOfRecords()
    {
       return noOfRecords;
    }
   //following method find missing values in a record
    public int findMissingAttr(StringTokenizer aRecord,int noa)
    {
        int msIndex=0;
        for(int i=0;i<noa;i++)

        {
            str = aRecord.nextToken(); //get attribute value
            if(str.equals("?")||str.equals(" "))  //if missing then
            {
                setMissingAttr(i);     //set the missing index
                msIndex++;     //count the no. of missing values found in a record
            }
         }
        return msIndex;   //return missing index
    }
}
