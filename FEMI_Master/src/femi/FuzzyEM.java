/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package femi;

import java.io.*;
import java.util.*;
import Jama.*;
import java.text.DecimalFormat;
/**
 *
 * @author grahman
 */
public class FuzzyEM
{
/** used for managing reading and writing to file */
    FileManager fileManager_g;
    File outFile;
    private String [] attrType;
    private int [] missingAttrs;
    private int noOfAttrs;
    private int noOfRecords;
    private int noOfNumericalAttrs;
    private int totalMissingValues;
    private int loopTerminator;
    private    double [][]dataOriginal;
    private    double []mu;
    private    double [][]cov;
    private    double []std;
    private    double totalMD;
    private    double [][]cor;
    private int [] avgImputation;
    private int totAvg;
    String str;
    int temp;
    public int fuzzyEM(String nFile, String dFile, String oFile,int sPos,
            String []infAttrType,int noOfInf,double [][]MD, int cluster)
        {
            MissingInformation sds= new MissingInformation();
            totalMissingValues=sds.findMissingAttributeInfo(dFile,nFile);
            if(totalMissingValues>0)
            {

                noOfAttrs=sds.getNoOfAttr();
                missingAttrs=sds.getMissingAttr();
                noOfRecords=sds.getTotalNoOfRecords();
                if(noOfInf>0)
                {
                attrType=infAttrType;
                noOfNumericalAttrs=noOfInf;
                }
                else
                {
                attrType=sds.getTypeOfAttr();
                noOfNumericalAttrs=sds.getNoOfNumericalAttr();
                }
                calTotalMD(MD,cluster);
                chkCorrelation(dFile,sPos);
                emAlgorithmImplementation(dFile,oFile,sPos,MD,cluster);
                if(totAvg>0)
                {
                    AverageImputation avgImp=new AverageImputation();
      avgImp.averageImputationInt(oFile, noOfAttrs, avgImputation,sPos);
                }
            }
            return totalMissingValues;
       }
     /*
     * check correlation of the dataset
     */
    private void calTotalMD(double [][]MD,int cluster)
    {
        totalMD=0.0;
        for(int r=0;r<noOfRecords;r++)
        {
            totalMD+=MD[cluster][r];
        }
    }

    /*
     * check correlation of the dataset
     */
    private void chkCorrelation(String dFile,int sPos)
    {
         StringTokenizer tokenizer;
         FileManager fileManager=new FileManager();
         String [] dataFile_g = fileManager.readFileAsArray(new File(dFile));
         noOfRecords=dataFile_g.length;
         avgImputation=new int[noOfAttrs];
          for(int i=sPos; i<noOfAttrs;i++)
              avgImputation[i]=0;
         totAvg=0;
         double preVal, curVal;
         for(int c=sPos;c<noOfAttrs;c++)
         {
             if (attrType[c].equals("n"))
            {
                int cnt=0;
                preVal=0.0;curVal=0.0;
                for(int i=0; i<noOfRecords;i++)
                {
             tokenizer = new StringTokenizer(dataFile_g[i], " ,\t\n\r\f");
                    for(int j=sPos;j<c;j++)
                    {
                      tokenizer.nextToken();
                    }
                    str=tokenizer.nextToken();
                    if (str.equals("?"))
                     curVal=0.0;
                    else
                     curVal=Double.parseDouble(str);
                    if(curVal!=preVal)
                    {
                        preVal=curVal;
                        cnt++;
                    }
                   if(cnt>=2)break;
                }
                if(cnt<=1&& noOfRecords!=1)
                    {
                        noOfNumericalAttrs--;
                        attrType[c]="c";
                        avgImputation[c]=1;
                        totAvg++;
                    }
             }
         }
    }
    /*
     * Implementation of EM algorithm here for MVI
     */
    public void emAlgorithmImplementation(String dFile,String oFile,
            int sPos,double [][]MD, int cluster)
    {
        StringTokenizer tokenizer;
        if(noOfRecords<=noOfNumericalAttrs)
        {
            System.out.println("In FuzzyEM algorithm, no of records should be "
                    + "greater than numerical attributes, "
                    + "but here Total record= "+noOfRecords+" and Total "
                    + "numerical attributes= "+noOfNumericalAttrs);
        }
        else
        {
            dataOriginal=new double[noOfRecords][noOfNumericalAttrs];

            mu=new double[noOfNumericalAttrs];
            std=new double[noOfNumericalAttrs];
            fileManager_g = new FileManager();
            double val;
            //preserve the missing record number
            int []msRow=new int[totalMissingValues];
            //preserve the missing column number
            int []msCol=new int[totalMissingValues];
            //preserve the total of missing record number
            int []msRec=new int[totalMissingValues];
            int []msStart=new int[totalMissingValues];
            int []msLength=new int[totalMissingValues];

            //finding the cell locations (row, column) of missing values
            //into the arrays msRow and msCol respectively
 //and finding the data without categorical attributes into  dataOriginal array
            int ms=0, totMV;
            int rec=0;
       String [] dataFile_g = fileManager_g.readFileAsArray(new File(dFile));
            noOfRecords=dataFile_g.length;
             for(int i=0; i<noOfRecords;i++)
                {
                    int nc=0; //numerical attriblutes counts
                tokenizer = new StringTokenizer(dataFile_g[i], " ,\t\n\r\f");
                    int fg=0;
                    for(int c=sPos;c<noOfAttrs;c++)
                    {
                        str = tokenizer.nextToken();
                        if (attrType[c].equals("n"))
                        {
                            if (str.equals("?"))
                            {
                                val = 0.0;
                                msRow[ms]=i;
                                msCol[ms]=nc;
                                ms++;
                                fg++;
                            }
                            else
                            {
                               val=Double.parseDouble(str);
                            }
                            dataOriginal[i][nc]=val;
                            nc++;
                        }

                    }
                   if(fg>0)
                    {
                       msRec[rec]=i;
                       msLength[rec] =fg;
                        if(rec==0)
                         msStart[rec] =0;
                        else
                         msStart[rec] =msStart[rec-1]+msLength[rec-1];

                       rec++;
                    }
                }
           totMV=ms;
            int totRec=rec;
           int []isConverse=new int[noOfNumericalAttrs];
           isConverse=missingAttrs.clone();
           double [][]dataCurrent=new double[noOfRecords][noOfNumericalAttrs];
           mu=new double[noOfNumericalAttrs];
           initilizeArray(mu);
           double []muPrevious=new double[noOfNumericalAttrs];
           initilizeArray(muPrevious);
           cov=new double[noOfNumericalAttrs][noOfNumericalAttrs];
           cor=new double[noOfNumericalAttrs][noOfNumericalAttrs];
    double [][]covPrevious=new double[noOfNumericalAttrs][noOfNumericalAttrs];

           MatrixCalculation mxCal=new MatrixCalculation();
//           Matrix curDataset;

           int T=0;
           int meanError;
           int covError;
           generateLoopTerminator();
           //copy of original data for updating
           dataCurrent=dataOriginal.clone();

           do{
               muPrevious=mu.clone();
               covPrevious=cov.clone();
               ms=0;


               computeMean(dataCurrent, noOfRecords, noOfNumericalAttrs,
                       msRow, msCol, ms, mu,MD,cluster);

               computeCovariance(dataCurrent, noOfRecords,
                       noOfNumericalAttrs,  mu,cov,MD,cluster);

               if(T==0)
               {
                   ms = totMV;
                   mxCal.computeCorrelation(dataCurrent,noOfRecords,
                           noOfNumericalAttrs, cor);
               }
               for(rec=0;rec<totRec;rec++)
               {
                    emImputation(dataCurrent,msRec[rec],msCol,
                            msStart[rec],msLength[rec],T);
               }

               //mean error calculation
               meanError= calMeanError(muPrevious,mu);
               //covariance matrix (det) error calculation
               covError=calCovError(covPrevious,cov);
               T++;
               if(T>loopTerminator)break;
               if(T%1000==0)
                   System.out.println("Fuzzy EM iteration: "+T);

           }while(meanError==1 || covError==1);
//           System.out.println("Iteration: "+T);
        //writing dataset to the file oFile after imputation
        String recStr;
        int nc=0; //numerical attriblutes counts
        outFile = new File(oFile);
        DecimalFormat df = new DecimalFormat("####0.0");
        dataFile_g = fileManager_g.readFileAsArray(new File(dFile));
        for(int i=0; i<noOfRecords;i++)
                {
                    nc=0;
                    recStr="";
                tokenizer = new StringTokenizer(dataFile_g[i], " ,\t\n\r\f");
                    for(int c=sPos;c<noOfAttrs;c++)
                    {
                        str = tokenizer.nextToken();
                        if (attrType[c].equals("n"))
                        {
                            if(c==sPos)
                            recStr=recStr+df.format(dataCurrent[i][nc]);
                            else
                             recStr=recStr+", "+df.format(dataCurrent[i][nc]);
                            nc++;
                        }
                        else
                        {
                            if(c==sPos)
                                recStr=recStr+str;
                            else
                                recStr=recStr+", "+str;
                        }
                    }
                    recStr=recStr+"\n";
                   if(i==0)
                       fileManager_g.writeToFile(outFile, recStr);
                   else
                       fileManager_g.appendToFile(outFile, recStr);
                }
        }
    }
    public void printDataset(double [][]data)
    {
        DecimalFormat df = new DecimalFormat("####0.0");
        for(int i=0;i<noOfRecords;i++)
        {
            System.out.print(i+" ");
            for(int j=0;j<noOfNumericalAttrs;j++)
            {
                System.out.print(df.format(data[i][j])+" ");
            }
            System.out.print("\n");
        }
    }

    //impute value for a specific cell and
    //EM Implementation.
    public void emImputation(double [][]dataElement,int msRow,int []msCol,
            int sPos, int m,int T)
    {
       int r,c,k,j;
       int a=noOfNumericalAttrs-m;
       double []X_a=new double[a];
       double []X_m=new double[m];
       double []Mu_a=new double[a];
       double []Mu_m=new double[m];
       double [][]cov_aa=new double[a][a];
       double [][]cov_mm=new double[m][m];
       double [][]cov_am=new double[a][m];
       double [][]B=new double[a][m];

Matrix matX_a,matX_m, matMu_a,matMu_m,matcov_aa,matcov_mm,matcov_am,matcov_ma;
       Matrix matinvOfcov_aa,matB,  matC,mate;
       // finding available data elements for the record msRow
       // finding the mean vector except attribute msCol
       k=0;
       for(c=0;c<noOfNumericalAttrs;c++)
       {
           if(chkAttrIsMiss(c,msCol,sPos,sPos+m)==0)
           {
               X_a[k]=dataElement[msRow][c];
               Mu_a[k]=mu[c];
               k++;
           }
       }

       MatrixCalculation mxCal=new MatrixCalculation();
       //converting the data array to matrix
       matX_a= mxCal.oneDArrayToMatrix(X_a);
//       matX_a.print(4,2);
       matMu_a= mxCal.oneDArrayToMatrix(Mu_a);
//       matMu_a.print(4,2);
       //initialising missing cell
        for(c=sPos,k=0;c<sPos+m;c++,k++)
        {
            X_m[k]=dataElement[msRow][msCol[c]];
            Mu_m[k]=mu[msCol[c]];//mean value of missing attribute
        }

       matX_m= mxCal.oneDArrayToMatrix(X_m);//convert to matrix
//       matX_m.print(4,2);
       matMu_m= mxCal.oneDArrayToMatrix(Mu_m);//convert to matrix
//       matMu_m.print(4,2);

       //if do not have any availble values then simple add the mean
       if(a>0)
       {
           //finding covariance only for the attributes having values
           k=0;
           for(r=0;r<noOfNumericalAttrs;r++)
           {
               j=0;
               if(chkAttrIsMiss(r,msCol,sPos,sPos+m)==0)
               {
                   for(c=0;c<noOfNumericalAttrs;c++)
                   {
                       if(chkAttrIsMiss(c,msCol,sPos,sPos+m)==0)
                       {
                           cov_aa[k][j]=cov[r][c];
                           j++;
                       }
                   }
                   k++;
               }
            }
           matcov_aa= new Matrix(cov_aa);//convert to matrix of available cov
          double tt=matcov_aa.det();
          if(tt!=0)
          {
    //       matcov_aa.print(4,2);
          for(r=sPos,j=0;r<sPos+m;r++,j++)
            {
           for(c=sPos,k=0;c<sPos+m;c++,k++)
            {
               //covariance array only for missing attribute
               cov_mm[j][k]=cov[msCol[r]][msCol[c]];
            }
            }
           matcov_mm=new Matrix(cov_mm);//convert to matrix
    //       matcov_mm.print(4,2);
           //finding cross covariance for avaliable and missing
           k=0;
           for(r=0;r<noOfNumericalAttrs;r++)
           {
              if(chkAttrIsMiss(r,msCol,sPos,sPos+m)==0)
               {
                  j=0;
                  for(c=sPos;c<sPos+m;c++)
                    {
                      cov_am[k][j]=cov[r][msCol[c]];
                      j++;
                    }
                  k++;
                }
            }
           //convert to matrix of cross cov of available & missing
          matcov_am= new Matrix(cov_am);
    //create matrix of cross cov for missing & available
          matcov_ma=matcov_am.transpose(); 
    //      matcov_ma.print(4,2);
          //implementing EM equation
          try
          {
          matinvOfcov_aa=matcov_aa.inverse();
    //      matinvOfcov_aa.print(4,2);
          matB=matinvOfcov_aa.times(matcov_am);
    //      matB.print(4,2);
    //      mattransposeOfB=matB.transpose();
          matC=matcov_ma.times(matinvOfcov_aa.times(matcov_am));
    //      matC.print(4,2);

          ////
          Matrix Xa_Minus_Mua=matX_a.minus(matMu_a);
          Matrix Xa_Minus_Mua_TimesB=Xa_Minus_Mua.times(matB);
          matX_m=matMu_m.plus(Xa_Minus_Mua_TimesB);
 ///following the calculation of the residual matrix e (1xPm),
          //for first iteration
          // with mean zero and unknown covariance matrix C (Pm x Pm)
          if(T==0)
          {
        mate = generateResidualMatrix(X_m,matMu_m, matcov_mm,matC, msRow, m);
            matX_m=matX_m.plus(mate);
            }
          }
          catch(Exception ex)
          {
            matX_m=matMu_m;
          }
          }
         else
          {
              matX_m=matMu_m;
            }
        }
       else
        {
           matX_m=matMu_m;
        }
      //result
      mxCal.matrixTo1DArray(matX_m,X_m,m);
      // update dataset
       for(c=sPos,k=0;c<sPos+m;c++,k++)
        {
           dataElement[msRow][msCol[c]]=X_m[k];
        }

    }

/* the following method generates perturbed dataset
 @param matX_m matrix of a recored of missing attributes
 @param matMu_m mean matrix of missing attributes
 @param matCov_m covariance matrix of missing attributes
 @param pertVar covariance matrix of the data set
 @param recNum record number having missiong values
 @param m no. of missing values
 @return a residual matrix with mean zero and unknown covariance
 */

    public Matrix  generateResidualMatrix(double []matX_m,Matrix matMu_m,
            Matrix matCov_m,Matrix pertVar,int recNum,int m)
    {
            Matrix output, cholPertVariance,pertVariance;
            Matrix mean, randomMatrix;
            CholeskyDecomposition cholesky;
            pertVariance=pertVar;
            cholesky = pertVariance.chol();
            cholPertVariance = cholesky.getL();
            //return zero mean vector
            mean = calculateMean(matX_m,matMu_m,matCov_m,recNum,m);
            randomMatrix = generateRandomVariates(m);
            output = mean.plus(pertVar.times(randomMatrix.transpose()));
            output = output.transpose(); // it produces a matrix of dim.(1x6)

            return output;
    }// end of generateResidualMatrix()

// this method returns a vector of expected values for a perturbed record
    // given the original record
    public Matrix calculateMean(double []X_m,Matrix matMu_m,
            Matrix matCov_m,int recNum,int m){
        Matrix c;//, mean, result,covXX,covYX,covYY;
        MatrixCalculation mxCal=new MatrixCalculation();

        double [] c_rec = new double[m];
        for (int i=0;i<m;i++)
            c_rec[i]=0.0;//X_m[i];
            c = mxCal.oneDArrayToMatrix(c_rec);
        return c.transpose();//mean;
    }
    // following method generates a vector of random variates
    // from a standard normal distribution
    public Matrix generateRandomVariates(int numAttr){
        Matrix rndMat;
        double[] randVariates = new double[numAttr];
        for(int i=0; i<numAttr; i++)
           randVariates[i] = 0;
        Random rand = new Random();
        for(int j=0; j<numAttr; j++){
           // randVariates[j] = rand.nextNormal();
           randVariates[j] = rand.nextGaussian();
        }
        MatrixCalculation mxCal=new MatrixCalculation();
       //converting the data array to matrix
        rndMat =  mxCal.oneDArrayToMatrix(randVariates);
        // The following print outs are to see the dimensions of the
        // rndMat matrix:
        // System.out.println("Printing Z vector: ");
       return rndMat;
    }
    //check whether an attribute has found in the array msCol.
public int chkAttrIsMiss(int sCol,int []msCol,int sPos,int ePos)
    {
        int i, flag=0;
        for(i=sPos;i<ePos;i++)
        {
            if(msCol[i]==sCol)
            {
                flag=1;break;
            }
        }
        return flag;
    }

    //claculate the mean error and
    //check whether a missing attribute has conversed or not.
    public int calMeanError(double []preMu,double []curMu)
    {

        int isError=1, tA=0;
        double diff=0.0;
        double cuT=0.0,cyAvg=0.0;
        double preT=0.0,preAvg=0.0;
        tA=preMu.length;
        for(int i=0;i<tA;i++)
        {
            cuT=cuT+curMu[i];
            preT=preT+preMu[i];
        }
        if(tA>0)
        {
            cyAvg= cuT/tA;
            preAvg= preT/tA;
        }

        diff=Math.abs(preAvg-cyAvg);

         if(diff<0.0000000001)
         {
            isError=0;
         }


        return isError;
    }

     //claculate the covariance error and
    //check whether a missing attribute has conversed or not.
    public int calCovError(double [][]preCov,double [][]curCov)
    {

        int isError=1;
        double diff=0.0;
        double preDet=0.0;
        double curDet=0.0;
        Matrix preCov_mat=new Matrix(preCov);
        Matrix curCov_mat=new Matrix(curCov);
        preDet=preCov_mat.det();
        curDet=curCov_mat.det();
        diff=Math.abs(preDet-curDet);
        if(diff<0.0000000001)
         {
            isError=0;
         }
        return isError;
    }
    //following method will generate a random number between 1000 and 32767
    //In case of INFINITE loop, it will be used as loop terminator
    public void generateLoopTerminator()
    {
        Random rand = new Random();
        loopTerminator = 1000+rand.nextInt(31767);
    }
    //following method will initialize an array.(data type double)
     public void initilizeArray(double []curArr)
    {
      for(int i=0;i<curArr.length;i++)
          curArr[i]=0.0;
     }

     // the following method takes two int variables as input.
    // computes means of attributes from initialAttr to
    // finalAttr

    public void computeMean(double[][]DataElement,int numRecords,int numAttr,
       int []msRow,int []msCol,int ms,double []mu,double [][]MD, int cluster )
    {
        int totRec,isMiss;
        for(int i=0; i<numAttr; i++)
            mu[i] = 0;
        double sum=0;
        for(int i=0; i<numAttr; i++)
        {
           sum = 0;totRec=0;
           for(int j = 0; j<numRecords; j++)
           {
                   isMiss=0;
                   for(int m=0;m<ms;m++)
                   {
                     if(msRow[m]==j && msCol[m]==i) {isMiss=1;break;}
                   }
                   if(isMiss==0)
                   {
                       totRec++;//counting available rows of an attribute i
                       sum += MD[cluster][j]*DataElement[j][i];
                   }
           }
          if(totalMD>0)     mu[i] = sum/totalMD;
        }

    }
// the following method initialises all variables of the
    // double[][] cov equals to zero.
    // This method therefore should be used only once before
    // the computation of the covariances between the attributes
    // by computeCovariance()
    public void initialiseCovariance(double [][]cov,int numAttr){
        for(int i=0; i<numAttr; i++){
           for(int j=0; j<numAttr; j++){
              cov[i][j]=(double)0.0;
           }
        }// end of the outer for()
    }
    

    // the following method takes two int variables as input.
    // the input variables are initial attribute and final attr.
    // this method calculates the co-variance between each attr
    // pair from initial attribute to final attribute.
    // this method also calculates variance of each attribute.
    public void computeCovariance(double[][]DataElement,int numRecords,
            int numAttr,double []mu,double [][]cov,double [][]MD, int cluster )
    {
       initialiseCovariance(cov,numAttr);
        for(int i=0; i<numAttr; i++){
            for(int j=0; j<numAttr; j++){
   cov[i][j]= getCovariance(i,j,DataElement,mu,numRecords,numAttr,MD,cluster);

             } // end of inner for()



        }// end of outer for()
    }
public double getCovariance(int attrPos1, int attrPos2,double[][]DataElement,
        double []mu,int numRecords,int numAttr,double [][]MD, int cluster){

        double sum_coproduct = 0;
        double mean_x = mu[attrPos1];
        double mean_y = mu[attrPos2];
        for(int i=0;i<numRecords;i+=1){
            double delta_x = DataElement[i][attrPos1]-mean_x;
            double delta_y = DataElement[i][attrPos2]-mean_y;
            sum_coproduct += MD[cluster][i]*delta_x * delta_y ;
        }
        return sum_coproduct / totalMD;
    }
public double getCovariance_tmp(int attrPos1, int attrPos2,double[][]DataElement,
        int numRecords,int numAttr,double [][]MD, int cluster){
        double result = 0;
        double sum_coproduct = 0;
        double mean_x = DataElement[0][attrPos1];
        double mean_y = DataElement[0][attrPos2];
        for(int i=2;i<numRecords+1;i+=1){
            double sweep =Double.valueOf(i-1)/i;
            double delta_x = DataElement[i-1][attrPos1]-mean_x;
            double delta_y = DataElement[i-1][attrPos2]-mean_y;
            sum_coproduct += MD[cluster][i-1]*delta_x * delta_y * sweep;
            mean_x += delta_x / i;
            mean_y += delta_y / i;
        }
        double cov_x_y = sum_coproduct / totalMD;
        result=cov_x_y;
        return result;
    }

}
