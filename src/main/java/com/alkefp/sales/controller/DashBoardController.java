package com.alkefp.sales.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alkefp.sales.beans.BaseResponse;
import com.alkefp.sales.beans.Pair;
import com.alkefp.sales.beans.PartPayment;
import com.alkefp.sales.beans.SaleSummary;
import com.alkefp.sales.beans.SaleSummaryInput;
import com.alkefp.sales.beans.User;
import com.alkefp.sales.beans.query.PartPaymentQuery;
import com.alkefp.sales.dao.SummaryDao;
import com.alkefp.sales.helper.XlsBuilder;


@RestController
@RequestMapping("/home")
public class DashBoardController {

	@Autowired
	private SummaryDao summaryDao;
	
	@Autowired
	private XlsBuilder xlsBuilder;
	
	
	
	private User getUser() {
		
		UserDetails userDetails =
				 (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<String,User> userMap = summaryDao.getUserMap();
		String userName = userDetails.getUsername();
		User user = userMap.get(userName);
		return user;
		
	}
	
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public BaseResponse<User> getUserDetail() {
		
		BaseResponse<User> baseResponse = new BaseResponse<>();
		User user = getUser();
		baseResponse.setData(user);
		baseResponse.setSuccess(true);
		return baseResponse;
	}
	
	@RequestMapping(value = "/summary", method = RequestMethod.POST)
	public BaseResponse<List<SaleSummary>> getSaleSummary(@RequestBody SaleSummaryInput input) {
		
		User user =  getUser();
		List<Integer> years = input.getYears();
		List<Integer> clientIds = input.getClientids();
		List<SaleSummary> saleSummary = summaryDao.getSummaryView(years,user.getGroupId(),clientIds);
		BaseResponse<List<SaleSummary>> summaryResponse = new BaseResponse<>();
		summaryResponse.setData(saleSummary);
		summaryResponse.setSuccess(true);
		return summaryResponse;
	}
	
	@RequestMapping(value = "/summary/xls", method = RequestMethod.POST,produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	public void downloadInvoice( @RequestBody ArrayList<Integer> fyYears,
	        HttpServletResponse response) {
		
		 User user = getUser();
		try {
			 response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	         response.addHeader("Content-Disposition", "attachment; filename=\"summary_invoice.xlsx\"");
	         xlsBuilder.downloadXls(user.getGroupId(),fyYears,response);
	         response.getOutputStream().flush();
	         response.getOutputStream().close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	
	
	@RequestMapping(value = "/partpayment", method = RequestMethod.POST)

	public BaseResponse<List<PartPayment>> getPartPayment(@RequestBody PartPaymentQuery partPaymentQuery) {
		
		User user =  getUser();
		System.out.println("!!!!!!!!!!!!!!1"+partPaymentQuery.getInvoiceId());
		System.out.println("@@@@@@@@@@555@@@@"+partPaymentQuery.getFyYear());
		List<PartPayment> partPayments = summaryDao.getPartPayment(partPaymentQuery.getInvoiceId(), partPaymentQuery.getFyYear(),user.getGroupId());
		BaseResponse<List<PartPayment>> partPaymentResponse = new BaseResponse<>();
		partPaymentResponse.setData(partPayments);
		partPaymentResponse.setSuccess(true);
		return partPaymentResponse;
	}
	
	
	@RequestMapping(value = "/add/invoice", method = RequestMethod.POST)

	public BaseResponse<String> addInvoice(@RequestBody ArrayList<SaleSummary> saleSummary) {
		
		User user =  getUser();
		summaryDao.insertInvoice(saleSummary.get(0),user.getGroupId());
		BaseResponse<String> addPartPaymentRes = new BaseResponse<>();
		addPartPaymentRes.setData("success");
		addPartPaymentRes.setSuccess(true);
		return addPartPaymentRes;
	}
	
	
	@RequestMapping(value = "/invoice/delete", method = RequestMethod.POST)

	public BaseResponse<String> removeInvoice(@RequestBody SaleSummary saleSummary) {
		
		User user =  getUser();
		summaryDao.removeInvoice(saleSummary,user.getGroupId());
		BaseResponse<String> addPartPaymentRes = new BaseResponse<>();
		addPartPaymentRes.setData("success");
		addPartPaymentRes.setSuccess(true);
		return addPartPaymentRes;
	}
	
	
	@RequestMapping(value = "/overview", method = RequestMethod.POST)

	public BaseResponse<Map<String,Object>> getOverview(@RequestBody SaleSummaryInput input) {
		
		User user =  getUser();
		int fyyear = input.getYears().get(0);
		List<Integer> clientids = input.getClientids();
		
		Map<String,Object> overview = null;

		if(clientids != null && clientids.size()>0 && clientids.get(0)!=null && (clientids.get(0)>0)) {
			overview = summaryDao.getOverviewByClient(fyyear,user.getGroupId(),clientids);
		} else {
			overview = summaryDao.getOverview(fyyear,user.getGroupId());
		}

		BaseResponse<Map<String,Object>> addPartPaymentRes = new BaseResponse<>();
		addPartPaymentRes.setData(overview);
		addPartPaymentRes.setSuccess(true);
		return addPartPaymentRes;
	}
	
	
	@RequestMapping(value = "/update/invoice", method = RequestMethod.POST)

	public BaseResponse<String> updateInvoice(@RequestBody SaleSummary saleSummary) {
		
		User user =  getUser();
		summaryDao.updateInvoice(saleSummary,user.getGroupId());
		BaseResponse<String> addPartPaymentRes = new BaseResponse<>();
		addPartPaymentRes.setData("success");
		addPartPaymentRes.setSuccess(true);
		return addPartPaymentRes;
	}
	
	@RequestMapping(value = "/add/partpayment", method = RequestMethod.POST)

	public BaseResponse<String> insertPartPayment(@RequestBody ArrayList<PartPayment> partPayments) {
		
		User user =  getUser();
		summaryDao.insertPartPayment(partPayments,user.getGroupId());
		BaseResponse<String> addPartPaymentRes = new BaseResponse<>();
		addPartPaymentRes.setData("success");
		addPartPaymentRes.setSuccess(true);
		return addPartPaymentRes;
	}
	
	
	
	@RequestMapping(value="/clients",method =RequestMethod.GET)
	public BaseResponse<List<Pair<Integer,String>>> getClients() {
		
		User user =  getUser();
		BaseResponse<List<Pair<Integer,String>>> clientsResponse = new BaseResponse<>();
		List<Pair<Integer,String>> pairs = summaryDao.getClients(user.getGroupId());
		Collections.sort(pairs, new Comparator<Pair<Integer,String>>(){

			@Override
			public int compare(Pair<Integer, String> s1,
					Pair<Integer, String> s2) {
				
				return s1.getValue().compareTo(s2.getValue());
			}});
		clientsResponse.setData(pairs);
		clientsResponse.setSuccess(true);
		return clientsResponse;
	}
	
	@RequestMapping(value="/fy",method =RequestMethod.GET)
	public BaseResponse<List<Pair<Integer,String>>> getFinancialYear() {
		
		System.out.println("getting fyyear");
		List<Pair<Integer,String>> fy = summaryDao.getFYYear();
		BaseResponse<List<Pair<Integer,String>>> fyResponse = new BaseResponse<>();
		fyResponse.setData(fy);
		fyResponse.setSuccess(true);
		return fyResponse;
	}
	
	
	@RequestMapping(value = "/monthly/xls/{year}/{month}", method = RequestMethod.POST,produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	public void downloadMonhtlySales(@PathVariable("year") int year,@PathVariable("month") int month, HttpServletResponse response) {
		
		// User user = getUser();
		try {
			 response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	         response.addHeader("Content-Disposition", "attachment; filename=\"monthysales.xlsx\"");
	         Calendar startCal = Calendar.getInstance();
	         startCal.set(2018,month, 1);
	         
	         Calendar endCal = Calendar.getInstance();
	         endCal.set(2018,month, 31);
	      
	         String monthText = new SimpleDateFormat("MMM").format(startCal.getTime());
	         
	         xlsBuilder.buildMonthlySales(startCal.getTime(), endCal.getTime(), year,monthText, response);
	         response.getOutputStream().flush();
	         response.getOutputStream().close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
		
}
