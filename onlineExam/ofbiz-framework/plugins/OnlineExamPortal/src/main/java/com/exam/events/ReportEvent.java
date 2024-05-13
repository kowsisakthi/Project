package com.exam.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.LocalDispatcher;

import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;
/**
 * 
 * @author DELL
 *
 */
public class ReportEvent {
	private static final String MODULE = ReportEvent.class.getName();
	//private static final String RES_ERR = "OnlineexamUiLabels";
/**
 * 
 * @param request
 * @param response
 * @return
 */
	public static String fetchResults(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);
		Map<String, Object> combinedMap = UtilHttp.getCombinedMap(request);
		try {
			//calling fetchExamResult service
			Map<String, Object> resultMap = dispatcher.runSync(ConstantValues.FETCH_EXAM_RESULT,
					UtilMisc.toMap(ConstantValues.COMBINED_MAP, combinedMap, EntityConstants.USER_LOGIN, userLogin));
			if (UtilValidate.isEmpty(resultMap)) {
				request.setAttribute(ConstantValues.ERROR_MESSAGE, ConstantValues.EXAM_RESULT_ERROR_MESSAGE);
				return ConstantValues.ERROR;
			}
			request.setAttribute(ConstantValues.EXAM_LIST_REPORT, resultMap.get(ConstantValues.EXAM_LIST_REPORT));
			request.setAttribute(ConstantValues.EXAM_WISE_PERFORMANCE, resultMap.get(ConstantValues.EXAM_WISE_PERFORMANCE));
			request.getSession().removeAttribute(ConstantValues.USER_ATTEMPT_PERFORMANCE_ID);
						return ConstantValues.SUCCESS;
		} catch (Exception e) {
			return ConstantValues.ERROR;
		}
	}
}
