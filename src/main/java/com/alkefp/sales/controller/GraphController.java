package com.alkefp.sales.controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alkefp.sales.beans.BaseResponse;
import com.alkefp.sales.beans.User;
import com.alkefp.sales.beans.graph.ClientSales;
import com.alkefp.sales.dao.GraphDao;
import com.alkefp.sales.dao.SummaryDao;
import com.alkefp.sales.vo.ClientSalesDataSetVO;
import com.alkefp.sales.vo.ClientSalesVO;
import com.alkefp.sales.vo.SalesByMonthDataSetVO;
import com.alkefp.sales.vo.SalesByMonthVO;

@RestController
@RequestMapping("/graph")
public class GraphController {

	@Autowired
	private GraphDao graphDao;
	

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
	
	@RequestMapping(value = "/sales/client/{fyyear}", method = RequestMethod.GET)
	public BaseResponse<ClientSalesVO> getSalesByClient(@PathVariable("fyyear") int year) {
		
		User user = getUser();
		List<ClientSales> clientSales = graphDao.getClientSales(year,user.getGroupId());
		ClientSalesVO csvo = convertTo(clientSales);
		BaseResponse<ClientSalesVO> clientSalesVo = new BaseResponse<>();
		clientSalesVo.setData(csvo);
		clientSalesVo.setSuccess(true);
		return clientSalesVo;
	}
	
	@RequestMapping(value = "/sales/month/{fyyear}", method = RequestMethod.GET)
	public BaseResponse<SalesByMonthVO> getSalesByMonths(@PathVariable("fyyear") int year) {
		
		User user = getUser();
		List<SalesByMonthDataSetVO> salesByMonth = graphDao.getSalesByMonth(year,user.getGroupId());
		SalesByMonthVO smvo = new SalesByMonthVO();
		smvo.setDataSets(salesByMonth);
		BaseResponse<SalesByMonthVO> clientSalesVo = new BaseResponse<>();
		clientSalesVo.setData(smvo);
		clientSalesVo.setSuccess(true);
		return clientSalesVo;
	}


	private ClientSalesVO convertTo(List<ClientSales> clientSales) {
		
		ClientSalesVO vo = new ClientSalesVO();
		List<String> clientName = new ArrayList<>();
		List<Double> billAmt = new ArrayList<>();
		List<Double> payAmt = new ArrayList<>();
		List<Double> remainingAmt = new ArrayList<>();
		vo.setClientName(clientName);
		
		for(ClientSales cs :clientSales) {
			clientName.add(cs.getClientName());
			billAmt.add(cs.getBillAmt());
			payAmt.add(cs.getPartPayAmt());
			remainingAmt.add(cs.getRemainingPayAmt());
		}
		
		List<ClientSalesDataSetVO> clientSalesDS = new ArrayList<>();
		ClientSalesDataSetVO billAmtDS = new ClientSalesDataSetVO();
		billAmtDS.setData(billAmt);
		billAmtDS.setLabel("Total Sale");
		billAmtDS.setBgColor("rgba(54, 162, 235, 0.2)");
		billAmtDS.setBorderColor("rgba(54, 162, 235, 1)");
		clientSalesDS.add(billAmtDS);
		
		ClientSalesDataSetVO payAmtDS = new ClientSalesDataSetVO();
		payAmtDS.setData(payAmt);
		payAmtDS.setLabel("Pay Received");
		payAmtDS.setBgColor("rgba(75, 192, 192, 1)");
		payAmtDS.setBorderColor("rgba(75, 192, 192, 1)");
		clientSalesDS.add(payAmtDS);
		
		ClientSalesDataSetVO remainingAmtDS = new ClientSalesDataSetVO();
		remainingAmtDS.setData(remainingAmt);
		remainingAmtDS.setLabel("Remaining Payment");
		remainingAmtDS.setBgColor("rgba(255, 99, 132, 0.2)");
		remainingAmtDS.setBorderColor("rgba(255,99,132,1)");
		clientSalesDS.add(remainingAmtDS);
		vo.setDataSets(clientSalesDS);
		
		return vo;
	}
	
	/*
	 * 
	 * select  a.fmonth,a.fyYear,part, bill from(
(select month(payDate) as fmonth,fyYear,sum(partPymt) as part from partPayment p group by month(payDate),fyYear) 
a   join 
(select month(invoiceDate) as fmonth, fyYear,sum(billAmt) as bill from sales p group by month(invoiceDate),fyYear) s 
on a.fmonth = s.fmonth and a.fyYear=s.fyYear)
	 * 
	 * 
	 */
	private List<SalesByMonthDataSetVO> getSalesByMonth() {
		
		List<SalesByMonthDataSetVO> dataSet = new ArrayList<>();
		
		for(int i=0;i<2;i++) {
			SalesByMonthDataSetVO salesByMonthDataSetVO = new SalesByMonthDataSetVO();
			List<Double> data = new ArrayList<>();
			for(int j=0;j<12;j++) {
				double rand = Math.random();
				rand = rand*100000;
				DecimalFormat df = new DecimalFormat("#.##");
				data.add(rand);
			}
			salesByMonthDataSetVO.setData(data);
			salesByMonthDataSetVO.setLabel("profit");
			dataSet.add(salesByMonthDataSetVO);
		}
		
		return dataSet;
		
	}
	
}
