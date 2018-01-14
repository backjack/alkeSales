package com.alkefp.sales.controller;

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
import com.alkefp.sales.beans.InvoiceDetail;
import com.alkefp.sales.beans.Item;
import com.alkefp.sales.beans.User;
import com.alkefp.sales.dao.InvoiceDetailDao;
import com.alkefp.sales.dao.SummaryDao;
import com.alkefp.sales.helper.InvoiceBuilder;

@RestController
@RequestMapping("/invoice")	
public class InvoiceItemController {


@Autowired
private SummaryDao summaryDao;


@Autowired
private InvoiceBuilder invoiceBuilder;

@Autowired
private InvoiceDetailDao invoiceDetailDao;

private User getUser() {
		
		UserDetails userDetails =
				 (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<String,User> userMap = summaryDao.getUserMap();
		String userName = userDetails.getUsername();
		User user = userMap.get(userName);
		return user;
}



@RequestMapping(value = "/item/detail", method = RequestMethod.POST)
public BaseResponse<InvoiceDetail> getInvoiceDetail(@RequestBody InvoiceDetail invoiceDetailReq) {
	
	User user = getUser();

	String invoiceId  = invoiceDetailReq.getInvoiceId();
	int year = invoiceDetailReq.getFyear();
	InvoiceDetail invoiceDetail = invoiceDetailDao.getFullInvoiceDetail(invoiceId,year,user.getGroupId());
	BaseResponse<InvoiceDetail> invoiceDetailVO = new BaseResponse<>();
	invoiceDetailVO.setData(invoiceDetail);
	invoiceDetailVO.setSuccess(true);
	return invoiceDetailVO;
}


@RequestMapping(value = "/detail/add", method = RequestMethod.POST)
public BaseResponse<InvoiceDetail> addInvoiceDeatil(@RequestBody InvoiceDetail invoiceDetail) {
	
	User user = getUser();
	invoiceDetail.setGroupId(user.getGroupId());
	if(invoiceDetailDao.getInvoiceDetail(invoiceDetail)==0) {
	   invoiceDetailDao.addInvoiceDetail(invoiceDetail);
	}else {
	   invoiceDetailDao.updateInvoiceDetail(invoiceDetail);
	}
	/*invoiceDetailDao.addInvoiceItem(invoiceDetail.getItems(), invoiceDetail.getInvoiceId(),
			invoiceDetail.getFyear(), invoiceDetail.getGroupId());*/
		
	BaseResponse<InvoiceDetail> invoiceDetailVO = new BaseResponse<>();
	invoiceDetailVO.setData(invoiceDetail);
	invoiceDetailVO.setSuccess(true);
	return invoiceDetailVO;
}

@RequestMapping(value = "/detail/download/{type}", method = RequestMethod.POST,produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
public void downloadInvoice( @RequestBody InvoiceDetail invoiceDetail,@PathVariable Integer type,
        HttpServletResponse response) {
	
	 User user = getUser();
	invoiceDetail.setGroupId(user.getGroupId());

	try {

		System.out.println("#$%%%% type"+ type);
		 response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
         response.addHeader("Content-Disposition", "attachment; filename=\"invoice.docx\"");
         invoiceBuilder.buildInvoice(invoiceDetail,response,type);
         response.getOutputStream().flush();
         response.getOutputStream().close();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
}

@RequestMapping(value = "/item/add", method = RequestMethod.POST)
public BaseResponse<Boolean> addInvoiceItem(@RequestBody List<Item> items) {
	
	User user = getUser();
	invoiceDetailDao.addInvoiceItem(items, user.getGroupId());
	BaseResponse<Boolean> baseResponse = new BaseResponse<>();
	baseResponse.setData(true);
	baseResponse.setSuccess(true);
	return baseResponse;
}


@RequestMapping(value = "/item/update", method = RequestMethod.POST)
public BaseResponse<Boolean> updateInvoiceItem(@RequestBody List<Item> items) {
	
	User user = getUser();
	invoiceDetailDao.updateInvoiceItem(items, user.getGroupId());
	BaseResponse<Boolean> baseResponse = new BaseResponse<>();
	baseResponse.setData(true);
	baseResponse.setSuccess(true);
	return baseResponse;
}

@RequestMapping(value = "/item/delete", method = RequestMethod.POST)
public BaseResponse<Boolean> deleteInvoiceItem(@RequestBody Item item) {
	
	User user = getUser();
	item.setGroupId(user.getGroupId());
	invoiceDetailDao.deleteInvoiceItem(item);
	BaseResponse<Boolean> baseResponse = new BaseResponse<>();
	baseResponse.setData(true);
	baseResponse.setSuccess(true);
	return baseResponse;
}




 
}
