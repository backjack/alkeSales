package com.alkefp.sales.controller;

import com.alkefp.sales.beans.User;
import com.alkefp.sales.dao.SummaryDao;
import com.alkefp.sales.vo.MonthlySales;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/utility")
public class UtilityController {


   @Autowired
   private SummaryDao summaryDao;
	
   private User getUser() {
		
		UserDetails userDetails =
				 (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<String,User> userMap = summaryDao.getUserMap();
		String userName = userDetails.getUsername();
		User user = userMap.get(userName);
		return user;
		
	}
	
   @RequestMapping(value = "/monthly/view/{year}/{month}", method = RequestMethod.GET,produces = "application/json")
	public List<MonthlySales> getMontlySales(@PathVariable("year") int year,@PathVariable("month") int month) {
		
	   User user = getUser();
	   Calendar startCal = Calendar.getInstance();

	   if(month<3) {
	   	year = year+1;
	   }
       startCal.set(year,month, 1);
       
       Calendar endCal = Calendar.getInstance();
       endCal.set(year,month, 31);
    
	   List<MonthlySales> sales = summaryDao.getMonthlySales(startCal.getTime(), endCal.getTime(), year);
	 
	   return sales;
	}
   
    @RequestMapping(value = "/monthly/taxes/{year}/{month}", method = RequestMethod.GET,produces = "application/json")
	public Map<String,Long> getMontlyTaxes(@PathVariable("year") int year,@PathVariable("month") int month) {
		
	   User user = getUser();
	   Calendar startCal = Calendar.getInstance();
		if(month<3) {
			year = year+1;
		}
       startCal.set(year,month, 1);
       
       Calendar endCal = Calendar.getInstance();
       endCal.set(year,month, 31);
	   Map<String,Long> taxes = summaryDao.getMonthlyTaxes(startCal.getTime(), endCal.getTime(), year,user.getGroupId());
	 
	   return taxes;
	}
}
