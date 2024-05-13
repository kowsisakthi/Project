package com.exam.events;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;
/**
 * 
 * @author DELL
 *
 */
public class QuestionInformation {
	// Define a constant for the class name
	public static final String MODULE_NAME = QuestionInformation.class.getName();
/**
 * 
 * @param request
 * @param response
 * @return
 */
	public static String getQuestionInfo(HttpServletRequest request, HttpServletResponse response) {
		// Retrieve the LocalDispatcher and Delegator from the request attributes
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);

		// Retrieve examId, noOfQuestions, and initialize sequenceNum and performanceId
		String examId = request.getAttribute(ConstantValues.EXAM_ID).toString();
		String partyId = userLogin.getString(ConstantValues.PARTY_ID);
		String noOfQuestions = request.getAttribute(ConstantValues.EXAM_TOTAL_QUES).toString();
		int sequenceNum = 1;
		String performanceId =(String) request.getSession().getAttribute(ConstantValues.USER_ATTEMPT_PERFORMANCE_ID);
		if (UtilValidate.isEmpty(performanceId)) {
			try {
				// Validate examId
				if (UtilValidate.isEmpty(partyId)) {
					String errMsg = ConstantValues.PARTY_ID
							+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
					return ConstantValues.ERROR;
				}

				// Validate noOfQuestions
				if (UtilValidate.isEmpty(noOfQuestions)) {
					String errMsg = ConstantValues.USER_ATTEMPT_TOTAL_QUES
							+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
					;
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
					return ConstantValues.ERROR;
				}


				// Query UserExamMapping for the given examId
				GenericValue userExamMapping = EntityQuery.use(delegator).from(ConstantValues.USER_EXAM_MAPPING)
						.where(ConstantValues.EXAM_ID, examId, ConstantValues.PARTY_ID, partyId).queryOne();

				// Retrieve necessary information from UserExamMapping
				String noOfAttempt = userExamMapping.getString(ConstantValues.USEREXAM_NO_OF_ATTEMPTS);
				String allowedAttempt = userExamMapping.getString(ConstantValues.USEREXAM_ALLOWED_ATTEMPTS);
				Integer allowedAttemptInt = Integer.parseInt(allowedAttempt);
				Integer attemptCount = Integer.parseInt(noOfAttempt);
				noOfAttempt = String.valueOf(attemptCount + 1);
				Integer noOfAttemptExam = Integer.parseInt(noOfAttempt);
				if (allowedAttemptInt >= noOfAttemptExam) {
					// Call service to create a UserAttemptMaster record
					Map<String, Object> userAttemptMasterResult = dispatcher.runSync(ConstantValues.CREATE_USER_ATTEMPT_MASTER,
							UtilMisc.toMap(ConstantValues.EXAM_ID, examId, ConstantValues.EXAM_TOTAL_QUES,
									noOfQuestions, ConstantValues.PARTY_ID, partyId, ConstantValues.USER_ATTEMPT_NUMBER,
									noOfAttempt, EntityConstants.USER_LOGIN, userLogin));

					// Check if the service call resulted in an error
					if (ServiceUtil.isError(userAttemptMasterResult)) {
						String errorMessage = ServiceUtil.getErrorMessage(userAttemptMasterResult);
						request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
						Debug.logError(errorMessage, MODULE_NAME);
						return ConstantValues.ERROR;
					} else {
						// Handle success scenario
						String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_SUCCESS_MESSAGE,
								UtilHttp.getLocale(request));
						ServiceUtil.getMessages(request, userAttemptMasterResult, successMessage);
						performanceId = userAttemptMasterResult.get(ConstantValues.USER_ATTEMPT_PERFORMANCE_ID)
								.toString();
					}
				} else {
					String errorMessage =  UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.ATTEMPT_ERROR,
							UtilHttp.getLocale(request));
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
					Debug.logError(errorMessage, MODULE_NAME);
					return ConstantValues.ERROR;
				}

				if (UtilValidate.isEmpty(performanceId)) {
					String errMsg = ConstantValues.USER_ATTEMPT_PERFORMANCE_ID
							+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
					;
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
					return ConstantValues.ERROR;
				}


				// Query ExamTopicMapping for the given examId
				List<GenericValue> examTopicMapping = EntityQuery.use(delegator).from(ConstantValues.EXAM_TOPIC_MAPPING)
						.where(ConstantValues.EXAM_ID, examId).queryList();

				// Check if examTopicList is empty
				if (UtilValidate.isEmpty(examTopicMapping)) {
					String errMsg = ConstantValues.EXAM_TOPIC_LIST
							+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
					;
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
					return ConstantValues.ERROR;
				}

				// Process each ExamTopicMapping
				for (GenericValue oneExamTopic : examTopicMapping) {
					// Retrieve necessary information from ExamTopicMapping
					String topicId = oneExamTopic.getString(ConstantValues.TOPIC_ID);
					String topicPassPercentage = oneExamTopic.getString(ConstantValues.EXAM_TOPIC_PASS_PERCENTAGE);
					String questionsPerExam = oneExamTopic.getString(ConstantValues.TOPIC_QUES_PER_EXAM);

					// Validate topic information
					if (UtilValidate.isEmpty(topicId) || UtilValidate.isEmpty(topicPassPercentage)
							|| UtilValidate.isEmpty(questionsPerExam)) {
						String errMsg = ConstantValues.TOPIC_INFORMATION + UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.EMPTY_VARIABLE_MESSAGE,
								UtilHttp.getLocale(request));
						;
						request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
						return ConstantValues.ERROR;
					}

					// Call service to create a UserAttemptTopicMaster record
					Map<String, Object> userAttemptTopicMasterResult = dispatcher.runSync(
							ConstantValues.CREATE_USER_ATTEMPT_TOPIC_MASTER,
							UtilMisc.toMap(ConstantValues.TOPIC_ID, topicId, ConstantValues.USER_ANSWER_PERFORMANCE_ID,
									performanceId, ConstantValues.EXAM_TOPIC_PASS_PERCENTAGE, topicPassPercentage,
									ConstantValues.USER_TOPIC_TOTAL_QUES, questionsPerExam, EntityConstants.USER_LOGIN,
									userLogin));

					// Check if the service call resulted in an error
					if (ServiceUtil.isError(userAttemptTopicMasterResult)) {
						String errorMessage = ServiceUtil.getErrorMessage(userAttemptTopicMasterResult);
						request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
						Debug.logError(errorMessage, MODULE_NAME);
						return ConstantValues.ERROR;
					} else {
						// Handle success scenario
						String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_SUCCESS_MESSAGE,
								UtilHttp.getLocale(request));
						ServiceUtil.getMessages(request, userAttemptTopicMasterResult, successMessage);
						request.setAttribute(ConstantValues.SUCCESS_MESSAGE, successMessage);
					}

					// Call service to update UserExamMapping with noOfAttempts
					Map<String, Object> userExamMappingNoOfAttemptsResult = dispatcher.runSync(
							ConstantValues.UPDATE_USEREXAM_MAPPING_NO_OF_ATTEMPTS,
							UtilMisc.toMap(ConstantValues.EXAM_ID, examId, ConstantValues.USEREXAM_NO_OF_ATTEMPTS,
									noOfAttempt, ConstantValues.PARTY_ID, partyId, EntityConstants.USER_LOGIN,
									userLogin));

					// Check if the service call resulted in an error
					if (ServiceUtil.isError(userExamMappingNoOfAttemptsResult)) {
						String errorMessage = ServiceUtil.getErrorMessage(userExamMappingNoOfAttemptsResult);
						request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
						Debug.logError(errorMessage, MODULE_NAME);
						return ConstantValues.ERROR;
					} else {
						// Handle success scenario
						String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_SUCCESS_MESSAGE,
								UtilHttp.getLocale(request));
						ServiceUtil.getMessages(request, userExamMappingNoOfAttemptsResult, successMessage);
						request.setAttribute(ConstantValues.SUCCESS_MESSAGE, successMessage);
					}

					// Call service to get question information
					Map<String, Object> questionInformationResult = dispatcher.runSync(ConstantValues.GET_QUESTION_INFORMATION,
							UtilMisc.toMap(ConstantValues.EXAM_ID, examId, ConstantValues.REQUEST, request,
									EntityConstants.USER_LOGIN, userLogin));

					// Check if the service call resulted in an error
					if (ServiceUtil.isError(questionInformationResult)) {
						String errorMessage = ServiceUtil.getErrorMessage(questionInformationResult);
						request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
						Debug.logError(errorMessage, MODULE_NAME);
						return ConstantValues.ERROR;
					} else {
						// Handle success scenario
						String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_SUCCESS_MESSAGE,
								UtilHttp.getLocale(request));
						ServiceUtil.getMessages(request, questionInformationResult, successMessage);
						request.setAttribute(ConstantValues.QUESTION, questionInformationResult);
						request.getSession().setAttribute(ConstantValues.QUESTIONS, questionInformationResult);
					}
				}

				// Retrieve selectedQuestions from the session
				@SuppressWarnings("unchecked")
				List<List<GenericValue>> questionsInfoList = (List<List<GenericValue>>) request.getSession()
						.getAttribute(ConstantValues.SELECTED_QUESTIONS);

				// Process each question in selectedQuestions
				for (List<GenericValue> questions : questionsInfoList) {
					for (GenericValue oneQuestion : questions) {
						// Retrieve questionId from the GenericValue
						String questionId = oneQuestion.getString(ConstantValues.QUES_ID);

						// Call service to create a UserAttemptAnswerMaster record
						Map<String, Object> resultMap = dispatcher.runSync(ConstantValues.CREATE_USER_ATTEMPT_ANSWER_MASTER,
								UtilMisc.toMap(ConstantValues.QUES_ID, questionId,
										ConstantValues.USER_ANSWER_PERFORMANCE_ID, performanceId, ConstantValues.USER_ANSWER_SEQUENCE_ID,
										sequenceNum, EntityConstants.USER_LOGIN, userLogin));

						// Increment sequenceNum
						++sequenceNum;
						if (!ServiceUtil.isSuccess(resultMap)) {
							String errMsg = ConstantValues.USER_ATTEMPT + UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.EMPTY_VARIABLE_MESSAGE,
									UtilHttp.getLocale(request));
							;
							request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
							return ConstantValues.ERROR;
						}
					}
				}

				// Query UserAttemptAnswerMaster for the given performanceId
				List<GenericValue> userAttemptAnswerMasterList = EntityQuery.use(delegator)
						.from(ConstantValues.USER_ATTEMPT_ANSWER_MASTER).where(ConstantValues.USER_ANSWER_PERFORMANCE_ID, performanceId)
						.queryList();
				if (UtilValidate.isEmpty(userAttemptAnswerMasterList)) {
					String errMsg = ConstantValues.USER_ATTEMPT_ANSWER_MASTER
							+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
					;
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
					return ConstantValues.ERROR;
				}

				// Set questionSequence in the request
				request.setAttribute(ConstantValues.USER_ATTEMPT_ANSWER_MASTER_QUESINFO, userAttemptAnswerMasterList);
			} catch (Exception e) {
				// Handle any exceptions that may occur
				String errMsg = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_CALLING_ERROR, UtilHttp.getLocale(request));
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
				return ConstantValues.ERROR;
			}
			request.setAttribute(ConstantValues.SUCCESS_MESSAGE, ConstantValues.QUESTION_INFORMATION_SUCCESS_MESSAGE);

			request.getSession().setAttribute(ConstantValues.USER_ATTEMPT_PERFORMANCE_ID, performanceId);
			return ConstantValues.SUCCESS;
		}
		 else {
			try {
				List<GenericValue> userAttemptAnswerMasterList = EntityQuery.use(delegator)
						.from(ConstantValues.USER_ATTEMPT_ANSWER_MASTER).where(ConstantValues.USER_ANSWER_PERFORMANCE_ID, performanceId)
						.queryList();
				if (UtilValidate.isEmpty(userAttemptAnswerMasterList)) {
					String errMsg = ConstantValues.USER_ATTEMPT_ANSWER_MASTER
							+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
					;
					request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
					return ConstantValues.ERROR;
				}
				request.setAttribute(ConstantValues.QUESTION, request.getSession().getAttribute(ConstantValues.QUESTIONS));
				// Set questionSequence in the request
			request.setAttribute(ConstantValues.USER_ATTEMPT_ANSWER_MASTER_QUESINFO, userAttemptAnswerMasterList);
			

			} catch (Exception e) {
				String errMsg =  UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_CALLING_ERROR, UtilHttp.getLocale(request));
				request.setAttribute(ConstantValues.ERROR_MESSAGE, errMsg);
				return ConstantValues.ERROR;
			}

		}
		
		request.setAttribute(ConstantValues.SUCCESS_MESSAGE, ConstantValues.QUESTION_INFORMATION_SUCCESS_MESSAGE);
		return ConstantValues.SUCCESS;
		
	}

}
