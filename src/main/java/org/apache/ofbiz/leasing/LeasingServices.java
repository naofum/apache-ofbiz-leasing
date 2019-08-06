/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.leasing;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//import javax.transaction.Transaction;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilNumber;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.security.Security;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.poi.ss.formula.functions.FinanceLib;

//import com.ibm.icu.util.Calendar;

/**
 * Order Processing Services
 */

/**
 * @author
 *
 */
public class LeasingServices {

    public static final String module = LeasingServices.class.getName();
    public static final String resource = "LeasingUiLabels";
    public static final String resource_error = "LeasingErrorUiLabels";
    public static final String resourceProduct = "ProductUiLabels";
    public static final String resourceOrder = "OrderUiLabels";

    public static final int taxDecimals = UtilNumber.getBigDecimalScale("salestax.calc.decimals");
    public static final int taxRounding = UtilNumber.getBigDecimalRoundingMode("salestax.rounding");
    public static final int orderDecimals = UtilNumber.getBigDecimalScale("order.decimals");
    public static final int orderRounding = UtilNumber.getBigDecimalRoundingMode("order.rounding");
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(taxDecimals, taxRounding);

    private static boolean hasPermission(GenericValue userLogin, String action, Security security) {
        boolean hasPermission = security.hasEntityPermission("ORDERMGR", "_" + action, userLogin);
        return hasPermission;
    }

    /** Service for getting quote payment */
    public static Map<String, Object> getQuotePayment(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Security security = ctx.getSecurity();
        List<GenericValue> toBeStored = new LinkedList<GenericValue>();
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> successResult = ServiceUtil.returnSuccess();

        String quoteId = (String) context.get("quoteId");

        List<GenericValue> payments = new LinkedList<GenericValue>();
    	GenericValue paymentDay = null;
    	GenericValue paymentAmount = null;
    	GenericValue paymentTimes = null;
    	GenericValue paymentRate = null;
    	GenericValue leaseRate = null;
        try {
            payments = EntityQuery.use(delegator).from("QuotePayment").where("quoteId", quoteId).orderBy("quotePaymentId").queryList();
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }
        try {
        	paymentDay = EntityQuery.use(delegator).from("QuoteTerm").where("termTypeId", "LSE_PAY_DAY_1", "quoteId", quoteId, "quoteItemSeqId", "_NA_").queryOne();
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }
        try {
        	paymentAmount = EntityQuery.use(delegator).from("QuoteTerm").where("termTypeId", "LSE_PAY_AMOUNT_1", "quoteId", quoteId, "quoteItemSeqId", "_NA_").queryOne();
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }
        try {
        	paymentTimes = EntityQuery.use(delegator).from("QuoteTerm").where("termTypeId", "LSE_PAY_TIMES_1", "quoteId", quoteId, "quoteItemSeqId", "_NA_").queryOne();
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }
        try {
        	paymentRate = EntityQuery.use(delegator).from("QuoteTerm").where("termTypeId", "LSE_PAY_RATE", "quoteId", quoteId, "quoteItemSeqId", "_NA_").queryOne();
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }
        try {
        	leaseRate = EntityQuery.use(delegator).from("QuoteTerm").where("termTypeId", "LSE_LEASE_RATE_TERM", "quoteId", quoteId, "quoteItemSeqId", "_NA_").queryOne();
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }

        List<Object> lists = new LinkedList<Object>();
        for (GenericValue payment : payments) {
             List<Object> list = new LinkedList<Object>();
        	list.add(payment.getString("quotePaymentId"));
        	list.add(payment.getTimestamp("paymentDate").getTime());
        	list.add(payment.getBigDecimal("paymentAmount"));
        	list.add(payment.getBigDecimal("principal"));
        	list.add(payment.getBigDecimal("interest"));
        	list.add(payment.getBigDecimal("remainValue"));
        	lists.add(list);
        }
        BigDecimal totalPrice = getPrice(delegator, quoteId, locale);
        successResult.put("lists", lists);
        successResult.put("price", totalPrice.toString());
        if (paymentDay == null || paymentDay.getString("textValue") == null) {
            successResult.put("paymentDay", "");
        } else {
            successResult.put("paymentDay", paymentDay.getString("textValue"));
        }
        if (paymentAmount == null || paymentAmount.getLong("termValue") == null) {
            successResult.put("paymentAmount", "");
        } else {
            successResult.put("paymentAmount", paymentAmount.getLong("termValue").toString());
        }
        if (paymentTimes == null || paymentTimes.getLong("termValue") == null) {
            successResult.put("paymentTimes", "");
        } else {
            successResult.put("paymentTimes", paymentTimes.getLong("termValue").toString());
        }
        if (paymentRate == null || paymentRate.getString("textValue") == null) {
            successResult.put("paymentRate", "");
        } else {
            successResult.put("paymentRate", paymentRate.getString("textValue"));
        }
        if (leaseRate == null || leaseRate.getString("textValue") == null) {
            successResult.put("leaseRate", "");
        } else {
            successResult.put("leaseRate", leaseRate.getString("textValue"));
        }

        return successResult;
    }

    /** Service for creating and updating quote payment */
    public static Map<String, Object> createQuotePayment(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Security security = ctx.getSecurity();
        List<GenericValue> toBeStored = new LinkedList<GenericValue>();
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> successResult = ServiceUtil.returnSuccess();

        GenericValue userLogin = (GenericValue) context.get("userLogin");

        Map<String, Object> resultSecurity = new HashMap<String, Object>();
        boolean hasPermission = LeasingServices.hasPermission(userLogin, "CREATE", security);
        if (!hasPermission) {
            return resultSecurity;
        }
        String quoteId = (String) context.get("quoteId");
        LocalDate localdate = LocalDate.parse((String) context.get("paymentDay"), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDateTime datetime = LocalDateTime.of(localdate, LocalTime.of(0, 0));
    	Date startDate = Date.from(datetime.toInstant(ZoneOffset.UTC));
    	BigDecimal payAmount = BigDecimal.ZERO;
    	try {
    		payAmount = new BigDecimal("0" + context.get("paymentAmount"));
    	} catch (Exception e) {
    		//
    	}
    	if (context.get("paymentTimes") == null || context.get("paymentTimes").equals("")) {
    		return ServiceUtil.returnError("Times is required.");
    	}
    	Integer payTimes= Integer.valueOf("0" + context.get("paymentTimes"));
    	if (context.get("paymentRate") == null || context.get("paymentRate").equals("")) {
    		return ServiceUtil.returnError("Rate is required.");
    	}
    	BigDecimal payRate= new BigDecimal("0" + context.get("paymentRate"));
    	payRate = payRate.divide(BigDecimal.valueOf(100));
    	BigDecimal lseRate = BigDecimal.ZERO;
    	try {
    		lseRate = new BigDecimal("0" + context.get("leaseRate"));
    	} catch (Exception e) {
    		//
    	}

//        Map<String, Object> map = new HashMap<String, Object>();
        List<Object> lists = new LinkedList<Object>();
//        int price = 100000;
//        double r = 0.05 / 12; // rate
//        double n = 60; // number of periods
//    	double p = -1 * price; // present value
        BigDecimal totalPrice = getPrice(delegator, quoteId, locale);
        double price = totalPrice.doubleValue();
        double r = payRate.divide(BigDecimal.valueOf(12), 5, BigDecimal.ROUND_HALF_UP).doubleValue();
        double n = payTimes.doubleValue();
        double f = 0; // future value (remain value)
        boolean t = false; // payment when start of period (false: end of term)
//        Date startDate = new Date();
        lists = calcFromTotalAmt(startDate, r, (int) n, price, f, t);
        successResult.put("lists", lists);
        Debug.logWarning("payRate: " + payRate, module);

        try {
            delegator.removeByAnd("QuotePayment", UtilMisc.toMap("quoteId", quoteId));
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }

        BigDecimal leaseAmount = BigDecimal.ZERO;
        for (Object l : lists) {
        	@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) l;
        	String quotePaymentId = ((Integer) list.get(0)).toString();
        	Date date = (Date) list.get(1);
        	Instant instant = date.toInstant();
        	Timestamp paymentDate = Timestamp.from(instant);
        	BigDecimal paymentAmount = (BigDecimal) list.get(2);
        	if (leaseAmount.equals(BigDecimal.ZERO)) {
        		leaseAmount = paymentAmount;
        	}
        	BigDecimal principal = (BigDecimal) list.get(3);
        	BigDecimal interest = (BigDecimal) list.get(4);
        	BigDecimal remainValue = (BigDecimal) list.get(5);
        	GenericValue quotePayment = delegator.makeValue("QuotePayment", UtilMisc.toMap("quoteId", quoteId,
        			"quotePaymentId", quotePaymentId, "paymentDate", paymentDate, "paymentAmount", paymentAmount, "principal", principal,
        			"interest", interest, "remainValue", remainValue));
            try {
                delegator.create(quotePayment);
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
                // if this fails no problem
            }
        }

        try {
            delegator.removeByAnd("QuoteTerm", UtilMisc.toMap("termTypeId", "LSE_PAY_TIMES_1", "quoteId", quoteId, "quoteItemSeqId", "_NA_"));
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }

//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(startDate);
//    	GenericValue paymentDay = delegator.makeValue("QuoteTerm", UtilMisc.toMap("termTypeId", "LSE_PAY_DAY_1", "quoteId", quoteId, "quoteItemSeqId", "_NA_", "textValue", calendar.getTimeInMillis()));
    	GenericValue paymentDay = delegator.makeValue("QuoteTerm", UtilMisc.toMap("termTypeId", "LSE_PAY_DAY_1", "quoteId", quoteId, "quoteItemSeqId", "_NA_", "textValue", datetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
    	GenericValue paymentAmount = delegator.makeValue("QuoteTerm", UtilMisc.toMap("termTypeId", "LSE_PAY_AMOUNT_1", "quoteId", quoteId, "quoteItemSeqId", "_NA_", "termValue", leaseAmount.longValue()));
    	GenericValue paymentTimes = delegator.makeValue("QuoteTerm", UtilMisc.toMap("termTypeId", "LSE_PAY_TIMES_1", "quoteId", quoteId, "quoteItemSeqId", "_NA_", "termValue", (long) n));
    	GenericValue paymentRate = delegator.makeValue("QuoteTerm", UtilMisc.toMap("termTypeId", "LSE_PAY_RATE", "quoteId", quoteId, "quoteItemSeqId", "_NA_", "textValue", payRate.multiply(BigDecimal.valueOf(100)).toString()));
    	GenericValue leaseRate = delegator.makeValue("QuoteTerm", UtilMisc.toMap("termTypeId", "LSE_LEASE_RATE_TERM", "quoteId", quoteId, "quoteItemSeqId", "_NA_", "textValue", lseRate.toString()));
        try {
            delegator.createOrStore(paymentDay);
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }
        try {
            delegator.createOrStore(paymentAmount);
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }
        try {
            delegator.createOrStore(paymentTimes);
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }
        try {
            delegator.createOrStore(paymentRate);
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }
        try {
            delegator.createOrStore(leaseRate);
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }

        successResult.put("lists", lists);
        successResult.put("price", totalPrice.toString());
        successResult.put("paymentDay", paymentDay.getString("textValue"));
        successResult.put("paymentAmount", paymentAmount.getLong("termValue").toString());
        successResult.put("paymentTimes", paymentTimes.getLong("termValue").toString());
        successResult.put("paymentRate", paymentRate.getString("textValue"));
        successResult.put("leaseRate", leaseRate.getString("textValue"));

        return successResult;
    }

    private static BigDecimal getPrice(Delegator delegator, String quoteId, Locale locale) {
        List<GenericValue> quoteItems = new LinkedList<GenericValue>();
        try {
        	quoteItems = EntityQuery.use(delegator).from("QuoteItem").where("quoteId", quoteId).orderBy("quoteItemSeqId").queryList();
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            // if this fails no problem
        }
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (GenericValue quoteItem : quoteItems) {
        	BigDecimal quantity = quoteItem.getBigDecimal("quantity");
        	if (quantity == null) {
        		quantity = BigDecimal.ONE;
        	}
        	BigDecimal selectedAmount = quoteItem.getBigDecimal("selectedAmount");
        	if (selectedAmount == null) {
        		selectedAmount = BigDecimal.ONE;
        	}
        	BigDecimal quoteUnitPrice = quoteItem.getBigDecimal("quoteUnitPrice");
        	if (quoteUnitPrice == null) {
        		quoteUnitPrice = BigDecimal.ZERO;
        	}
        	BigDecimal price = quoteUnitPrice.multiply(quantity).multiply(selectedAmount);
        	totalPrice = totalPrice.add(price);
        }
        if (locale.getLanguage().equals("ja")) {
        	totalPrice.setScale(0);
        } else {
        	totalPrice.setScale(2);
        }
    	return totalPrice;
    }

    /**
     * calc from price and numbers of payment
     *
     * @param rate rate of year
     * @param numbers numbers of payment
     * @param price price
     * @param fv future value
     * @param t true = payment start of periods, false = payment end of periods
     * @return list
     */
    public static List<Object> calcFromTotalAmt(Date start, double rate, int numbers, double price, double fv, boolean t) {
        List<Object> lists = new LinkedList<Object>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        double r = rate / 12; // rate
        double n = (double) numbers; // number of periods
    	double p = -1 * price; // present value
        double f = 0d; // future value (remain value)
        double pmt = FinanceLib.pmt(r, n, p, f, t);
    	BigDecimal leaseAmount = BigDecimal.valueOf(pmt).setScale(0, BigDecimal.ROUND_HALF_UP);
    	BigDecimal remainPrice = BigDecimal.valueOf(price).setScale(0, BigDecimal.ROUND_HALF_UP);
        for (int i = 0; i < numbers; i++) {
        	BigDecimal interest = remainPrice.multiply(BigDecimal.valueOf(r)).setScale(0, BigDecimal.ROUND_HALF_UP);
        	BigDecimal principal = leaseAmount.subtract(interest);
        	remainPrice = remainPrice.subtract(principal);
            List<Object> list = new LinkedList<Object>();
            list.add(Integer.valueOf(i + 1));
            list.add(getBusinessDate(calendar));
            list.add(leaseAmount);
            list.add(principal);
            list.add(interest);
            if (i == numbers - 1) {
                list.add(BigDecimal.ZERO);
            } else {
                list.add(remainPrice);
            }
            lists.add(list);
            calendar.add(Calendar.MONTH, 1);
        }
    	return lists;
    }

    /**
     * get next business date
     *
     * @param calendar
     * @return
     */
    public static Date getBusinessDate(Calendar calendar) {
    	Calendar cal = (Calendar) calendar.clone();
    	if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
    		cal.add(Calendar.DATE, 1);
    	}
    	if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
    		cal.add(Calendar.DATE, 1);
    	}
    	return cal.getTime();
    }
}
