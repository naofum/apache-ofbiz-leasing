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

GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
quoteId = request.getParameter("quoteId");

conditionList = new LinkedList();
conditionList.add(new EntityExpr("quoteId", EntityOperator.EQUALS, quoteId));
entityCondition = new EntityConditionList(conditionList, EntityOperator.AND);
orderByList = UtilMisc.toList("quoteTypeId", "to_number(quotePaymentId)");


dynamicView = new DynamicViewEntity();
dynamicView.addMemberEntity("QT", "Quote");
dynamicView.addAlias("QT", "quoteId");
dynamicView.addAlias("QT", "quoteTypeId");
dynamicView.addAlias("QT", "partyId");
dynamicView.addAlias("QT", "issueDate");
dynamicView.addAlias("QT", "statusId");
dynamicView.addAlias("QT", "currencyUomId");
dynamicView.addAlias("QT", "productStoreId");
dynamicView.addAlias("QT", "salesChannelEnumId");
dynamicView.addAlias("QT", "validFromDate");
dynamicView.addAlias("QT", "validThruDate");
dynamicView.addAlias("QT", "quoteName");
dynamicView.addAlias("QT", "description");

dynamicView.addMemberEntity("QP", "QuotePayment")
dynamicView.addViewLink("QP", "QT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("quoteId"))
dynamicView.addAlias("QP", "quotePaymentId")
dynamicView.addAlias("QP", "paymentDate")
dynamicView.addAlias("QP", "currencyUomId")
dynamicView.addAlias("QP", "paymentAmount")
dynamicView.addAlias("QP", "principal")
dynamicView.addAlias("QP", "interest")
dynamicView.addAlias("QP", "remainValue")
dynamicView.addAlias("QP", "remainAmount")

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

request.setAttribute("jrDataSource", jrDataSource);
request.setAttribute("jrParameters", jrParameters);

return "success"
