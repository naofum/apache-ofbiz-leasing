/*
 *
 * Copyright 2001-2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import java.sql.*;
import org.apache.ofbiz.entity.*;
import org.apache.ofbiz.entity.condition.*;
import org.apache.ofbiz.entity.model.ModelKeyMap
import org.apache.ofbiz.base.util.*;
import org.apache.ofbiz.content.report.*;

import org.apache.ofbiz.service.ServiceUtil
import org.apache.ofbiz.base.util.Debug

import java.util.LinkedList;
//import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.model.DynamicViewEntity;

Debug.logInfo("-=-=-=- TEST GROOVY SERVICE -=-=-=-", "")

uiLabelMap = UtilProperties.getResourceBundleMap("CommonUiLabels", locale)
entityLabelMap = UtilProperties.getResourceBundleMap("CommonEntityLabels", locale)

GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
quoteId = request.getParameter("quoteId");

conditionList = new LinkedList();
conditionList.add(new EntityExpr("quoteId", EntityOperator.EQUALS, quoteId));
//conditionList.add(new EntityExpr("quoteItemId", EntityOperator.EQUALS, "00001"));
entityCondition = new EntityConditionList(conditionList, EntityOperator.AND);
//orderByList = UtilMisc.toList("quoteTypeId");
//groupByList = UtilMisc.toList("quoteId");

dynamicView = new DynamicViewEntity();
dynamicView.addMemberEntity("QT", "Quote");
//dynamicView.addAlias("QT", "quoteId");
//dynamicView.addAlias("QT", "quoteTypeId");
dynamicView.addAlias("QT", "quoteId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QT", "quoteTypeId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QT", "partyId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QT", "issueDate", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QT", "statusId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QT", "currencyUomId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QT", "productStoreId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QT", "salesChannelEnumId", null, null, null, Boolean.TRUE, null);;
dynamicView.addAlias("QT", "validFromDate", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QT", "validThruDate", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QT", "quoteName", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QT", "description", null, null, null, Boolean.TRUE, null);

dynamicView.addMemberEntity("QI", "QuoteItem")
dynamicView.addViewLink("QI", "QT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("quoteId"))
dynamicView.addAlias("QI", "quoteItemSeqId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "productId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "productFeatureId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "deliverableTypeId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "skillTypeId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "uomId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "workEffortId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "custRequestId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "custRequestItemSeqId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "quantity", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "selectedAmount", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "quoteUnitPrice", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "reservStart", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "reservLength", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "reservPersons", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "configId", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "estimatedDeliveryDate", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "comments", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "isPromo", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("QI", "leadTimeDays", null, null, null, Boolean.TRUE, null);

dynamicView.addMemberEntity("PT", "Party")
dynamicView.addViewLink("QT", "PT", Boolean.TRUE, ModelKeyMap.makeKeyMapList("partyId"))
dynamicView.addAlias("PT", "partyTypeId", null, null, null, Boolean.TRUE, null);

dynamicView.addMemberEntity("PG", "PartyGroup")
dynamicView.addViewLink("QT", "PG", Boolean.TRUE, ModelKeyMap.makeKeyMapList("partyId"))
dynamicView.addAlias("PG", "groupName", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("PG", "groupNameLocal", null, null, null, Boolean.TRUE, null);

dynamicView.addMemberEntity("PS", "Person")
dynamicView.addViewLink("QT", "PS", Boolean.TRUE, ModelKeyMap.makeKeyMapList("partyId"))
dynamicView.addAlias("PS", "salutation", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("PS", "firstName", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("PS", "middleName", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("PS", "lastName", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("PS", "personalTitle", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("PS", "suffix", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("PS", "nickname", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("PS", "firstNameLocal", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("PS", "middleNameLocal", null, null, null, Boolean.TRUE, null);
dynamicView.addAlias("PS", "lastNameLocal", null, null, null, Boolean.TRUE, null);

dynamicView.addMemberEntity("QP", "QuotePayment")
dynamicView.addViewLink("QT", "QP", Boolean.TRUE, ModelKeyMap.makeKeyMapList("quoteId"))
dynamicView.addAlias("QP", "totalPaymentAmount", "paymentAmount", null, null, Boolean.FALSE, "sum")
dynamicView.addAlias("QP", "countPayments", "quotePaymentId", null, null, Boolean.FALSE, "count")
//groupBy = new LinkedList<String>();
//groupBy.add("quoteId");
//dynamicView.setGroupBy();

andExprs = new LinkedList<EntityCondition>();
andExprs.add(EntityCondition.makeCondition("quoteId", EntityOperator.EQUALS, quoteId));
mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
orderBy = new LinkedList<String>();
orderBy.add("quoteTypeId");
eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, orderBy, null);

//eli = delegator.findListIteratorByCondition("Quote", entityCondition, null, null, orderByList, null);
//eli = delegator.findByAnd("Quote", [quoteId: quoteId], null, false)
jrDataSource = new JREntityListIteratorDataSource(eli);

jrParameters = new HashMap();
jrParameters.put("quoteId", quoteId);
jrParameters.put("uiLabelMap", uiLabelMap);
jrParameters.put("entityLabelMap", entityLabelMap);

request.setAttribute("jrDataSource", jrDataSource);
request.setAttribute("jrParameters", jrParameters);

return "success"
