package com.exam.events;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.webapp.control.LoginWorker;

import com.exam.forms.HibernateValidation;
import com.exam.forms.checks.LoginFormCheck;
import com.exam.helper.HibernateHelper;
import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;
/**
 * 
 * @author DELL
 *
 */
public class LoginEvent {

	public static final String module = LoginEvent.class.getName();
/**
 * 
 * @param request
 * @param response
 * @return
 */
	// Method to Login
	public static String dologin(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		

//		Locale locale = UtilHttp.getLocale(request);
//		String username = request.getAttribute(EntityConstants.CAPS_USER_NAME).toString();
//		String password = request.getAttribute(EntityConstants.CAPS_PASSWORD).toString();
//
//		Map<String, Object> userLoginMap = UtilMisc.toMap(EntityConstants.USER_LOGIN_ID, username, EntityConstants.PASSWORD, password);


		try {
//			HibernateValidation Hibernet = HibernateHelper.populateBeanFromMap(userLoginMap, HibernateValidation.class);
//			Set<ConstraintViolation<HibernateValidation>> errors = HibernateHelper.checkValidationErrors(Hibernet,
//					LoginFormCheck.class);
//			boolean hasFormErrors = HibernateHelper.validateFormSubmission(delegator, errors, request, locale,
//					ConstantValues.MANDATORY_FIELD_ERRORMSG_LOGINFORM, ConstantValues.ONLINE_EXAM_UI_LABELS, false);
			boolean hasFormErrors=false;
			if (!hasFormErrors) {
				String result = LoginWorker.login(request, response);
				if (result.equals(ConstantValues.SUCCESS)) {
					String successMsg = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.LOGIN_SUCCESS_MESSAGE, UtilHttp.getLocale(request));
					request.setAttribute(ConstantValues.SUCCESS_MESSAGE, successMsg);
					GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);
					// Query to retrieve data from UserLogin Entity
					String partyId = userLogin.getString(ConstantValues.PARTY_ID);
					
					//Query to retrieve data's from PartyRole Entity
					List<GenericValue> role = EntityQuery.use(delegator).from(ConstantValues.PARTY_ROLE).where(ConstantValues.PARTY_ID, partyId)
							.cache().queryList();
					request.setAttribute(ConstantValues.ROLE, role.get(1).getString(ConstantValues.ROLE_TYPE_ID));
					request.getSession().setAttribute(ConstantValues.PARTY_ID, partyId);
				}
				request.setAttribute(ConstantValues.RESULT, result);
			} else {
				request.setAttribute(ConstantValues.ERROR_MESSAGE, ConstantValues.LOGIN_ERROR);
			}
			request.setAttribute(ConstantValues.HIBERNAT_RESULT, hasFormErrors);
		} catch (Exception e) {
			String errMsg = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_CALLING_ERROR, UtilHttp.getLocale(request))+e.toString();//"Error in calling or excecuting the service: " + e.toString();
			request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
			return ConstantValues.ERROR;
		}
		return ConstantValues.SUCCESS;
	}
}