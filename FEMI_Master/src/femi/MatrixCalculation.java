/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package femi;

import Jama.*;

/**
 *
 * @author grahman
 */
public class MatrixCalculation {

    /**
     * The method takes two integer variables as input.
     * computes means of attributes from initialAttr to finalAttr
     */
    

    public void computeMean(double[][]DataElement,int numRecords,int numAttr, int []msRow,int []msCol,int ms,double []mu )
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
                       sum += DataElement[j][i];
                   }
           }
          if(totRec>0)     mu[i] = sum/totRec;
        }

    }

    //the following method compute standard deviation
    
public void computeStd(double[][]DataElement,int numRecords,int numAttr, int []msRow,int []msCol,int ms,double []mu,double []std )
    {
        int totRec,isMiss;
        for(int i=0; i<numAttr; i++)
            std[i] = 0;
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
                       sum += DataElement[j][i]-mu[i];
                   }
           }
          if(totRec>0)     std[i] = Math.sqrt(sum/totRec);
        }

    }

    /**
     * The method initializes all variables of the double[][] cov equals to zero.
     * This method therefore should be used only once before
     * the computation of the covariances between the attributes
     * by computeCovariance()
     */
    
    public void initialiseCovariance(double [][]cov,int numAttr){
        for(int i=0; i<numAttr; i++){
           for(int j=0; j<numAttr; j++){
              cov[i][j]=(double)0.0;
           }
        }// end of the outer for()
    }
    // this method calculates correlation of each attribute.
    public void computeCorrelationStdCov(double []std,double [][]cov,int numAttr, double [][]cor){
       initialiseCovariance(cor,numAttr);
        for(int i=0; i<numAttr; i++){
            for(int j=0; j<numAttr; j++)
               {
                if(std[i]!=0 && std[j]!=0)
                {
                    cor[i][j] = cov[i][j] / (std[i] * std[j]);
                 }
               } // end of inner for()
        }// end of outer for()
   }
 // this method calculates correlation of each attribute.
    public void computeCorrelation(double[][]DataElement,int numRecords,int numAttr, double [][]cor){
       initialiseCovariance(cor,numAttr);
        for(int i=0; i<numAttr; i++){
            for(int j=0; j<numAttr; j++)
               {
                    cor[i][j] = getCorrelation(i,j,DataElement,numRecords,numAttr);
                } // end of inner for()
        }// end of outer for()
   }
public double getCorrelation(int attrPos1, int attrPos2,double[][]DataElement,int numRecords,int numAttr){
        double result = 0;
        double sum_sq_x = 0;
        double sum_sq_y = 0;
        double sum_coproduct = 0;
        double mean_x = DataElement[0][attrPos1];
        double mean_y = DataElement[0][attrPos2];
        for(int i=2;i<numRecords+1;i+=1){
            double sweep =Double.valueOf(i-1)/i;
            double delta_x = DataElement[i-1][attrPos1]-mean_x;
            double delta_y = DataElement[i-1][attrPos2]-mean_y;
            sum_sq_x += delta_x * delta_x * sweep;
            sum_sq_y += delta_y * delta_y * sweep;
            sum_coproduct += delta_x * delta_y * sweep;
            mean_x += delta_x / i;
            mean_y += delta_y / i;
        }
        double pop_sd_x = (double) Math.sqrt(sum_sq_x/numRecords);
        double pop_sd_y = (double) Math.sqrt(sum_sq_y/numRecords);
        double cov_x_y = sum_coproduct / numRecords;
        double std_x_y=pop_sd_x*pop_sd_y;
        if(std_x_y !=0.0)
            result = cov_x_y / std_x_y;
        else
            result=1.0;
        return result;
    }

    

    // the following method takes two int variables as input.
    // the input variables are initial attribute and final attr.
    // this method calculates the co-variance between each attr
    // pair from initial attribute to final attribute.
    // this method also calculates variance of each attribute.
    public void computeCovariance(double[][]DataElement,int numRecords,int numAttr,double []mu,double [][]cov ){
       initialiseCovariance(cov,numAttr);
        for(int i=0; i<numAttr; i++){
            for(int j=0; j<numAttr; j++){
                    cov[i][j]= getCovariance(i,j,DataElement,mu,numRecords,numAttr);
//                    if(i==j) {
//                        cov[i][j]= compVar(i,DataElement,numRecords,numAttr,mu);
//                    }
//                    else {
//                       cov[i][j]= compCov(i,j,DataElement,numRecords,numAttr,mu);
//                    }
               
             } // end of inner for()

         

        }// end of outer for()
        // System.out.println();
    }
public double getCovariance(int attrPos1, int attrPos2,double[][]DataElement,double []mu,int numRecords,int numAttr){
        double sum_coproduct = 0;
        double mean_x = mu[attrPos1];
        double mean_y = mu[attrPos2];
        for(int i=0;i<numRecords;i+=1){
            double delta_x = DataElement[i][attrPos1]-mean_x;
            double delta_y = DataElement[i][attrPos2]-mean_y;
            sum_coproduct += delta_x * delta_y;
        }
       return sum_coproduct / numRecords;
    }

    // the following method helps computeCovariance() method in
    // computing variance of a given attribute
    public double compVar(int attrPos,double[][]DataElement,int numRecords,int numAttr,double []mu){
        double var=0;
        double sum=0;

        for(int i=0; i<numRecords; i++){
           sum += Math.pow((DataElement[i][attrPos]-mu[attrPos]),2);
        }
        var = sum/(numRecords-1);
        if(var> -2000000 && var<2000000){
           var=((int)(var*100))/100.0;
        }
        return var;
    }

    // the following method helps computeCovariance() method in
    // computing co-variance of two given attributes
    public double compCov(int attrPos1, int attrPos2,double[][]DataElement,int numRecords,int numAttr,double []mu){
        double cov=0;
        double sum=0;

        for(int i=0; i<numRecords; i++){
            sum += (DataElement[i][attrPos1]-mu[attrPos1])
                  *(DataElement[i][attrPos2]-mu[attrPos2]);
        }
        //cov = Math.sqrt(sum/(numRecords-1));
        cov = sum/numRecords;
        if(cov> -2000000 && cov<2000000){
           cov=((int)(cov*100))/100.0;
        }
        return cov;
    }

    // the following method creates the Mean vector
    public Matrix oneDArrayToMatrix(double []mu){
        int numAttr=mu.length;
        double[][] arrayMx =new double[1][numAttr];
        for(int i=0;i<numAttr;i++)
            arrayMx[0][i]=mu[i];
        Matrix Mu_x = new Matrix(arrayMx);
        return Mu_x;
    }

     // the following method return 1 D array from a Mtrix
    public void matrixTo1DArray(Matrix src,double []dest,int numAttr)
    {
        double[][] arrayMx =src.getArray();
        for(int i=0;i<numAttr;i++)
            dest[i]=arrayMx[0][i];
    }
    // the following method creates all necessary matrices
    // from the relevent arrays
    public Matrix createAllCovMatrices(double [][]cov)
    {
      Matrix covXX = new Matrix(cov);
      return covXX;
    }

 /**
 * Pesudo-inverse of a matrix
 * @param A Matrix A will be inversed/pesudo-inversed
 * @param n the number of rows
 * @param m the number of columns
 * @return A the inverse/pesudo-inverse of the given matrix A
 */

public Matrix pesudoInverse(Matrix A, int n,int m)
{
    try{
    if(m==n)
        {
            A=A.inverse();
        }
        else
        {
            Matrix ATrans=A.transpose();
            if(n>m)
                {
                Matrix aMatTe=ATrans.times(A);
                Matrix aMatTemp=aMatTe.inverse();
                A=aMatTemp.times(ATrans).transpose();
                }
            else
            {
                Matrix aMatTe=A.times(ATrans);
                Matrix aMatTemp=aMatTe.inverse();
                A=ATrans.times(aMatTemp).transpose();
            }

        }
    }
    catch(Exception e)
    {
        //do nothing
    }
    return A;
}

}
