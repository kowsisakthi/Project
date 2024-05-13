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
public class TopicMasterEvents {

	private static final String MODULE = TopicMasterEvents.class.getName();
	//private static final String RES_ERR = "OnlineExamPortalUiLabels";
/**
 * 
 * @param request
 * @param response
 * @return
 */
	public static String createTopic(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Object> combinedMap = UtilHttp.getCombinedMap(request);
		Locale locale = UtilHttp.getLocale(request);

		Delegator delegator = (Delegator) combinedMap.get(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) combinedMap.get(EntityConstants.DISPATCHER);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);

		String topicName = (String) combinedMap.get(ConstantValues.TOPIC_NAME);

		Map<String, Object> topicInfo = UtilMisc.toMap(ConstantValues.TOPIC_NAME, topicName, EntityConstants.USER_LOGIN,
				userLogin);

		try {
			Debug.logInfo("=======Creating TopicMaster record in event using service CreateTopicMaster=========",
					MODULE);
			HibernateValidationMaster hibernate = HibernateHelper.populateBeanFromMap(topicInfo,
					HibernateValidationMaster.class);

			Set<ConstraintViolation<HibernateValidationMaster>> errors = HibernateHelper
					.checkValidationErrors(hibernate, TopicMasterCheck.class);
			boolean hasFormErrors = HibernateHelper.validateFormSubmission(delegator, errors, request, locale,
					"Mandatory Err TopicMaster Entity", ConstantValues.ONLINE_EXAM_UI_LABELS, false);

			if (hasFormErrors) {
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errors);
				return ConstantValues.ERROR;
			}
			try {
				Map<String, ? extends Object> createTopicMasterInfoResult = dispatcher.runSync(ConstantValues.CREATE_TOPIC_MASTER,
						topicInfo);
				ServiceUtil.getMessages(request, createTopicMasterInfoResult, null);
				if (ServiceUtil.isError(createTopicMasterInfoResult)) {
					String errorMessage = ServiceUtil.getErrorMessage(createTopicMasterInfoResult);
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
					Debug.logError(errorMessage, MODULE);
					return ConstantValues.ERROR;
				} else {
					String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_SUCCESS_MESSAGE,
							UtilHttp.getLocale(request));
					ServiceUtil.getMessages(request, createTopicMasterInfoResult, successMessage);
					request.setAttribute(ConstantValues.SUCCESS_MESSAGE, successMessage);
					return ConstantValues.SUCCESS;
				}
			} catch (GenericServiceException e) {
				String errorMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_CALLING_ERROR,
						UtilHttp.getLocale(request)) + e.toString();
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
				return ConstantValues.ERROR;
			}

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			request.setAttribute(ConstantValues.ERROR_MESSAGE, e);
			return ConstantValues.ERROR;
		}
	}
/**
 * 
 * @param request
 * @param response
 * @return
 */
	public static String fetchOneTopic(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);

		String topicId = (String) request.getAttribute(ConstantValues.TOPIC_ID);
		try {
			GenericValue fetchedTopic = EntityQuery.use(delegator).select(ConstantValues.TOPIC_NAME).from(ConstantValues.TOPIC_MASTER)
					.where(ConstantValues.TOPIC_ID, topicId).cache().queryOne();

			request.setAttribute(ConstantValues.TOPIC_MASTER_, fetchedTopic);
			return ConstantValues.SUCCESS;

		} catch (GenericEntityException e) {
			request.setAttribute(ConstantValues.ERROR, e);
			return ConstantValues.ERROR;
		}
	}
/**
 * 
 * @param request
 * @param response
 * @return
 */
	public static String fetchAllTopics(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		List<Map<String, Object>> viewTopicList = new ArrayList<Map<String, Object>>();
		try {
			List<GenericValue> listOfTopics = EntityQuery.use(delegator).from(ConstantValues.TOPIC_MASTER).cache().queryList();
			if (UtilValidate.isNotEmpty(listOfTopics)) {
				for (GenericValue topic : listOfTopics) {
					Map<String, Object> topicList = new HashMap<String, Object>();
					topicList.put(ConstantValues.TOPIC_ID, topic.get(ConstantValues.TOPIC_ID));
					topicList.put(ConstantValues.TOPIC_NAME, topic.get(ConstantValues.TOPIC_NAME));
					viewTopicList.add(topicList);
				}
				Map<String, Object> topicsInfo = new HashMap<>();
				topicsInfo.put(ConstantValues.TOPIC_LIST, viewTopicList);
				request.setAttribute(ConstantValues.TOPIC_INFO, topicsInfo);
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
/**
 * 
 * @param request
 * @param response
 * @return
 */
	public static String updateTopic(HttpServletRequest request, HttpServletResponse response) {

		Locale locale = UtilHttp.getLocale(request);

		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);
		String topicId = (String) request.getAttribute(ConstantValues.TOPIC_ID);
		String topicName = (String) request.getAttribute(ConstantValues.TOPIC_NAME);
		Map<String, Object> topicInfo = UtilMisc.toMap(ConstantValues.TOPIC_ID, topicId, ConstantValues.TOPIC_NAME,
				topicName, EntityConstants.USER_LOGIN, userLogin);

		try {
			Debug.logInfo("=======Updating TopicMaster record in event using service UpdateTopicMaster=========",
					MODULE);
			HibernateValidationMaster hibernate = HibernateHelper.populateBeanFromMap(topicInfo,
					HibernateValidationMaster.class);

			Set<ConstraintViolation<HibernateValidationMaster>> errors = HibernateHelper
					.checkValidationErrors(hibernate, TopicMasterCheck.class);
			boolean hasFormErrors = HibernateHelper.validateFormSubmission(delegator, errors, request, locale,
					"Mandatory Err TopicMaster Entity", ConstantValues.ONLINE_EXAM_UI_LABELS, false);

			if (hasFormErrors) {
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errors);
				return ConstantValues.ERROR;
			}
			try {
				Map<String, ? extends Object> updateTopicMasterInfoResult = dispatcher.runSync(ConstantValues.UPDATE_TOPIC_MASTER,
						topicInfo);
				ServiceUtil.getMessages(request, updateTopicMasterInfoResult, null);
				if (ServiceUtil.isError(updateTopicMasterInfoResult)) {
					String errorMessage = ServiceUtil.getErrorMessage(updateTopicMasterInfoResult);
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
					Debug.logError(errorMessage, MODULE);
					return ConstantValues.ERROR;
				} else {
					String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_SUCCESS_MESSAGE,
							UtilHttp.getLocale(request));
					ServiceUtil.getMessages(request, updateTopicMasterInfoResult, successMessage);
					request.setAttribute(ConstantValues.SUCCESS_MESSAGE, successMessage);
					Debug.logError(successMessage, MODULE);
					return ConstantValues.SUCCESS;
				}
			} catch (GenericServiceException e) {
				String errMsg = "Error setting topic info: " + e.toString();
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
				return ConstantValues.ERROR;
			}
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			request.setAttribute(ConstantValues.ERROR_MESSAGE, e);
			return ConstantValues.ERROR;
		}
	}
/**
 * 
 * @param request
 * @param response
 * @return
 */
	public static String deleteTopic(HttpServletRequest request, HttpServletResponse response) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		String topicId = (String) request.getAttribute(ConstantValues.TOPIC_ID);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);
		Map<String, Object> topicInfo = UtilMisc.toMap(ConstantValues.TOPIC_ID, topicId, EntityConstants.USER_LOGIN,
				userLogin);

		try {
			Debug.logInfo("=======Deleting TopicMaster record in event using service DeleteTopicMaster=========",
					MODULE);
			try {
				Map<String, ? extends Object> deleteTopicMasterInfoResult = dispatcher.runSync(ConstantValues.DELETE_TOPIC_MASTER,
						topicInfo);
				if (UtilValidate.isNotEmpty(topicInfo)) {
					String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.DELETE_ERROR_MESSAGE,
							UtilHttp.getLocale(request));
					ServiceUtil.getMessages(request, deleteTopicMasterInfoResult, successMessage);
					request.setAttribute(ConstantValues.SUCCESS_MESSAGE, successMessage);
					Debug.logError(successMessage, MODULE);
					return ConstantValues.SUCCESS;
				} else {
					String errorMessage = ServiceUtil.getErrorMessage(deleteTopicMasterInfoResult);
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
					Debug.logError(errorMessage, MODULE);
					return ConstantValues.ERROR;
				}
			} catch (GenericServiceException e) {
				String errorMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.DELETE_ERROR_MESSAGE,
						UtilHttp.getLocale(request)) + e.toString();
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
				return ConstantValues.ERROR;
			}
		} catch (Exception e) {
			Debug.logError(e, MODULE);
			request.setAttribute(ConstantValues.ERROR_MESSAGE, e);
			return ConstantValues.ERROR;
		}
	}

}
