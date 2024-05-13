package com.exam.events;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

import com.exam.forms.HibernateValidationMaster;
import com.exam.forms.checks.ExamMasterCheck;
import com.exam.helper.HibernateHelper;
import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;

/**
 * 
 * @author DELL
 *
 */
public class ExamMasterEvents {
	private static final String MODULE = ExamMasterEvents.class.getName();

	/**
	 * Method to insert data into ExamMaster Entity
	 */
	public static String createExam(HttpServletRequest request, HttpServletResponse response) {
		Locale locale = UtilHttp.getLocale(request);

		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);

		String examName = (String) request.getAttribute(ConstantValues.EXAM_NAME);
		String description = (String) request.getAttribute(ConstantValues.EXAM_DESCRIPTION);
		String creationDate = (String) request.getAttribute(ConstantValues.EXAM_CREATION_DATE);
		String expirationDate = (String) request.getAttribute(ConstantValues.EXAM_EXPIRATION_DATE);
		String noOfQuestions = (String) request.getAttribute(ConstantValues.EXAM_TOTAL_QUES);
		String durationMinutes = (String) request.getAttribute(ConstantValues.EXAM_DURATION);
		String passPercentage = (String) request.getAttribute(ConstantValues.EXAM_PASS_PERCENTAGE);
		String questionsRandomized = (String) request.getAttribute(ConstantValues.EXAM_QUES_RANDOMIZED);
		String answersMust = (String) request.getAttribute(ConstantValues.EXAM_ANS_MUST);
		String enableNegativeMark = (String) request.getAttribute(ConstantValues.EXAM_ENABLE_NEG_MARK);
		String negativeMarkValue = (String) request.getAttribute(ConstantValues.EXAM_NEG_MARK);

		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		LocalDateTime creationLocalDateTime = LocalDateTime.parse(creationDate, inputFormatter);
		String creationDateFormatted = creationLocalDateTime.format(outputFormatter);

		LocalDateTime expirationLocalDateTime = LocalDateTime.parse(expirationDate, inputFormatter);
		String expirationDateFormatted = expirationLocalDateTime.format(outputFormatter);

		Map<String, Object> examInfo = UtilMisc.toMap(ConstantValues.EXAM_NAME, examName,
				ConstantValues.EXAM_DESCRIPTION, description, ConstantValues.EXAM_CREATION_DATE, creationDateFormatted,
				ConstantValues.EXAM_EXPIRATION_DATE, expirationDateFormatted, ConstantValues.EXAM_TOTAL_QUES,
				noOfQuestions, ConstantValues.EXAM_DURATION, durationMinutes, ConstantValues.EXAM_PASS_PERCENTAGE,
				passPercentage, ConstantValues.EXAM_QUES_RANDOMIZED, questionsRandomized, ConstantValues.EXAM_ANS_MUST,
				answersMust, ConstantValues.EXAM_ENABLE_NEG_MARK, enableNegativeMark, ConstantValues.EXAM_NEG_MARK,
				negativeMarkValue, EntityConstants.USER_LOGIN, userLogin);

		try {
			HibernateValidationMaster hibernate = HibernateHelper.populateBeanFromMap(examInfo,
					HibernateValidationMaster.class);

			Set<ConstraintViolation<HibernateValidationMaster>> errors = HibernateHelper
					.checkValidationErrors(hibernate, ExamMasterCheck.class);
			boolean hasFormErrors = HibernateHelper.validateFormSubmission(delegator, errors, request, locale,
					"Mandatory Err ExamMaster Entity", ConstantValues.ONLINE_EXAM_UI_LABELS, false);

			if (hasFormErrors) {
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errors);
				return ConstantValues.ERROR;
			}
			try {
				// Calling Entity-Auto service to insert data into ExamMaster Entity
				Map<String, ? extends Object> createExamMasterInfoResult = dispatcher.runSync(ConstantValues.CREATE_EXAM_MASTER,
						examInfo);
				ServiceUtil.getMessages(request, createExamMasterInfoResult, null);
				if (ServiceUtil.isError(createExamMasterInfoResult)) {
					String errorMessage = ServiceUtil.getErrorMessage(createExamMasterInfoResult);
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
					Debug.logError(errorMessage, MODULE);
					return ConstantValues.ERROR;
				}
				String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
						ConstantValues.SERVICE_SUCCESS_MESSAGE, UtilHttp.getLocale(request));
				ServiceUtil.getMessages(request, createExamMasterInfoResult, successMessage);
				request.setAttribute(ConstantValues.SUCCESS_MESSAGE, successMessage);
				return ConstantValues.SUCCESS;

			} catch (GenericServiceException e) {
				String errorMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
						ConstantValues.SERVICE_CALLING_ERROR, UtilHttp.getLocale(request)) + e.toString();// "Error setting exam info:
																							// " + e.toString();
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
	 * Method to retrieve data's from ExamMaster Entity
	 * 
	 * @param request
	 * @param response
	 * @return
	 */

	public static String fetchAllExams(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		List<Map<String, Object>> viewExamList = new ArrayList<Map<String, Object>>();
		try {
			// Query to retrieve data's from ExamMaster
			List<GenericValue> listOfExams = EntityQuery.use(delegator).from(ConstantValues.EXAM_MASTER).queryList();
			if (UtilValidate.isNotEmpty(listOfExams)) {
				for (GenericValue exam : listOfExams) {
					Map<String, Object> examList = new HashMap<String, Object>();
					examList.put(ConstantValues.EXAM_ID, exam.get(ConstantValues.EXAM_ID));
					examList.put(ConstantValues.EXAM_NAME, exam.get(ConstantValues.EXAM_NAME));
					examList.put(ConstantValues.EXAM_DESCRIPTION, exam.get(ConstantValues.EXAM_DESCRIPTION));
					examList.put(ConstantValues.EXAM_CREATION_DATE, exam.get(ConstantValues.EXAM_CREATION_DATE));
					examList.put(ConstantValues.EXAM_EXPIRATION_DATE, exam.get(ConstantValues.EXAM_EXPIRATION_DATE));
					examList.put(ConstantValues.EXAM_TOTAL_QUES, exam.get(ConstantValues.EXAM_TOTAL_QUES));
					examList.put(ConstantValues.EXAM_DURATION, exam.get(ConstantValues.EXAM_DURATION));
					examList.put(ConstantValues.EXAM_PASS_PERCENTAGE, exam.get(ConstantValues.EXAM_PASS_PERCENTAGE));
					examList.put(ConstantValues.EXAM_QUES_RANDOMIZED, exam.get(ConstantValues.EXAM_QUES_RANDOMIZED));
					examList.put(ConstantValues.EXAM_ANS_MUST, exam.get(ConstantValues.EXAM_ANS_MUST));
					examList.put(ConstantValues.EXAM_ENABLE_NEG_MARK, exam.get(ConstantValues.EXAM_ENABLE_NEG_MARK));
					examList.put(ConstantValues.EXAM_NEG_MARK, exam.get(ConstantValues.EXAM_NEG_MARK));
					viewExamList.add(examList);
				}
				Map<String, Object> examsInfo = new HashMap<>();
				examsInfo.put(ConstantValues.EXAM_LIST, viewExamList);
				request.setAttribute(ConstantValues.EXAM_INFO, examsInfo);
				return ConstantValues.SUCCESS;
			}
			String errorMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.ERROR_IN_FETCHING_DATA,
					UtilHttp.getLocale(request));// "No matched fields in ExamMaster Entity";
			request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
			Debug.logError(errorMessage, MODULE);
			return ConstantValues.ERROR;

		} catch (GenericEntityException e) {
			request.setAttribute(ConstantValues.ERROR, e);
			return ConstantValues.ERROR;
		}
	}

	/**
	 * Method to Update data's into ExamMaster Entity
	 * 
	 * @param request
	 * @param response
	 * @return
	 */

	public static String updateExam(HttpServletRequest request, HttpServletResponse response) {

		Locale locale = UtilHttp.getLocale(request);

		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);

		String examId = (String) request.getAttribute(ConstantValues.EXAM_ID);
		String examName = (String) request.getAttribute(ConstantValues.EXAM_NAME);
		String description = (String) request.getAttribute(ConstantValues.EXAM_DESCRIPTION);
		String creationDate = (String) request.getAttribute(ConstantValues.EXAM_CREATION_DATE);
		String expirationDate = (String) request.getAttribute(ConstantValues.EXAM_EXPIRATION_DATE);
		String noOfQuestions = (String) request.getAttribute(ConstantValues.EXAM_TOTAL_QUES);
		String durationMinutes = (String) request.getAttribute(ConstantValues.EXAM_DURATION);
		String passPercentage = (String) request.getAttribute(ConstantValues.EXAM_PASS_PERCENTAGE);
		String questionsRandomized = (String) request.getAttribute(ConstantValues.EXAM_QUES_RANDOMIZED);
		String answersMust = (String) request.getAttribute(ConstantValues.EXAM_ANS_MUST);
		String enableNegativeMark = (String) request.getAttribute(ConstantValues.EXAM_ENABLE_NEG_MARK);
		String negativeMarkValue = (String) request.getAttribute(ConstantValues.EXAM_NEG_MARK);

		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		String creationDateString = (String) request.getAttribute(ConstantValues.EXAM_CREATION_DATE);
		LocalDateTime creationLocalDateTime = LocalDateTime.parse(creationDateString, inputFormatter);
		String creationDateFormatted = creationLocalDateTime.format(outputFormatter);

		String expirationDateString = (String) request.getAttribute(ConstantValues.EXAM_EXPIRATION_DATE);
		LocalDateTime expirationLocalDateTime = LocalDateTime.parse(expirationDateString, inputFormatter);
		String expirationDateFormatted = expirationLocalDateTime.format(outputFormatter);

		Map<String, Object> examInfo = UtilMisc.toMap(ConstantValues.EXAM_ID, examId, ConstantValues.EXAM_NAME,
				examName, ConstantValues.EXAM_DESCRIPTION, description, ConstantValues.EXAM_CREATION_DATE,
				creationDateFormatted, ConstantValues.EXAM_EXPIRATION_DATE, expirationDateFormatted,
				ConstantValues.EXAM_TOTAL_QUES, noOfQuestions, ConstantValues.EXAM_DURATION, durationMinutes,
				ConstantValues.EXAM_PASS_PERCENTAGE, passPercentage, ConstantValues.EXAM_QUES_RANDOMIZED,
				questionsRandomized, ConstantValues.EXAM_ANS_MUST, answersMust, ConstantValues.EXAM_ENABLE_NEG_MARK,
				enableNegativeMark, ConstantValues.EXAM_NEG_MARK, negativeMarkValue, EntityConstants.USER_LOGIN,
				userLogin);

		try {
			HibernateValidationMaster hibernate = HibernateHelper.populateBeanFromMap(examInfo,
					HibernateValidationMaster.class);

			Set<ConstraintViolation<HibernateValidationMaster>> errors = HibernateHelper
					.checkValidationErrors(hibernate, ExamMasterCheck.class);
			boolean hasFormErrors = HibernateHelper.validateFormSubmission(delegator, errors, request, locale,
					"Mandatory Fields missing in ExamMaster Entity", ConstantValues.ONLINE_EXAM_UI_LABELS, false);

			if (hasFormErrors) {
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errors);
				return ConstantValues.ERROR;

			}
			try {
				// Calling Entity-Auto Service to Update data into ExamMaster Entity
				Map<String, ? extends Object> updateExamMasterInfoResult = dispatcher.runSync(ConstantValues.UPDATE_EXAM_MASTER,
						examInfo);
				ServiceUtil.getMessages(request, updateExamMasterInfoResult, null);
				if (ServiceUtil.isError(updateExamMasterInfoResult)) {
					String errorMessage = ServiceUtil.getErrorMessage(updateExamMasterInfoResult);
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
					Debug.logError(errorMessage, MODULE);
					return ConstantValues.ERROR;
				} else {
					String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
							ConstantValues.SERVICE_SUCCESS_MESSAGE, UtilHttp.getLocale(request));
					ServiceUtil.getMessages(request, updateExamMasterInfoResult, successMessage);
					request.setAttribute(ConstantValues.SUCCESS_MESSAGE, successMessage);
					return ConstantValues.SUCCESS;
				}
			} catch (GenericServiceException e) {
				String errMsg = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_CALLING_ERROR,
						UtilHttp.getLocale(request)) + e.toString();
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
				return ConstantValues.ERROR;
			}

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			request.setAttribute(ConstantValues.ERROR_MESSAGE, e);
			return ConstantValues.ERROR;
		}
	}

}
