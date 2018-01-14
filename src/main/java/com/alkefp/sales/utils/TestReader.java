package com.alkefp.sales.utils;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class TestReader {

	/**
	 * @param args
	 */
	
	
	public void readTotal()throws Exception {
		
		File folder = new File("C:\\logs\\docs");
		File[] listOfFiles = folder.listFiles();
		
		for(File f: listOfFiles) {
			if(f.isFile() && f.getName().endsWith(".doc")) {
		FileInputStream in = new FileInputStream(f);
	    HWPFDocument document = new HWPFDocument(in);
	    WordExtractor extractor = new WordExtractor(document);
	    String  val= extractor.getText();
	    
	     
	    
	     int index4 = val.indexOf("AFP/INV/");
	     String val3 = val.substring(index4, (index4+12));
	  
	     int index1 = val.indexOf("Grand Total");
	     int index2 = val.indexOf("Total In Words");
	     if(index2 ==-1) {
	         index2 = val.indexOf("In Words");
	     }
	     String val1 = val.substring(index1, index2);
	     val1 = val1.replaceAll("Grand Total","");
	     val1 = val1.replaceAll("/\r?\n|\r/","");
	     val1 = val1.replaceAll("\\[a-z,A-Z]+","");
	     val1 = val1.replaceAll(",", "");
	     val1= val1.replaceAll(" ", "");
	     double value = Double.valueOf(val1);

	     
	     
	      int index = val.indexOf("To,");
		  int lastIndex = val.indexOf("Invoice No");
		  String clientInfo =  val.substring(index+3, lastIndex);
		  clientInfo = clientInfo.trim();
		  String client = clientInfo.split(",")[0];
	     
		  
		   index1 = val.indexOf("Dated");
		   lastIndex = val.indexOf("Invoice No");
		  String invoiceDate =  val.substring(index1, index1+16);
		  invoiceDate= invoiceDate.replace("Dated", "");
		 
	     
	     System.out.println(val3+","+value+","+client+","+invoiceDate);
/*	     System.out.println(value);
	     System.out.println(client);*/
	     in.close();
			}
		}
	}
	
	public void readFiles() throws Exception{
		
		File folder = new File("C:\\logs\\docs");
		File[] listOfFiles = folder.listFiles();
		
		for(File f: listOfFiles) {
			
			if(f.isFile() && f.getName().endsWith(".docx")) {
				FileInputStream in = new FileInputStream(f);
				  XWPFDocument document = new XWPFDocument(in);
				  
				  for(int i=0;i<1;i++) {
					  XWPFParagraph para = document.getParagraphArray(i);
		
					  String val = para.getText();
					//  val = val.replaceAll("\\n","|");
					  int index = val.indexOf("TAX INVOICE To,");
					  int lastIndex = val.indexOf("Invoice No");
					   val =  val.substring(index+17, lastIndex);
					   val= val.trim();
					   val = val.replaceFirst("\\n","|");
					   val = val.replaceAll("\\n","");
					  System.out.println(val);
					  System.out.println("--------------------------");
		        }
				  
				  in.close();
				  
		  } else if(f.isFile() && f.getName().endsWith(".doc")) {
			 
			   FileInputStream in = new FileInputStream(f);
		       HWPFDocument document = new HWPFDocument(in);
		        WordExtractor extractor = new WordExtractor(document);
		        String val = extractor.getText();
		      //  System.out.println(val);
		        val = val.replaceAll("\\s+\\n",",");
		        val = val.replaceAll(",\\s+",",");
		        int index = val.indexOf("To,");
		        
				  int lastIndex = val.indexOf("Invoice No");
				   val =  val.substring(index+3, lastIndex);
				   val= val.trim();
				  // val = val.replaceFirst("\\n","|");
				   val = val.replaceAll("\\n","");
				   val = val.replaceFirst(",","");
				   val = val.replaceFirst(",","|");
		        System.out.println(val);
		        System.out.println("----------#####----------------");
		       
		        in.close();
		  }
		}
	}
	
	
	/*public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		new TestReader().readTotal();
	}
*/
}
