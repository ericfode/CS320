package com.efode.externalSort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

public class ExternalSort
{
	public static void main(String[] args)
	{
		externalSort("data");
	}
	private static void externalSort(String file)
	{
	     try
	     {
	         FileReader input = new FileReader(file + ".txt"); 
	         BufferedReader inputReader = new BufferedReader(input);
	         ArrayList<Integer> tenKInts = new ArrayList<Integer>();
	         int numFiles = 0;
	         String row = inputReader.readLine();
	         //while there is more rows
	         while (row !=null)
	         {
	             // get 100m rows (about 500mb)
	             for(int i=0; i<100000000; i++)
	             {
	            	 //this could be optimized by reading chunks into a buffer and 
	            	 //then converting them to ints in mass in anouther thread
	            	 row = inputReader.readLine();
	            	 if(row == null)
	            	 {
	            		 break;
	            	 }
	                 tenKInts.add(Integer.parseInt(row)); 
	             }
	             //When the when the ints holder is full sort it with the built in sort, this will be faster
	             //then a sort we could write by hand (at least that is how it is in c++)
	             Collections.sort(tenKInts);
	             
	             // write to disk with the chunck of ints that we just sorted
	             FileWriter fw = new FileWriter(file + "_chunk" + numFiles + ".txt");
	             BufferedWriter bw = new BufferedWriter(fw);	        
	             for(int i=0; i<tenKInts.size(); i++)
	             {
	            	 //All of these conversions from string to int and back are expensive, a biniary file would be better
	                 bw.append(tenKInts.get(i).toString() +"\n");
	             }
	             bw.close();
	             //tally the number of files that will need to be mereged
	             numFiles++;
	             //clear the buffer of ints so it can be filled again
	             tenKInts.clear();
	         }
	     
	         //incermentaly merge the files together
	         mergeFiles(file, numFiles);
	         
	         //clean up
	         inputReader.close();
	         input.close();
	         
	     }
	     catch (Exception ex)
	     {
	         ex.printStackTrace();
	         System.exit(-1);
	     }
	     
	     
	}

	private static void mergeFiles(String file, int numFiles)
	{
	     try
	     {
	    	 //Prepare arrays to hold the buffer readers for each file
	         ArrayList<FileReader> mergefr = new ArrayList<FileReader>();
	         ArrayList<BufferedReader> mergefbr = new ArrayList<BufferedReader>();
	         
	         //Prepare a list to be used as a buffer for the mereged ints
	         ArrayList<Integer> ints = new ArrayList<Integer>(); 
	         
	         //prepare the filewriter for the merged chucnks
	         FileWriter fw = new FileWriter(file + "_sorted.txt");
	         BufferedWriter bw = new BufferedWriter(fw);
	      
	         
	         boolean someFileStillHasRows = false;
	         
	         //For all of the files
	         for (int i=0; i<numFiles; i++)
	         {
	        	 //add a file reader and buffer to their respective lists
	             mergefr.add(new FileReader(file+"_chunk"+i+".txt"));
	             mergefbr.add(new BufferedReader(mergefr.get(i)));

	             // get a row
	             // this again could be optimised by taking care of the merging and conversions in anouther
	             // threard assuming that shared memorie was being used
	             String line = mergefbr.get(i).readLine();
	             if (line != null)
	             {
	                 ints.add(Integer.parseInt(line));
	                 someFileStillHasRows = true;
	             }
	             else 
	             {
	                 ints.add(null);
	             }
	                 
	         }
	         
	         Integer item = 0;
	         int cnt = 0;
	         //as long as there are more ints somewyhere
	         while (someFileStillHasRows)
	         {
	             Integer min;
	             int minIndex = 0;
	             
	             //get the first int to start the merge
	             item = ints.get(0);
	             if (item!=null) {
	                 min = item;
	                 minIndex = 0;
	             }
	             else {
	                 min = null;
	                 minIndex = -1;
	             }
	             
	             // check which one is min so that it can be written to a file
	             for(int i=1; i<ints.size(); i++)
	             {
	                 item = ints.get(i);
	                 if (min!=null) {
	                     
	                     if(item!=null && item < min)
	                     {
	                         minIndex = i;
	                         min = ints.get(i);
	                     }
	                 }
	                 else
	                 {
	                     if(item!=null)
	                     {
	                         min = item;
	                         minIndex = i;
	                     }
	                 }
	             }
	             //If minIndex is greater then 0 
	             if (minIndex < 0) {
	                 someFileStillHasRows=false;
	             }
	             else
	             {
	                 // write to the sorted file
	            	 
	                 bw.append(ints.get(minIndex)+"\n");
	                 
	                 // get another row from the file that had the min to keep the buffer full
	                 String line = mergefbr.get(minIndex).readLine();
	                 if (line != null)
	                 {
	                     ints.set(minIndex,Integer.parseInt(line));
	                 }
	                 else 
	                 {
	                     ints.set(minIndex,null);
	                 }
	             }                       
	             
	             // check if one still has rows
	             for(int i=0; i<ints.size(); i++)
	             {
	                 
	                 someFileStillHasRows = false;
	                 if(ints.get(i)!=null) 
	                 {
	                     if (minIndex < 0) 
	                     {
	                         System.out.println("For some reason the minIndex is way to small exiting");
	                         System.exit(-1);
	                     }
	                     someFileStillHasRows = true;
	                     break;
	                 }
	             }
	             
	             // check the actual files one more time
	             if (!someFileStillHasRows)
	             {
	                 
	                 //write the last one not covered above
	                 for(int i=0; i<ints.size(); i++)
	                 {
	                     if (ints.get(i) == null)
	                     {
	                         String line = mergefbr.get(i).readLine();
	                         if (line!=null) 
	                         {
	                             
	                             someFileStillHasRows=true;
	                             ints.set(i,Integer.parseInt(line));
	                         }
	                     }
	                             
	                 }
	             }
	                 
	         }

	         // close all the files
	         bw.close();
	         fw.close();
	         for(int i=0; i<mergefbr.size(); i++)
	             mergefbr.get(i).close();
	         for(int i=0; i<mergefr.size(); i++)
	             mergefr.get(i).close();
	         
	         
	         
	     }
	     catch (Exception ex)
	     {
	         ex.printStackTrace();
	         System.exit(-1);
	     }
	}

}
    