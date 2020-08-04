/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package femi;
import java.io.*;
import java.util.*;

/**
 * FEMI (Fuzzy Clustering-based Missing Value Imputation Framework) for data preprocessing.
 * FEMI imputes numerical and categorical missing values by making an educated guess 
 * based on records that are similar to the record having a missing value. 
 * While identifying a group of similar records and making a guess based on the group, 
 * it applies a fuzzy clustering approach and our novel fuzzy expectation maximization algorithm.
 * 
 * <h2>Reference</h2>
 * 
 * Rahman M. G. and Islam M. Z. (2016): Missing Value Imputation using a Fuzzy Clustering based EM Approach, Knowledge and Information Systems, Vol. 46 (2), pp. 389 – 422. DOI: 10.1007/s10115-015-0822-y. </li> 
 *  
 * @author Md Geaur Rahman <gea.bau.edu.bd>
 */

public class FEMI {

/*
 * Global declaration
 */
    
 private final double mmm=1.3; //Fuzzification co-efficient
 private int kkk=3; //no. of clusters
 private final double epsilon=0.0001; //Termination criteria (or value)
 private int loopTerminator;
 private double [][]MD;//membership degree for records having no missing values
 private double [][]W;  //membership degree for records having missing values
 private double [][]wMD;  //membership degree for whole data set
 private double []tMD;  //total membership degree of each cluster
  //frequency of a centroid of an attribute for each cluster
 private double [][][]Frequency; 
 private String [][]dataset;//contains dataset
 private String [][]datasetWM;//contains dataset
 private int []domainsize;//contains domain size of each attribute
 private String [][] V; //contains centeriods
 //contains majority categorical value for each attribute and cluster
 private String [][]majorityCat; 
 private int []delta; //0->missing, 1 means NOT missing
 private int []RS;  //missing record id
 private int TotalMissing;  //total missing records
 private int []attrNtype;  //attribute type 1-> numerical,0->Categorical
 private String []attrType;  //attribute type N-> numerical,C->Categorical
 private int totalRecord;  //total records of the data file
 private int totalRecordWM;  //total records of the data file
 private int totalAttr;  //total attributes of the data file
 private int totalNumAttr;  //total attributes of the data file
 private int []listOfNumericalAttr;
 private String attrFile;  // attribute file of a data set
 private String dataFile;  // file name of a data set
 private String nameFile;  // name file of a data set
 private String []tempFileList;
 private int tempTotalFile;
    /**
     * read files and create k-clusters
     * @Param
     * attr- input attribute file
     * DF- input data file
     * DA-output files list, each file contains each cluster records
     */
 public void runFEMI(String attr, String DF, String OutF)
  {

     FileManager fileManager=new FileManager();
     initialize(attr,DF); 

     String clusterF=fileManager.changedFileName(DF, "_CT");
     tempFileList[tempTotalFile]=clusterF;tempTotalFile++;
     createClusters_GFCM(clusterF);

     calculateMembershipDegreeMR();

     combineMembershipDegree();
     printMDToFile(clusterF);

     findMajorityCatVal();
     ImputeByClustering(OutF);

     fileManager.removeListOfFiles(tempFileList,tempTotalFile);    
  }
 
 public void setClusterSize(int k)
 {
     this.kkk=k;
 }
 // this method is used to initialize variables

 private void initialize( String attr,String DF)
   {
        tMD=new double[kkk];
        tempFileList=new String[11];
        FileManager fileManager = new FileManager();
        attrFile=attr;
        dataFile=DF;
        //read attr file into an array
        String [] nFile = fileManager.readFileAsArray(new File(attrFile));
        //tokenize a record ==remove spaces, commas, tabs and newlines
        StringTokenizer ntoken = new StringTokenizer(nFile[0], " ,\n\t");
        totalAttr=ntoken.countTokens();
        domainsize=new int[totalAttr];
        attrNtype=new int[totalAttr];
        attrType=new String[totalAttr];
        totalNumAttr=0;
        //read attribute type from name file
        for(int i=0;i<totalAttr;i++)
        {
         String aty=ntoken.nextToken();
         attrNtype[i]=Integer.parseInt(aty);
         if (attrNtype[i]==1)
         {
             attrType[i]="n";
             totalNumAttr++;
            }
         else
             attrType[i]="c";
        }
        listOfNumericalAttr=new int[totalNumAttr];
        int j=0;
        for(int i=0;i<totalAttr;i++)
        {
            if (attrNtype[i]==1)
            {
                listOfNumericalAttr[j]=i;
                j++;
            }

        }
      //Normalize the data set
        String DF_Norm = fileManager.changedFileName(DF, "_Norm");
        Module mdl=new Module();
        mdl.normaliseFile(DF, DF_Norm,attrType,0,0.0,1.0);
        tempTotalFile=0;
        tempFileList[tempTotalFile]=DF_Norm;tempTotalFile++;

        fileToArray(DF_Norm);// read data file into array
//        System.out.println(TotalMissing);
        //remove missing values
        String DC = fileManager.changedFileName(DF_Norm, "_wm");
        fileManager.removeMissingValuesFromFile(new File(DF_Norm), DC);
        
        tempFileList[tempTotalFile]=DC;tempTotalFile++;

        fileToArrayWM(DC);// read data file without missing into array

        W=new double[kkk][TotalMissing];
        //generate name file
        nameFile = fileManager.changedFileName(DF, "_NAME");
        String gtest= mdl.extractDomainInfo(attrFile,DC, nameFile);
        
        tempFileList[tempTotalFile]=nameFile; tempTotalFile++;
 }
 /*
  * combine Membership Degree of (without+with)missing
  */
private void combineMembershipDegree()
{
    wMD=new double[kkk][totalRecord];

    int r,mm=0,wm=0;
    for(r=0;r<totalRecord;r++)
    {
        if(delta[r]==0)
      {
         for(int c=0;c<kkk;c++)
            wMD[c][r]=W[c][mm];
          mm++;
      }
        else
      {
          for(int c=0;c<kkk;c++)
            wMD[c][r]=MD[c][wm];
          wm++;
        }
    }

}

/*
  * initialize Membership Degree
  */
private void initializeMembershipDegree()
{
    MD=new double[kkk][totalRecordWM];

    double []tm=new double[kkk];
    double total;
    int c,r;
    for(r=0;r<totalRecordWM;r++)
    {
        total=0.0;
        for(c=0;c<kkk;c++)
        {
            tm[c]=generateRandomNumber(totalRecordWM);
            total+=tm[c];
        }
        for(c=0;c<kkk;c++)
        {
            MD[c][r]=tm[c]/total;
        }
    }
    
}


 // this method is used to print MD to file
private void printMDToFile(String outF)
{
        FileManager fileManager=new FileManager();
        String mdStr=fileManager.changedFileName(outF, "_md");
        File outFile=new File(mdStr);
        tempFileList[tempTotalFile]=mdStr;tempTotalFile++;
        for(int i=0;i<totalRecord;i++)
        {
            String rec="";
            for(int j=0;j<kkk;j++)
           {
            rec=rec+wMD[j][i]+", ";
            }
           if(i<totalRecord-1)
               rec=rec+"\n";

            if(i==0)
               fileManager.writeToFile(outFile, rec);
           else
               fileManager.appendToFile(outFile, rec);
        }
}




/*
 * calculate total membership degree
 */
private void createClusters_GFCM(String OutF)
{
    double diff=1.0, preObj=-1.0, cObj;
    initializeCentroids(nameFile);
    initializeMembershipDegree();
    updateCentroid();

    preObj=objectiveFunction();
    int cnt=0;
    generateLoopTerminator();
    do{
       updateMembershipDegree();
       updateCentroid();
       cObj=objectiveFunction();
       diff=Math.abs(preObj-cObj);
       cnt++;
       preObj=cObj;
       }while(diff>epsilon && cnt<=loopTerminator);


}
/*
 * calculate membership degree for the records having missing values
 */
private void calculateMembershipDegreeMR()
{
    double denominator, cDisN, cDisD;
    String cVal;

    for(int t=0;t<TotalMissing;t++)
    {
        for(int s=0;s<kkk;s++)
        {
            //calculate numerator
                cDisN=0.0;
                for(int i=0;i<totalAttr;i++)
                {
                 cVal=dataset[RS[t]][i];
                 if(cVal.equals("") ||cVal.equals("?")||cVal.equals("�"))
                 {

                 }
                else
                 {
                 if (attrType[i].equals("n"))
                 {
               cDisN+=calculateDistanceNumerical(s,i,Double.parseDouble(cVal));
                    }
                 else
                 {
                     cDisN+=calculateDistanceCategorical(s,i,cVal);
                 }
                }
            }
            //calculate denominator
            denominator=0.0;
            for (int c = 0; c < kkk; c++)
            {
                cDisD=0.0;
                for(int i=0;i<totalAttr;i++)
                {
                 cVal=dataset[RS[t]][i];
                 if(cVal.equals("") ||cVal.equals("?")||cVal.equals("�"))
                 {

                 }
                else
                     {

                        if (attrType[i].equals("n"))
                         {
              cDisD+=calculateDistanceNumerical(c,i,Double.parseDouble(cVal));
                            }
                         else
                         {
                             cDisD+=calculateDistanceCategorical(c,i,cVal);
                         }
                    }
                }
                //calculate denominator
                if(cDisD!=0)
                {
                    denominator += Math.pow(cDisN / cDisD, (1 / (mmm - 1)));
                }
             }
             if(denominator!=0)
             {
                W[s][t]=1.00/denominator;
             }
             else
             {
                W[s][t]=0.0;
             }
                 
        }
    }
}

/*
 * calculate total membership degree
 */
private void calculateTotalMembershipDegree()
{
    int c,r;
    for(c=0;c<kkk;c++)
    {
        tMD[c]=0.0;
        for(r=0;r<totalRecordWM;r++)
        {
            tMD[c]+=Math.pow(MD[c][r], mmm);
        }

    }
}

/*
 * update centroids
 */
private void updateCentroid()
{
    calculateTotalMembershipDegree();
    for(int c=0;c<kkk;c++)
    {
    for(int i=0;i<totalAttr;i++)
        {
         if (attrType[i].equals("n"))
             Frequency[c][i][0]=calculateNumericalCentroid(c,i);
         else
         {
           for(int d=0;d<domainsize[i];d++)
           {
               Frequency[c][i][d]=calculateFrequency(c,i,V[i][d]);
           }
         }
        }
    }
}

/*
 * update centroids
 */
private void updateMembershipDegree()
{
    double denominator, cDisN, cDisD;

    for(int t=0;t<totalRecordWM;t++)
    {
        for(int s=0;s<kkk;s++)
        {
            //calculate numerator 
             cDisN=0.0;
                for(int i=0;i<totalAttr;i++)
                {
                 if (attrType[i].equals("n"))
                 {
                     cDisN+=
         calculateDistanceNumerical(s,i,Double.parseDouble(datasetWM[t][i]));
                    }
                 else
                 {
                     cDisN+=calculateDistanceCategorical(s,i,datasetWM[t][i]);
                 }
                }
            
            //calculate denominator 
            denominator=0.0;
            for (int c = 0; c < kkk; c++)
            {
                cDisD=0.0;
                for(int i=0;i<totalAttr;i++)
                {
                 if (attrType[i].equals("n"))
                 {
                     cDisD+=
          calculateDistanceNumerical(c,i,Double.parseDouble(datasetWM[t][i]));
                    }
                 else
                 {
                     cDisD+=calculateDistanceCategorical(c,i,datasetWM[t][i]);
                 }
                }
                //calculate denominator

                denominator+=Math.pow(cDisN/cDisD, (1/(mmm-1)));
             }

            MD[s][t]=1.00/denominator;
        }
    }
}

/*
 * objective function
 */
private double objectiveFunction()
{
    double denominator=0.0, cDisN;

    for(int t=0;t<totalRecordWM;t++)
    {
        for(int s=0;s<kkk;s++)
        {
                cDisN=0.0;
                for(int i=0;i<totalAttr;i++)
                {
                 if (attrType[i].equals("n"))
                 {
                     cDisN+=
         calculateDistanceNumerical(s,i,Double.parseDouble(datasetWM[t][i]));
                    }
                 else
                 {
                     cDisN+=calculateDistanceCategorical(s,i,datasetWM[t][i]);
                 }
                }
              denominator+=MD[s][t]*cDisN;
        }
    }
    return denominator;
}

/*
 * For categorical attribute calculate distance
 */
private double calculateDistanceCategorical
        (int cluster, int attribute, String cVal )
{
    double distance=1.0;
    int r, d=-1;
    for(r=0;r<domainsize[attribute];r++)
        {
           if(V[attribute][r].equals(cVal))
           {
            d=r; break;
            }
        }

    if (d>-1)distance=1-Frequency[cluster][attribute][d];
    return distance;
}

/*
 * For numerical attribute calculate distance
 */
private double calculateDistanceNumerical
        (int cluster, int attribute, double cVal )
{
    double distance=0.0;
    distance=Math.pow(cVal-Frequency[cluster][attribute][0], 2);
    return distance;
}


/*
 * For categorical attribute calculate frequency
 */
private double calculateFrequency(int cluster, int attribute, String cVal )
{
    double total=0.0;
    int r;
    for(r=0;r<totalRecordWM;r++)
        {
           if(datasetWM[r][attribute].equals(cVal))
           {
            total+=Math.pow(MD[cluster][r], mmm);
            }
        }
    
    total=total/tMD[cluster];
    return total;
}

/*
 * For numerical attribute calculate centroid
 */
private double calculateNumericalCentroid(int cluster, int attribute )
{
    double total=0.0;
    int r;
    for(r=0;r<totalRecordWM;r++)
        {
            total+=Math.pow(MD[cluster][r], mmm)*
                    Double.parseDouble(datasetWM[r][attribute]);
        }

    total=total/tMD[cluster];
    return total;
}

 //following method will generate a random number between 1000 and 32767
    //In case of INFINITE loop, it will be used as loop terminator
    public double generateRandomNumber(int ub)
    {
        Random rand = new Random();
        return 1+rand.nextInt(ub);
    }

    //following method will generate a random number between 1000 and 32767
    //In case of INFINITE loop, it will be used as loop terminator
    public void generateLoopTerminator()
    {
        Random rand = new Random();
        loopTerminator = 1000+rand.nextInt(31767);
    }
 /*
  * initialize Centroids
  */
private void initializeCentroids(String nFile)
{
    FileManager fileManager = new FileManager();
    String [] nFileT = fileManager.readFileAsArray(new File(nFile));
    int l=nFileT.length;
    int max=0, i,j;
    StringTokenizer tokenizer;
    for(i=2;i<l;i++)
    {
        tokenizer = new StringTokenizer(nFileT[i], " ,\t\n\r\f");
        String aty=tokenizer.nextToken();
        if(aty.equals("c"))
        {
            aty=tokenizer.nextToken();
            domainsize[i-2]=Integer.parseInt(tokenizer.nextToken());
        }
        else
        {
            domainsize[i-2]=1;
        }
        if(domainsize[i-2]>max)max=domainsize[i-2];
    }
//   System.out.println(max);
   V=new String[totalAttr][max];
   Frequency=new double[kkk][totalAttr][max];
    for(i=2;i<l;i++)
    {
        tokenizer = new StringTokenizer(nFileT[i], " ,\t\n\r\f");
        String aty=tokenizer.nextToken();
        if(aty.equals("c"))
        {
            aty=tokenizer.nextToken();
            aty=tokenizer.nextToken();
            for(j=0;j<domainsize[i-2];j++)
                V[i-2][j]=tokenizer.nextToken();
        }
    }
}

 /*
  * store data file into dataset array
  */
private void fileToArray(String dFile)
{
    FileManager fileManager = new FileManager();
    String [] dataFileT = fileManager.readFileAsArray(new File(dFile));
    totalRecord=dataFileT.length;
    dataset=new String[totalRecord][totalAttr];
    RS=new int[totalRecord];
    delta=new int[totalRecord];
    TotalMissing=0;
    StringTokenizer tokenizer;
    for(int i=0;i<totalRecord;i++)
    {
        tokenizer = new StringTokenizer(dataFileT[i], " ,\t\n\r\f");
        delta[i]=1;
        for(int j=0;j<totalAttr;j++)
        {
            
            dataset[i][j]=tokenizer.nextToken();
            if(isMissing(dataset[i][j])==1)
            {
                delta[i]=0;
            }
       }
        if(delta[i]==0)
        {
            RS[TotalMissing] = i;TotalMissing++;
        }
     }
   totalRecordWM=totalRecord-TotalMissing;
  
}
/*
  * store data file (without missing) into dataset array
  */
private void fileToArrayWM(String dFile)
{
    FileManager fileManager = new FileManager();
    String [] dataFileT = fileManager.readFileAsArray(new File(dFile));
    datasetWM=new String[totalRecordWM][totalAttr];
    int i,j;
    StringTokenizer tokenizer;
    for(i=0;i<totalRecordWM;i++)
    {
        tokenizer = new StringTokenizer(dataFileT[i], " ,\t\n\r\f");
        for(j=0;j<totalAttr;j++)
        {
            datasetWM[i][j]=tokenizer.nextToken();
         }
       
     }
}

/*
  * find a value for categorical attribute having majority in each cluster 
  */
private void findMajorityCatVal()
{
  majorityCat=new String [kkk][totalAttr];
  for(int c=0;c<kkk;c++)
  {
      for(int j=0;j<totalAttr;j++)
      {
          if (attrType[j].equals("c"))
          {
              double max=Double.NEGATIVE_INFINITY;int vid=-1;
              for(int k=0;k<domainsize[j];k++)
              {
                  if(Frequency[c][j][k]>max)
                  {
                      max=Frequency[c][j][k];
                      vid=k;
                  }
              }
              majorityCat[c][j]=V[j][vid];
          }
      }
  }
}


/*
 * ****Approach Three******
 * Impute missing values based on approach three
 * Assign each record of D_C into cluster C_k ∈C
 for which it has a membership degree greater than zero.
 * Assign each record of D_I into cluster C_k
 for which it has a membership degree greater than zero.
 */
private void ImputeByClustering(String outF)
{
  FileManager fileManager = new FileManager();
  String []clusterFiles=new String [kkk];
  String []missingRecord=new String [kkk+1];
  String padd="_A3";
  
  for(int i=0;i<kkk;i++)
  {
      clusterFiles[i]=fileManager.changedFileName(dataFile, padd+i);
      fileManager.copyFile(dataFile, clusterFiles[i]);
  }
  String [] oDataFile = fileManager.readFileAsArray(new File(dataFile));
  //imputation by EM algorith
  FuzzyEM em=new FuzzyEM();
  for(int i=0;i<kkk;i++)
    {
         int temp=
  em.fuzzyEM(attrFile, clusterFiles[i], clusterFiles[i],0,attrType,-1, wMD, i);
    }
 //combine all imputed clusters

  for(int r=0;r<totalRecord;r++)
  {
      String tmp="";
      if(delta[r]==0)
      {
           for(int c=0;c<kkk;c++)
           {
               String [] cFile =
                    fileManager.readFileAsArray(new File(clusterFiles[c]));
               missingRecord[c]=cFile[r];
           }
           missingRecord[kkk]=oDataFile[r];
           tmp= weightedAvgByRecord(r, missingRecord);

      }
      else
       {
          tmp=oDataFile[r];
        }
      printRecToClusterFile(outF,tmp,r);

  }


  fileManager.removeListOfFiles(clusterFiles,kkk);

}


// this method is used to calculate weighted average based on
//membership degree and imputed clusters files

private String weightedAvgByRecord(int r, String []missingRecord)
{
    String tmp="";
    StringTokenizer token;
    String [][]cVal=new String[kkk+1][totalAttr];
    for(int c=0;c<=kkk;c++)
     {
       token= new StringTokenizer(missingRecord[c], " ,\t\n\r\f");
       for(int j=0;j<totalAttr;j++)
        {
            cVal[c][j]= token.nextToken();

        }
     }
    
    for(int j=0;j<totalAttr;j++)
        {
           if(isMissing(cVal[kkk][j])==1)
            {
              
               if (attrType[j].equals("n"))
               {
                   double tmpVal=0.0;
                   for(int c=0;c<kkk;c++)
                   {
                      if(isMissing(cVal[c][j])!=1)
                          tmpVal+= wMD[c][r]*Double.parseDouble(cVal[c][j]);
                    }
                   
                   tmp=tmp+tmpVal+",";
               }
                else
               {
                  tmp=tmp+ weightedAvgCat(r,  j)+",";
               }

            }
             else
            {
                tmp=tmp+cVal[kkk][j]+",";

              }
         }
    
    return tmp;
}
// this method is used to find the domain value which has max weighted avg.
private String weightedAvgCat(int r,  int attr)
{
   String tmp="";
   double max=Double.NEGATIVE_INFINITY;int vid=-1;
   for(int k=0;k<domainsize[attr];k++)
    {
      double tmpv=0.0;
      for(int c=0;c<kkk;c++)
      {
          tmpv=tmpv+wMD[c][r]*Frequency[c][attr][k];
      }
      if(tmpv>max)
      {
          max=tmpv;
          vid=k;
      }
  }
  tmp=V[attr][vid];
//  System.out.println(tmp);
  return tmp;
}
// this method is used to print MD to file
private void printRecToClusterFile(String outF,String rec, int cSize)
{
        FileManager fileManager=new FileManager();
        File outFile=new File(outF);
        rec=rec+"\n";
        if(cSize==0)
               fileManager.writeToFile(outFile, rec);
        else
               fileManager.appendToFile(outFile, rec);
}
 /*
  * this function will indicate whether or not a value is missing.
  */

 private int isMissing(String oStr)
    {
        int ret=0;
        if(oStr.equals("")||oStr.equals("?")||oStr.equals("�"))
                     {
                         ret=1;
                    }
      return ret;
    } 
}
