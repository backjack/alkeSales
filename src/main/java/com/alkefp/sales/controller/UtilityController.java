package com.alkefp.sales.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alkefp.sales.beans.User;
import com.alkefp.sales.dao.SummaryDao;
import com.alkefp.sales.vo.MonthlySales;


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
       startCal.set(2018,month, 1);
       
       Calendar endCal = Calendar.getInstance();
       endCal.set(2018,month, 31);
    
	   List<MonthlySales> sales = summaryDao.getMonthlySales(startCal.getTime(), endCal.getTime(), year);
	 
	   return sales;
	}
}