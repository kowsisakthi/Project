package com.exam.events;

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
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

import com.exam.forms.HibernateValidationMaster;
import com.exam.forms.checks.TopicMasterCheck;
import com.exam.helper.HibernateHelper;
import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * 
 * @author DELL
 *
 */
public class StudentListEvents {

	private static final String MODULE = StudentListEvents.class.getName();
	//private static final String RES_ERR = "OnlineexamUiLabels";
	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public static String fetchStudentList(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		List<Map<String, Object>> viewStudentList = new ArrayList<Map<String, Object>>();
		try {
			List<GenericValue> studentList = EntityQuery.use(delegator).from(ConstantValues.PARTY_ROLE).where(ConstantValues.ROLE_TYPE_ID,ConstantValues.USER).queryList();
			if (UtilValidate.isNotEmpty(studentList)) {
				for (GenericValue student : studentList) {
					Map<String, Object> studentlist = new HashMap<String, Object>();
					studentlist.put(ConstantValues.PARTY_ID, student.getString(ConstantValues.PARTY_ID));
					GenericValue userid = EntityQuery.use(delegator).from(EntityConstants.USER_LOGIN_ENTITY).where(ConstantValues.PARTY_ID,student.getString(ConstantValues.PARTY_ID) )
							.cache().queryOne();
					studentlist.put(EntityConstants.USER_LOGIN_ID, userid.getString(EntityConstants.USER_LOGIN_ID));
					GenericValue userName = EntityQuery.use(delegator).from(ConstantValues.PERSON).where(ConstantValues.PARTY_ID,student.getString(ConstantValues.PARTY_ID) )
							.cache().queryOne();
					String fullName=userName.getString(EntityConstants.FIRST_NAME)+" "+userName.getString(EntityConstants.LAST_NAME);
					studentlist.put(EntityConstants.USER_NAME, fullName);
					viewStudentList.add(studentlist);
				}
				Map<String, Object> studentListInfo = new HashMap<>();
				studentListInfo.put(ConstantValues.STUDENT_LIST, viewStudentList);
				request.setAttribute(ConstantValues.STUDENT_LIST_INFO, studentListInfo);
				return ConstantValues.SUCCESS;
			} else {
				String errorMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.ERROR_IN_FETCHING_DATA,
						UtilHttp.getLocale(request));
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
				Debug.logError(errorMessage, MODULE);
				return ConstantValues.ERROR;
			}
		} catch (GenericEntityException e) {
			request.setAttribute(ConstantValues.ERROR, e);
			return ConstantValues.ERROR;
		}
	}
}
