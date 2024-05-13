package com.exam.events;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.exam.forms.checks.ExamTopicMappingCheck;
import com.exam.helper.HibernateHelper;
import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;

/**
 * 
 * @author DELL
 *
 */
public class ExamTopicMappingEvents {
	public static final String MODULE = ExamTopicMappingEvents.class.getName();

	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public static String getSelectedExams(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> combinedMap = UtilHttp.getCombinedMap(request);

		Delegator delegator = (Delegator) combinedMap.get(EntityConstants.DELEGATOR);
		List<Map<String, Object>> viewSelectedTopicsList = new ArrayList<Map<String, Object>>();
		String examId = (String) combinedMap.get(ConstantValues.EXAM_ID);
		List<GenericValue> examForTopics = null;
		try {
			examForTopics = EntityQuery.use(delegator).from(ConstantValues.EXAM_TOPIC_MAPPING)
					.where(ConstantValues.EXAM_ID, examId).queryList();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		Map<String, Object> selectedTopicsInfo = new HashMap<>();
		Debug.log("" + examForTopics);
		if (UtilValidate.isNotEmpty(examForTopics)) {
			for (GenericValue topicPerExam : examForTopics) {
				Map<String, Object> mappedTopics = new HashMap<String, Object>();
				String topicName = "";
				try {
					topicName = EntityQuery.use(delegator).from(ConstantValues.TOPIC_MASTER)
							.where(ConstantValues.TOPIC_ID, topicPerExam.get(ConstantValues.TOPIC_ID)).queryOne()
							.getString(ConstantValues.TOPIC_NAME);
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mappedTopics.put(ConstantValues.TOPIC_ID, topicPerExam.getString(ConstantValues.TOPIC_ID));
				mappedTopics.put(ConstantValues.TOPIC_NAME, topicName);
				viewSelectedTopicsList.add(mappedTopics);
			}
		}
		selectedTopicsInfo.put(ConstantValues.SELECTED_TOPICS, viewSelectedTopicsList);
		request.setAttribute(ConstantValues.SELECTED_TOPICS_INFO, selectedTopicsInfo);
		request.setAttribute(ConstantValues.EXAM_ID, examId);
		return ConstantValues.SUCCESS;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws GenericEntityException
	 */
	public static String calculatePercentage(HttpServletRequest request, HttpServletResponse response)
			throws GenericEntityException {

		Map<String, Object> combinedMap = UtilHttp.getCombinedMap(request);

		Delegator delegator = (Delegator) combinedMap.get(EntityConstants.DELEGATOR);

		List<Map<String, Object>> viewSelectedTopicsList = new ArrayList<Map<String, Object>>();
		String examId = (String) combinedMap.get(ConstantValues.EXAM_ID);
		String percentage = (String) combinedMap.get(ConstantValues.EXAM_TOPIC_PERCENTAGE);
		Integer percentageForExam = 0;
		Integer updatedPercentageForExam = Integer.parseInt(percentage);
		String questionsPerTopic = (String) combinedMap.get(ConstantValues.TOPIC_QUES_PER_EXAM);
		Integer questionsForExam = 0;
		GenericValue exam = EntityQuery.use(delegator).from(ConstantValues.EXAM_MASTER)
				.where(ConstantValues.EXAM_ID, examId).queryOne();
		String examQues = exam.getString(ConstantValues.EXAM_TOTAL_QUES);
		Integer updatedQuestions = 0;
		Integer topicQuestionsPerExam = (int) ((Float.valueOf(percentage) / 100) * Integer.valueOf(examQues));

		List<GenericValue> examForTopics = EntityQuery.use(delegator).from(ConstantValues.EXAM_TOPIC_MAPPING)
				.where(ConstantValues.EXAM_ID, examId).queryList();
		Map<String, Object> selectedTopicsInfo = new HashMap<>();
		for (GenericValue topicPerExam : examForTopics) {
			Map<String, Object> mappedTopics = new HashMap<String, Object>();
			String topicName = EntityQuery.use(delegator).from(ConstantValues.TOPIC_MASTER)
					.where(ConstantValues.TOPIC_ID, topicPerExam.get(ConstantValues.TOPIC_ID)).queryOne()
					.getString(ConstantValues.TOPIC_NAME);
			String percentageCount = topicPerExam.getString(ConstantValues.EXAM_TOPIC_PERCENTAGE);
			String questionsCount = topicPerExam.getString(ConstantValues.TOPIC_QUES_PER_EXAM);
			questionsForExam = questionsForExam + Integer.parseInt(questionsCount);
			percentageForExam = percentageForExam + new BigDecimal(percentageCount).intValue();
			updatedPercentageForExam = updatedPercentageForExam + new BigDecimal(percentageCount).intValue();
			mappedTopics.put(ConstantValues.TOPIC_ID, topicPerExam.getString(ConstantValues.TOPIC_ID));
			mappedTopics.put(ConstantValues.TOPIC_NAME, topicName);
			viewSelectedTopicsList.add(mappedTopics);
		}
		if (updatedPercentageForExam > 100) {
			String warningMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
					ConstantValues.PERCENTAGE_WARNING_MESSAGE, UtilHttp.getLocale(request));
			percentageForExam = 100 - percentageForExam;
			selectedTopicsInfo.put(ConstantValues.SELECTED_TOPICS, viewSelectedTopicsList);
			selectedTopicsInfo.put(ConstantValues.EXAM_ID, examId);
			request.setAttribute(ConstantValues.SELECTED_TOPICS_INFO, selectedTopicsInfo);
			request.setAttribute(ConstantValues.MESSAGE, warningMessage);
			request.setAttribute(ConstantValues.PERCENTAGE, percentageForExam);
		} else {
			String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
					ConstantValues.PERCENTAGE_SUCCESS_MESSAGE, UtilHttp.getLocale(request));
			Debug.log(successMessage + "" + updatedPercentageForExam);
			request.setAttribute(ConstantValues.MESSAGE, successMessage);
			request.setAttribute(ConstantValues.PERCENTAGE, updatedPercentageForExam);
			selectedTopicsInfo.put(ConstantValues.SELECTED_TOPICS, viewSelectedTopicsList);
			selectedTopicsInfo.put(ConstantValues.EXAM_ID, examId);
			request.setAttribute(ConstantValues.SELECTED_TOPICS_INFO, selectedTopicsInfo);
		}
		updatedQuestions = Integer.parseInt(examQues) - questionsForExam;
		request.setAttribute(ConstantValues.QUESTIONS, updatedQuestions);

		request.setAttribute(ConstantValues.TOPIC_QUESTIONS_PEREXAM, topicQuestionsPerExam);
		return ConstantValues.SUCCESS;
	}

	/**
	 * Method to insert data into ExamTopicMapping Entity
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws GenericEntityException
	 */

	public static String createTopicForExam(HttpServletRequest request, HttpServletResponse response)
			throws GenericEntityException {
		Locale locale = UtilHttp.getLocale(request);
		Map<String, Object> combinedMap = UtilHttp.getCombinedMap(request);

		Delegator delegator = (Delegator) combinedMap.get(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) combinedMap.get(EntityConstants.DISPATCHER);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);
		String examId = (String) combinedMap.get(ConstantValues.EXAM_ID);
		String topicId = (String) combinedMap.get(ConstantValues.TOPIC_ID);
		String percentage = (String) combinedMap.get(ConstantValues.EXAM_TOPIC_PERCENTAGE);
		String topicPassPercentage = (String) combinedMap.get(ConstantValues.EXAM_TOPIC_PASS_PERCENTAGE);
		String questionsPerTopic = (String) combinedMap.get(ConstantValues.TOPIC_QUES_PER_EXAM);
		Integer percentageForExam = 0;
		Integer updatedPercentageForExam = Integer.parseInt(percentage);
		Integer questionsForExam = 0;
		GenericValue exam = EntityQuery.use(delegator).from(ConstantValues.EXAM_MASTER)
				.where(ConstantValues.EXAM_ID, examId).queryOne();

		Map<String, Object> examTopicInfo = UtilMisc.toMap(ConstantValues.EXAM_ID, examId, ConstantValues.TOPIC_ID,
				topicId, ConstantValues.EXAM_TOPIC_PERCENTAGE, percentage, ConstantValues.EXAM_TOPIC_PASS_PERCENTAGE,
				topicPassPercentage, ConstantValues.TOPIC_QUES_PER_EXAM, questionsPerTopic, EntityConstants.USER_LOGIN,
				userLogin);
		List<GenericValue> questionsPerTopicList = EntityQuery.use(delegator).from(ConstantValues.EXAM_TOPIC_MAPPING)
				.where(ConstantValues.EXAM_ID, examId).queryList();
		try {
			Integer examQues = Integer.parseInt(exam.getString(ConstantValues.EXAM_TOTAL_QUES));
			for (GenericValue questionPerTopic : questionsPerTopicList) {
				String questionsCount = questionPerTopic.getString(ConstantValues.TOPIC_QUES_PER_EXAM);
				questionsForExam = questionsForExam + Integer.parseInt(questionsCount);
			}

			for (GenericValue percentagePerTopic : questionsPerTopicList) {
				String percentageCount = percentagePerTopic.getString(ConstantValues.EXAM_TOPIC_PERCENTAGE);
				percentageForExam = percentageForExam + new BigDecimal(percentageCount).intValue();
				updatedPercentageForExam = updatedPercentageForExam + new BigDecimal(percentageCount).intValue();
			}
			Integer checkQuestionsCount = questionsForExam + Integer.parseInt(questionsPerTopic);
			Integer updatedQuestions = examQues - questionsForExam - Integer.parseInt(questionsPerTopic);

			if (checkQuestionsCount <= examQues) {
				Map<String, Object> createExamTopicMappingInfoResult = dispatcher
						.runSync(ConstantValues.CREATE_EXAM_TOPIC_MAPPING, examTopicInfo);
				Debug.log("" + createExamTopicMappingInfoResult);
				ServiceUtil.getMessages(request, createExamTopicMappingInfoResult, null);
				if (ServiceUtil.isError(createExamTopicMappingInfoResult)) {
					String errorMessage = ServiceUtil.getErrorMessage(createExamTopicMappingInfoResult);
					request.setAttribute(ConstantValues.ERROR, errorMessage);
					request.setAttribute(ConstantValues.PERCENTAGE, percentageForExam);
					return ConstantValues.ERROR;
				} else {
					String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
							ConstantValues.EXAM_FOR_TOPIC_SUCCESS_MESSAGE, UtilHttp.getLocale(request));
					ServiceUtil.getMessages(request, createExamTopicMappingInfoResult, successMessage);
					request.setAttribute(ConstantValues.SUCCESS_MESSAGE, successMessage);
					request.setAttribute(ConstantValues.PERCENTAGE, updatedPercentageForExam);
					request.setAttribute(ConstantValues.QUESTIONS, updatedQuestions);
					return ConstantValues.SUCCESS;
				}
			} else {
				String errMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
						ConstantValues.EXAM_FOR_TOPIC_WARNING_MESSAGE, UtilHttp.getLocale(request));
				request.setAttribute(ConstantValues.ERROR, errMessage);
				return ConstantValues.ERROR;
			}
		} catch (GenericServiceException e) {
			return ConstantValues.ERROR;
		}

	}

	/**
	 * Method to retrieve data's from ExamTopicMapping Entity
	 * 
	 * @param request
	 * @param response
	 * @return
	 */

	public static String fetchAllExamTopics(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		List<Map<String, Object>> viewExamTopicMapList = new ArrayList<Map<String, Object>>();
		try {
			// Query to retrieve data's from ExamTopicMapping Entity
			List<GenericValue> listOfExamTopicMapData = EntityQuery.use(delegator)
					.from(ConstantValues.EXAM_TOPIC_MAPPING).queryList();
			if (UtilValidate.isNotEmpty(listOfExamTopicMapData)) {
				for (GenericValue topicOfExam : listOfExamTopicMapData) {
					Map<String, Object> topicToMap = new HashMap<String, Object>();
					try {
						String examName = EntityQuery.use(delegator).from(ConstantValues.EXAM_MASTER)
								.where(ConstantValues.EXAM_ID, topicOfExam.get(ConstantValues.EXAM_ID)).queryOne()
								.getString(ConstantValues.EXAM_NAME);
						String topicName = EntityQuery.use(delegator).from(ConstantValues.TOPIC_MASTER)
								.where(ConstantValues.TOPIC_ID, topicOfExam.get(ConstantValues.TOPIC_ID)).queryOne()
								.getString(ConstantValues.TOPIC_NAME);
						if (UtilValidate.isEmpty(examName)) {
							Debug.logInfo("ExamName: ", examName);
							Debug.logInfo("TopicName: ", topicName);
						} else {
							Debug.logInfo("ExamName: ", examName);
							Debug.logInfo("TopicName: ", topicName);
							topicToMap.put(ConstantValues.EXAM_ID, examName);
							topicToMap.put(ConstantValues.TOPIC_ID, topicName);

						}
					} catch (Exception e) {
						Debug.logError(e, MODULE);
					}
					topicToMap.put(ConstantValues.EXAM_TOPIC_PERCENTAGE,
							topicOfExam.get(ConstantValues.EXAM_TOPIC_PERCENTAGE));
					topicToMap.put(ConstantValues.EXAM_TOPIC_PASS_PERCENTAGE,
							topicOfExam.get(ConstantValues.EXAM_TOPIC_PASS_PERCENTAGE));
					topicToMap.put(ConstantValues.TOPIC_QUES_PER_EXAM,
							topicOfExam.get(ConstantValues.TOPIC_QUES_PER_EXAM));
					viewExamTopicMapList.add(topicToMap);

				}
				Map<String, Object> examforTopicsInfo = new HashMap<>();
				examforTopicsInfo.put(ConstantValues.EXAM_TOPIC_LIST, viewExamTopicMapList);
				request.setAttribute(ConstantValues.EXAM_TOPICS_INFO, examforTopicsInfo);
				return ConstantValues.SUCCESS;
			}
			String errorMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
					ConstantValues.ERROR_IN_FETCHING_DATA, UtilHttp.getLocale(request));// "No matched fields in
																						// ExamTopicMapping Entity";
			request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
			Debug.logError(errorMessage, MODULE);
			return ConstantValues.ERROR;

		} catch (GenericEntityException e) {
			request.setAttribute(ConstantValues.ERROR, e);
			return ConstantValues.ERROR;
		}
	}

	// Method to Update data into ExamTopicMapping Entity
	public static String updateTopicForExam(HttpServletRequest request, HttpServletResponse response) {
		Locale locale = UtilHttp.getLocale(request);

		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);

		String examId = (String) request.getAttribute(ConstantValues.EXAM_ID);
		String topicId = (String) request.getAttribute(ConstantValues.TOPIC_ID);
		String percentage = (String) request.getAttribute(ConstantValues.EXAM_TOPIC_PERCENTAGE);
		String topicPassPercentage = (String) request.getAttribute(ConstantValues.EXAM_TOPIC_PASS_PERCENTAGE);
		String questionsPerExam = (String) request.getAttribute(ConstantValues.TOPIC_QUES_PER_EXAM);

		Map<String, Object> examtopicinfo = UtilMisc.toMap(ConstantValues.EXAM_ID, examId, ConstantValues.TOPIC_ID,
				topicId, ConstantValues.EXAM_TOPIC_PERCENTAGE, percentage, ConstantValues.EXAM_TOPIC_PASS_PERCENTAGE,
				topicPassPercentage, ConstantValues.TOPIC_QUES_PER_EXAM, questionsPerExam, EntityConstants.USER_LOGIN,
				userLogin);

		try {
			HibernateValidationMaster hibernate = HibernateHelper.populateBeanFromMap(examtopicinfo,
					HibernateValidationMaster.class);

			Set<ConstraintViolation<HibernateValidationMaster>> errors = HibernateHelper
					.checkValidationErrors(hibernate, ExamTopicMappingCheck.class);
			boolean hasFormErrors = HibernateHelper.validateFormSubmission(delegator, errors, request, locale,
					"Mandatory Err ExamTopicMapping Entity", ConstantValues.ONLINE_EXAM_UI_LABELS, false);

			if (hasFormErrors) {
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errors);
				return ConstantValues.ERROR;
			}
			try {
				// Calling Entity-Auto Service to Update data into ExamTopicMapping
				Map<String, ? extends Object> updateExamTopicMappingInfoResult = dispatcher
						.runSync(ConstantValues.UPDATE_EXAM_TOPIC_MAPPING_MASTER, examtopicinfo);
				ServiceUtil.getMessages(request, updateExamTopicMappingInfoResult, null);
				if (ServiceUtil.isError(updateExamTopicMappingInfoResult)) {
					String errorMessage = ServiceUtil.getErrorMessage(updateExamTopicMappingInfoResult);
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
					Debug.logError(errorMessage, MODULE);
					return ConstantValues.ERROR;
				}
				String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
						ConstantValues.SERVICE_SUCCESS_MESSAGE, UtilHttp.getLocale(request));
				ServiceUtil.getMessages(request, updateExamTopicMappingInfoResult, successMessage);
				request.setAttribute(ConstantValues.SUCCESS_MESSAGE, successMessage);
				return ConstantValues.SUCCESS;

			} catch (GenericServiceException e) {
				String errMsg = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
						ConstantValues.SERVICE_CALLING_ERROR, UtilHttp.getLocale(request)) + e.toString();
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
