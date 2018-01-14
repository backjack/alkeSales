package com.alkefp.sales.utils;

import java.util.*;
public class NumberToWord
{
  public static String pw(int n,String ch)
  { 
	String output ="";
    String  one[]={" "," One"," Two"," Three"," Four"," Five"," Six"," Seven"," Eight"," Nine"," Ten"," Eleven"," Twelve"," Thirteen"," Fourteen","Fifteen"," Sixteen"," Seventeen"," Eighteen"," Nineteen"};
 
    String ten[]={" "," "," Twenty"," Thirty"," Forty"," Fifty"," Sixty","Seventy"," Eighty"," Ninety"};
 
    if(n > 19) { 
    	
          output = ten[n/10]+" "+one[n%10];
    			} else {
    				output= one[n];
    				}
    if(n > 0)
    	return output =output +ch;
    return output;
  }
  
  
  
  public static String convertToWord(int value) {
	  String val6 = pw((value/1000000000)," Hundred");
      String val5 = pw((value/10000000)%100," crore");
      String val4 = pw(((value/100000)%100)," lakh");
      String val3 = pw(((value/1000)%100)," thousand");
      String val2 = pw(((value/100)%10)," hundred");
      String val1 = pw((value%100)," ");
      return val6 + val5+ val4+ val3+ val2+ val1;
  }
  
 /* public static void main(String[] args)
  {
    int n=0;
    Scanner scanf = new Scanner(System.in);
    System.out.println("Enter an integer number: ");
    while((n = scanf.nextInt()) > 0) {
    
    
    
    if(n <= 0)   {                
      System.out.println("Enter numbers greater than 0");
   }
   else
   {
	   NumberToWord a = new NumberToWord();
      
    }
    }
  }*/
}