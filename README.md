# FEMI
FEMI (Fuzzy Clustering-based Missing Value Imputation Framework) for data preprocessing.  FEMI imputes numerical and categorical missing values by making an educated guess based on records that are similar to the record having a missing value. While identifying a group of similar records and making a guess based on the group, it applies a fuzzy clustering approach and our novel fuzzy expectation maximization algorithm.

# Reference

Rahman M. G. and Islam M. Z. (2016): Missing Value Imputation using a Fuzzy Clustering based EM Approach, Knowledge and Information Systems, Vol. 46 (2), pp. 389 – 422. DOI: 10.1007/s10115-015-0822-y. 
 
@author Md Geaur Rahman <https://csusap.csu.edu.au/~grahman/>
  
# Two folders:
 
 1. FEMI_Master (NetBeans project)
 2. SampleData 
 
 FEMI is developed based on Java programming language (jdk1.8.0_211) using NetBeans IDE (8.0.2). 
 
# How to run:
 
	1. Open project in NetBeans
	2. Run the project

# Sample input and output:
run:
Please enter the name of the file containing the 2 line attribute information.(example: c:\data\attrinfo.txt)

C:\Gea\Research\FEMI\SampleData\attrinfo.txt

Please enter the name of the data file having missing values: (example: c:\data\data.txt)

C:\Gea\Research\FEMI\SampleData\data.txt

Please enter the name of the output file: (example: c:\data\out.txt)

C:\Gea\Research\FEMI\SampleData\output.txt


Imputation by FEMI is done. The completed data set is written to: 

C:\Gea\Research\FEMI\SampleData\output.txt

