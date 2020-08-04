/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package femi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class Main {
        /** command line reader */
    BufferedReader stdIn;
        /** class name, used in logging errors */
    static String className = femi.Main.class.getName();
    
    public Main()
    {
        stdIn = new BufferedReader(new InputStreamReader(System.in));
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main terminal=new Main();
        String fileAttrInfo = terminal.inputFileName("Please enter the name of the file containing the 2 line attribute information.(example: c:\\data\\attrinfo.txt?)");
        String fileDataFileIn= terminal.inputFileName("Please enter the name of the data file having missing values: (example: c:\\data\\data.txt?)");
        String fileOutput = terminal.inputFileName("Please enter the name of the output file: (example: c:\\data\\out.txt?)");
        //call FEMI
        FEMI femi=new FEMI();
        femi.setClusterSize(3);//set cluster size
        femi.runFEMI(fileAttrInfo, fileDataFileIn, fileOutput);
        System.out.println("\nImputation by FEMI is done. The completed data set is written to: \n"+fileOutput);
    }
      

    /**
     * Given a message to display to the user, ask user to enter a file name.
     *
     * @param message message to user prompting for filename
     * @return filename entered by user
     */
    private String inputFileName(String message)
    {
        String fileName = "";
        try
        {
            System.out.println(message);
            fileName = stdIn.readLine();
        }
        catch (IOException ex)
        {
            Logger.getLogger(className).log(Level.SEVERE, null, ex);
        }
        return fileName;
    }

}
