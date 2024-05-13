package com.exam.events;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;

/**
 * 
 * @author DELL
 *
 */
public class ExamInformation {

	// Define a constant for the class name
	public static final String module = ExamInformation.class.getName();

	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	
	public static String getExamInfo(HttpServletRequest request, HttpServletResponse response) {
		// Retrieve delegator and local dispatcher from the request attributes
		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);

		try {
			

			// Invoke the service to get exam information
			Map<String, Object> examInformationServiceResult = dispatcher.runSync(ConstantValues.GET_EXAM_INFORMATION,
					UtilMisc.toMap(EntityConstants.USER_LOGIN, userLogin, ConstantValues.REQUEST, request));

			// Check if the service call resulted in an error
			if (ServiceUtil.isError(examInformationServiceResult)) {
				// Handle error scenario
				String errorMessage = ServiceUtil.getErrorMessage(examInformationServiceResult);
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
				Debug.logError(errorMessage, module);
				return ConstantValues.ERROR;
			}

			// Handle success scenario
			String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
					ConstantValues.SERVICE_SUCCESS_MESSAGE, UtilHttp.getLocale(request));
			ServiceUtil.getMessages(request, examInformationServiceResult, successMessage);
			request.setAttribute(ConstantValues.EXAMS, examInformationServiceResult);
			return ConstantValues.SUCCESS;

		} catch (Exception e) {
			// Handle exceptions during service invocation
			Debug.logError(e, module);
			String errMsg = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_CALLING_ERROR,
					UtilHttp.getLocale(request)) + e.toString();// "Error in calling or executing the service: ";
			request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
			return ConstantValues.ERROR;
		}
	}
}
