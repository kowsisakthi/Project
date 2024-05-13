package com.exam.events;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;

import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

import com.exam.forms.HibernateValidation;
import com.exam.forms.checks.RegisterFormCheck;
import com.exam.helper.HibernateHelper;
import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
/**
 * 
 * @author DELL
 *
 */
public class RegistrationEvent {
	public static final String module = RegistrationEvent.class.getName();
	//public static String resource_error = "OnlineExamPortalUiLabels";
/**
 * 
 * @param request
 * @param response
 * @return
 */
	public static String register(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);

		Locale locale = UtilHttp.getLocale(request);
		Map<String, Object> combinedMap = UtilHttp.getCombinedMap(request);
		String username = combinedMap.get(EntityConstants.CAPS_USER_NAME).toString();
		String firstname = combinedMap.get(EntityConstants.CAPS_FIRSTNAME).toString();
		String lastname = combinedMap.get(EntityConstants.CAPS_LASTNAME).toString();
		String password = combinedMap.get(EntityConstants.CAPS_PASSWORD).toString();
		String confirmpassword = combinedMap.get(EntityConstants.CAPS_CONFIRMPASSWORD).toString();
		Map<String, Object> obj = UtilMisc.toMap(EntityConstants.USER_LOGIN_ID, username, EntityConstants.FIRST_NAME,
				firstname, EntityConstants.LAST_NAME, lastname, EntityConstants.CURRENT_PASSWORD, password,
				EntityConstants.CURRENT_PASSWORD_VERIFY, confirmpassword,EntityConstants.USER_LOGIN, userLogin);
		Map<String, Object> obj2 = UtilMisc.toMap(EntityConstants.USER_LOGIN_ID, username, EntityConstants.FIRST_NAME,
				firstname, EntityConstants.LAST_NAME, lastname, EntityConstants.PASSWORD, password,
				EntityConstants.CURRENT_PASSWORD_VERIFY, confirmpassword);
		try {
			HibernateValidation Hibernet = HibernateHelper.populateBeanFromMap(obj2, HibernateValidation.class);
			Set<ConstraintViolation<HibernateValidation>> errors = HibernateHelper.checkValidationErrors(Hibernet,
					RegisterFormCheck.class);
			boolean hasFormErrors = HibernateHelper.validateFormSubmission(delegator, errors, request, locale,
					ConstantValues.MANDATORY_FIELD_ERRORMSG_LOGINFORM, ConstantValues.ONLINE_EXAM_UI_LABELS, false);
			if (!hasFormErrors) {
				Map<String, Object> result = dispatcher.runSync(ConstantValues.CREATE_PERSON_AND_USER_LOGIN, obj);

				if (ServiceUtil.isError(result)) {
					request.setAttribute(ConstantValues.REGISTER_ERROR, ConstantValues.ERROR);
					return ConstantValues.ERROR;
				}

				Map<String, Object> roleresult = dispatcher.runSync(ConstantValues.ASSIGN_PARTY_ROLE,
						UtilMisc.toMap(ConstantValues.PARTY_ID, result.get(ConstantValues.PARTY_ID), ConstantValues.ROLE_TYPE_ID, ConstantValues.USER));
				
				request.setAttribute(ConstantValues.ROLE_RESULT, roleresult);
				request.setAttribute(ConstantValues.RESULT, result);
			}
		} catch (Exception e) {
			request.setAttribute(ConstantValues.REGISTER_ERROR, e);
			return ConstantValues.ERROR;
		}
		return ConstantValues.SUCCESS;
	}

}
