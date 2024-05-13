package com.exam.services;

import java.util.HashMap;

import java.util.Iterator;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityCrypto;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.entity.util.EntityUtilProperties;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.webapp.control.LoginWorker;

import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;

import org.apache.ofbiz.entity.model.ModelField;
import org.apache.ofbiz.common.login.LoginServices;
/**
 * 
 * @author DELL
 *
 */
public class LoginService {

	public static final String module = LoginService.class.getName();
/**
 * 
 * @param dctx
 * @param context
 * @return
 */
	public static Map<String, Object> checklogin(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> dataResult = new HashMap<String, Object>();
	
		String userLoginId = (String) context.get(EntityConstants.USER_NAME);
		String pass = (String) context.get(EntityConstants.PASSWORD);
		Delegator delegator = (Delegator) context.get(EntityConstants.DELEGATOR);
		try {
			GenericValue userLogin = EntityQuery.use(delegator).from(ConstantValues.USER_LOGIN).where(EntityConstants.USER_LOGIN_ID, userLoginId).queryOne();
			if (userLogin != null) {
				String password = userLogin.getString(EntityConstants.CURRENT_PASSWORD);
				boolean useEncryption = "true"
						.equals(EntityUtilProperties.getPropertyValue("security", "password.encrypt", delegator));
				if (pass != null && LoginServices.checkPassword(password, useEncryption, pass)) {
					dataResult.put("dataresult", "success-" + password);
					result.put("dataResultStatus", dataResult);
					Debug.log("=======Login success=========");
					return result;
				}
			}
			dataResult.put("dataresult", "Invalid password");
			result.put("dataResultStatus", dataResult);
			result.put("status", "failed");
			return result;
		} catch (Exception e) {
			Debug.logError(e, module);
			return ServiceUtil.returnError("Error in Login Service java class ........Invalid email" + module);
		}
	}
}