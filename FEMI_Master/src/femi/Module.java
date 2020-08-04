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
 * @author grahman
 */
public class Module {


    /**
     * Generate a name file from a data file. Also need to input a two line file
     * containing the attribute types one first line, and attribute names on second.
     * Line 1 shows attribute type for each attribute,
     * 0=categorical, 1=numerical, 2=class e.g.
     * <p>l 0 1 2 0 1</p>
     * <p>Line 2 shows attribute names, note that names can be separated by spaces,
     * tabs, or commas. However, names need to be one word, and can contain any
     * character except for comma.</p>
     * @param attrInfo the file containing attribute info on two lines
     * @param dataFile the data file we are generating the name file for
     * @param outFile the filename for the new name file
     * @return an message, either indicating success, or describing error
     */
    public String extractDomainInfo(String attrF, String dataF, String outF)
    {
        FileManager fileManager = new FileManager();
        File attrInfo=new File(attrF);
        File dataFile=new File(dataF);
        File outFile=new File(outF);
        /** read the two files and store as arrays */
        String [] attrFile = fileManager.readFileAsArray(attrInfo, 2);
        String [] dataStrings = fileManager.readFileAsArray(dataFile);
        /** store return message */
        String retStr="Resulting domain information successfully written to " + outFile.getPath();
        /** read the first line of the attribute info file to determine the number
         * and type of attributes, as well as class attribute
         */
        int [] attrTypes=null; //will store attribute type for each attribute
        String [] attrNames = null; //store the names of each attribute
        int classIndex = -1; //the attribute index of the class attribute
        int numAttrs = -1; //the number of attributes
        double [] numHighDomain=null; //store highest value for numerical attrs
        double [] numLowDomain=null;  //store lowest value for numerical attrs
        double [] intervals = null; //store the minimum interval between values
        ArrayList [] catValues = null; //for each categorical attr store a list of string values
        ArrayList [] numValues = null; //store list of numerical values
        try{
            /** tokenize first line to find out how many attributes we have, and then
             * store their type, tokenize second line to get attribute names
             */
            StringTokenizer tokens = new StringTokenizer(attrFile[0], " ,\n\t"); //remove spaces, commas, tabs and newlines
            StringTokenizer tokensNames =new StringTokenizer(attrFile[1], " ,\n\t"); //remove spaces, commas, tabs and newlines
            numAttrs = tokens.countTokens();
            attrTypes = new int [numAttrs];
            attrNames = new String[numAttrs];
            /** initalize variables for storing value info */
            numHighDomain = new double[numAttrs];
            numLowDomain = new double[numAttrs];
            intervals = new double[numAttrs];
            catValues = new ArrayList[numAttrs];
            numValues = new ArrayList[numAttrs];
            /** read attribute info, and initialize appropriate variables*/
            for(int currAttr=0; currAttr<numAttrs; currAttr++)
            {
                int currAttrType = Integer.parseInt(tokens.nextToken());//get attr type
                attrTypes[currAttr]=currAttrType;
                attrNames[currAttr]=tokensNames.nextToken(); //get attr name
                /** check for attribute type */
                if(currAttrType==2)//class attribute
                {
                    classIndex = currAttr;
                    catValues[currAttr] = new ArrayList<String>();
                }
                else if(currAttrType==0)//categorical attribute
                {
                    catValues[currAttr] = new ArrayList<String>();
                }
                else //numerical attribute
                {
                    numValues[currAttr] = new ArrayList<Double>();
                    numHighDomain[currAttr] = Double.NEGATIVE_INFINITY;//defaults
                    numLowDomain[currAttr] = Double.POSITIVE_INFINITY;//defaults
                }
            }
        }
        catch(Exception e)
        {
            retStr="There was a problem extracting data from the attribute info file.\n"+
                    "Please check the file and try again, no name file created.\n"+ e.toString();
            return retStr;
        }
        /**
         * Now we know how many attributes and types, we need to find the ranges and values.
         * For categorical attributes, we are building a list of values. For numerical
         * values, we are trying to find highest and lowest value, and storing all values
         * to an Integer list. Later we will sort these list to determine the minimum interval
         * between values, and the values for categorical.
         */
        for(int currRec=0; currRec<dataStrings.length; currRec++)
        {
            //System.out.println("rec: " + currRec + "dataStrings " + dataStrings.length + "\n" + dataStrings[currRec]);
            /**for each record, we need to tokenize the values for each attribute */

              StringTokenizer tokens = new StringTokenizer(dataStrings[currRec], " ,\t\n\r\f");
            /** for each attribute value, we determine which type */
            for(int currAttr=0; currAttr<numAttrs; currAttr++)
            {
                String currValue = tokens.nextToken();
                //System.out.println("currAttr: " + currAttr + " currValue: " + currValue);
                /** if categorical attribute, or class attribute, just add the value
                 * to the list for that attribute
                 */
                if(isMissing(currValue)==0)
                {
                    if(attrTypes[currAttr]==0 || attrTypes[currAttr]==2)
                    {
                        catValues[currAttr].add(currValue);
                    }
                    /** if numerical, convert the String value to a double, and store to list.
                     * Also check if we have a new high or low value.
                     */
                    else if(attrTypes[currAttr]==1)
                    {
                        double numValue = Double.parseDouble(currValue);
                        double currMax = numHighDomain[currAttr];
                        double currLow = numLowDomain[currAttr];
                        if(numValue>currMax)
                        {
                            numHighDomain[currAttr]=numValue;
                        }
                        if(numValue<currLow)
                        {
                            numLowDomain[currAttr]=numValue;
                        }
                        numValues[currAttr].add(numValue);
                     }
                }
            }
        }

       /** remove duplicate values from each list, and find intervals for numerical
        * values
        */
        List [] allValues = new List[numAttrs];
        for(int currAttr=0; currAttr<numAttrs; currAttr++)
        {

            /** if categorical attribute, or class attribute
             */
            if(attrTypes[currAttr]==0 || attrTypes[currAttr]==2)
            {
                allValues[currAttr] = removeDuplicateValuesString(catValues[currAttr]);
            }
            /** if numerical, convert the String value to a double, and store to list.
             * Also check if we have a new high or low value.
             */
            else if(attrTypes[currAttr]==1)
            {
                allValues[currAttr] = removeDuplicateValuesDouble(numValues[currAttr]);
                /** find intervals */
                intervals[currAttr] = findInterval(allValues[currAttr]);
            }
        }
        /** Now have all info we need to name file, just need to build the output String */
        StringBuilder outStr = new StringBuilder();
        /*<p>Note on format of nameFile</p>
         * <ul>
         *   <li><strong>First line:</strong> class attribute index, number of class values</li>
         *   <li><strong>Second line:</strong> number of records, number of attributes</li>
         *   <li><strong>Categorical attribute:</strong> <code>c</code>, attribute name, number of categories, values</li>
         *   <li><strong>Numerical attribute:</strong> <code>n</code>, attribute name, low domain,
         *       high domain, interval, number of values</li>
         * </ul>
         */
        /** first line */
        int numClasses=0;
        if (classIndex > -1)
         numClasses= allValues[classIndex].size();
        String firstLine = classIndex + ", " + numClasses + ",\n";
        outStr.append(firstLine);
        /** second line */
        String secondLine = dataStrings.length + ", " + numAttrs + ",\n";
        outStr.append(secondLine);
        /** now for each attribute */
        for(int currAttr=0; currAttr<numAttrs; currAttr++)
        {
            StringBuilder currLine= new StringBuilder(); //better for longer strings
            /** categorical or class attribute */
            if(attrTypes[currAttr]==0 || attrTypes[currAttr]==2)
            {
                currLine.append("c, ");
                currLine.append(attrNames[currAttr]); //attr name
                currLine.append(", ");
                List currCatAttr = allValues[currAttr];
                int numCats = currCatAttr.size();
                currLine.append(numCats); //number of categories
                currLine.append(", ");
                /** now print values */
                for(int i=0; i<numCats; i++)
                {
                    currLine.append(currCatAttr.get(i));
                    currLine.append(", ");
                }

            }
            /* numerical */
            else
            {
                currLine.append("n, ");
                currLine.append(attrNames[currAttr]); //attr name
                currLine.append(", ");
                double numVals = ((numHighDomain[currAttr]-numLowDomain[currAttr])/intervals[currAttr])+1;
                int nums = (int) Math.round(numVals);
                /*low domain, high domain, interval, number of values */
                String otherDetails = numLowDomain[currAttr]+ ", "+numHighDomain[currAttr]+ ", "
                        + intervals[currAttr]+ ", " + nums + ",";
                currLine.append(otherDetails);
            }
            currLine.append("\n");//end line
            outStr.append(currLine.toString());
        }
        //write the output string to file
        fileManager.writeToFile(outFile, outStr.toString());

        return retStr;
    }
/**
     * Given a list of {@link String} objects, sorts the list and trims 
     * any duplicate values. Note the returned list will also be ordered.
     *
     * @param unordered an unordered list of String, which may contain
     * duplicates of some values
     * @return an ordered list with only single copies of each value
     */
    public static List<String>
            removeDuplicateValuesString(ArrayList<String> unordered)
    {
  /** convert the list to an array and sort using a default java sort method */
       String [] unorderedArray = new String[unordered.size()];
       unorderedArray = unordered.toArray(unorderedArray);
       Arrays.sort(unorderedArray);
       //note: will sort in natural order for object type
       List <String> ordered = Arrays.asList(unorderedArray);
       //convert back to list
       List <String> trimmed = new ArrayList<String>();
       //add single copies of each value to this list
       /** get first value in list as current */
       String currValue = ordered.get(0);
       trimmed.add(currValue);//add first value to the list
       /** loop and remove duplicate values */
       for(int i=1; i<ordered.size(); i++)
       {
           String nextValue = ordered.get(i);
        /** check if we have a new current value, if so, store it, and add it
            * to the trimmed list
            */
           if(!currValue.equals(nextValue))
           {
               currValue = nextValue;
               trimmed.add(currValue);
           }
       }

       return trimmed; //return as a list
    }
 /**
     * Given a list of {@link Double} objects, sorts the list and trims 
     * any duplicate values. Note the returned list will also be ordered.
     *
     * @param unordered an unordered list of doubles, which may contain
     * duplicates of some values
     * @return an ordered list with only single copies of each value
     */
    public static List<Double>
            removeDuplicateValuesDouble(ArrayList<Double> unordered)
    {
        /** convert the list to an array and sort using
         * a default java sort method */
       Double [] unorderedArray = new Double[unordered.size()];
       unorderedArray = unordered.toArray(unorderedArray);
       Arrays.sort(unorderedArray);
       //note: will sort in natural order for object type
       List <Double> ordered = Arrays.asList(unorderedArray);
       //convert back to list
       List <Double> trimmed = new ArrayList<Double>();
       //add single copies of each value to this list
       /** get first value in list as current */
       Double currValue = ordered.get(0);
       trimmed.add(currValue);//add first value to the list
       /** loop and remove duplicate values */
       for(int i=1; i<ordered.size(); i++)
       {
           Double nextValue = ordered.get(i);
         /** check if we have a new current value, if so, store it, and add it
            * to the trimmed list
            */
           if(!currValue.equals(nextValue))
           {
               currValue = nextValue;
               trimmed.add(currValue);
           }
       }      
       return trimmed; //return as a list
    }

    /**
     * Find the minimum possible interval between two values in the list. If 
     * list size is smaller than 2, return 0 to indicate these is no interval.
     * For numbers that are really ints, find gcd(a,b) amongst all values.
     * For doubles, determine the max number of decimal places.
     *
     * @param values an ordered list of doubles containing no duplicate values
     * @return 0 when list size &lt; 2, smallest interval between
     * values otherwise
     */
    public static double findInterval(List <Double> values)
    {
        /** check we have at least two values */
        if(values.size()<2){return 0.0;} //error flag when empty or of size 1

        /** interval to 1*10^(-number of decimal places)*/
        int numDecs = getMaximumDecimalPlaces(values);
        double interval = Math.pow(10.0, numDecs*-1.0);
        //System.out.println("interval:" + interval +
        //" numDec: " + numDecs + " " + Math.pow(1, numDecs*-1.0));
        return interval;
    }
/**
     * Determine maximum number of decimal places for any value in the list.
     *
     * @param values a list of doubles
     * @return the largest number of places after the decimal for any value
     */
    private static int getMaximumDecimalPlaces(List <Double> values)
    {
        int maxPlaces=0;
        for(int i=0; i<values.size(); i++)
        {
            double origValue = values.get(i);
            /* we can check if the current value can be converted into an int
             * when times by 10^maxPlaces. If not increase maxPlaces
             * and try again
             */
            double multiplier = Math.pow(10.0, maxPlaces);
            double currValue = origValue*multiplier;
            /** round to make sure java is not being silly */
            currValue = roundToDecimals(currValue,1);
            /** if we don't currValue as an int, add 1 to maxPlaces */
           /*
            * Original loop was:  while(Math.floor(currValue)!= currValue)
            * it becomes infinite when I used 4 decimals number for CA data set
            * 
            */

           while(Math.floor(currValue)!= currValue && maxPlaces<10)
            {
                /** NOTE: need to formulate this way due to problem with java
                 * and numbers containing 3s. Should NOT have to round
                 * currValue to 1 decimal
                 */
    //System.out.println("arggh " + Math.abs(Math.floor(currValue)-currValue));
                maxPlaces++;
                multiplier = Math.pow(10.0, maxPlaces);
                currValue = origValue*multiplier;
                currValue = roundToDecimals(currValue,1);
   //System.out.println("currValue: "+ Math.floor(currValue) + " " + currValue);
            }
        }
        return maxPlaces;
    }
    /**
     * Rounds a decimal to the specified number of decimal places.
     *
     * @param value number to be rounded
     * @param positions required decimal places
     * @return value rounded to the required number of decimal places
     */
    public static double roundToDecimals(double value, int positions)
    {
        //add a half so that when we take the floor we get the value rounded 
        //to nearest whole, rather than truncated
        double tempD = (value*Math.pow(10,positions)) + 0.5;
        
        int temp = (int)Math.floor(tempD);
        return ((double)temp)/Math.pow(10, positions);
    }
//this method will normalise a datafile
 public void normaliseFile(String srcFile,String destFile,
         String []attrTypeStr,int sPos, double lb, double ub)
    {
        StringTokenizer oToken;
        String oStr;
        int noR,noA;
        double curVal;
        noA=attrTypeStr.length;
        double []max=new double[noA];
        double []min=new double[noA];
        for(int i=sPos;i<noA;i++)
        {
            max[i]=Double.NEGATIVE_INFINITY;//defaults
            min[i]=Double.POSITIVE_INFINITY;//defaults
        }
        FileManager fileManager = new FileManager();
        File outF=new File(destFile);
        String [] oDataFile = fileManager.readFileAsArray(new File(srcFile));
        noR=oDataFile.length;

        String [][]dbTmp=new String[noR][noA];

        //finding min and max values of each numerical atributes
        for(int i=0;i<noR;i++)
        {
            oToken = new StringTokenizer(oDataFile[i], " ,\t\n\r\f");
            if(sPos==1)
            {
                dbTmp[i][0]=oToken.nextToken();
            }

            for(int j=sPos;j<noA;j++)
            {
                  oStr=oToken.nextToken();
                  dbTmp[i][j]=oStr;
                  if (attrTypeStr[j].equals("n"))
                    {
                        if(isMissing(oStr)==1)
                            curVal=0.0;
                        else
                        {
                            curVal=Double.parseDouble(oStr);
                            if(curVal<min[j])min[j]=curVal;
                            if(curVal>max[j])max[j]=curVal;
                        }
                    }
            }
        }
        //now normalising att numerical attributes
        DecimalFormat df = new DecimalFormat("####0.00000");
        for(int i=0;i<noR;i++)
        {
            String rec="",newStr="";
            if(sPos==1)
            {
                rec=dbTmp[i][0];
            }

            for(int j=sPos;j<noA;j++)
            {
                oStr=dbTmp[i][j];
                if(attrTypeStr[j].equals("n"))
                {
                     if(isMissing(oStr)==1)
                     {
                         newStr=oStr;
                    }
                     else
                     {
                            curVal=Double.parseDouble(oStr);
                     double dnom=max[j]-min[j]+lb;
                     if(dnom!=0)
                     {
                        curVal=((curVal-min[j]+lb)/dnom)*ub;
                     }
                     else
                     {
                         curVal = 0.0;
                         }
                      newStr=""+df.format(curVal);
                    }
                 }
                else
                    newStr=oStr;

                if(sPos==0&&j==sPos)
                    rec=newStr;
                else
                     rec=rec+", "+newStr;
            }
             if(i<noR-1)
                 rec=rec+"\n";
             if(i==0)
                fileManager.writeToFile(outF, rec);
             else
                fileManager.appendToFile(outF, rec);
        }
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
