package com.exam.events;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.lucene.analysis.CharArrayMap.EntrySet;
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
import org.apache.ofbiz.service.ServiceUtil;

import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;

import net.lingala.zip4j.headers.VersionMadeBy;
/**
 * 
 * @author DELL
 *
 */
public class UserResult {
	public static final String MODULE = UserResult.class.getName();
	//private static final String RES_ERR = "OnlineExamPortalUiLabels";
/**
 * 
 * @param request
 * @param response
 * @return
 */
	public static String getUserResult(HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute(EntityConstants.DELEGATOR);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute(EntityConstants.DISPATCHER);
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute(EntityConstants.USER_LOGIN);
		String performanceId = (String) request.getSession().getAttribute(ConstantValues.USER_ANSWER_PERFORMANCE_ID);
		String examId = (String) request.getAttribute(ConstantValues.EXAM_ID);

		List<Map<String, Object>> answers = (List<Map<String, Object>>) request.getAttribute(ConstantValues.USER_SELECTED_ANSWER);

		List<List<Map<String, Object>>> questions = (List<List<Map<String, Object>>>) request.getAttribute(ConstantValues.USER_QUESTIONS);
		int correctAnswerCount = 0, correctAnswerMark = 0, wrongAnswerCount = 0, wrongAnswerMark = 0,
				totalExamMarks = 0;

		Map<String, Integer> topic = new HashMap<>();
		
		try {
			List<GenericValue> examTopicMapping = EntityQuery.use(delegator).from(ConstantValues.EXAM_TOPIC_MAPPING)
					.where(ConstantValues.EXAM_ID, examId).queryList();
			for (GenericValue getTopic : examTopicMapping) {
				String topicId = getTopic.getString(ConstantValues.TOPIC_ID);
				topic.put(topicId, 0);
			}
			for (Map<String, Object> oneAnswer : answers) {

				for (List<Map<String, Object>> questionList : questions) {
					for (Map<String, Object> oneQuestion : questionList) {

						if (oneAnswer.get(ConstantValues.USER_ANSWER_QUESTION_ID).toString().trim()
								.equalsIgnoreCase(oneQuestion.get(ConstantValues.USER_ANSWER_QUESTION_ID).toString().trim())) {
							Integer questionIdInt = (Integer) oneAnswer.get(ConstantValues.USER_ANSWER_QUESTION_ID);
							String questionId = String.valueOf(questionIdInt);
							String selectedAnswer = oneAnswer.get(ConstantValues.QUES_ANSWER).toString();
							int isFlagged = 0;
							Map<String, Object> userAttemptAnswerMasterResult = dispatcher.runSync(
									ConstantValues.UPDATE_USER_ATTEMPT_ANSWER_MASTER,
									UtilMisc.toMap(ConstantValues.QUES_ID, questionId,
											ConstantValues.USER_TOPIC_PERFORMANCE_ID, performanceId,
											ConstantValues.USER_ANSWER_SUBMITTED, selectedAnswer,
											ConstantValues.USER_ANSWER_FLAGGED, isFlagged, EntityConstants.USER_LOGIN,
											userLogin));
							if (ServiceUtil.isError(userAttemptAnswerMasterResult)) {
								String errorMessage = ServiceUtil.getErrorMessage(userAttemptAnswerMasterResult);
								request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
								Debug.logError(errorMessage, MODULE);
								return ConstantValues.ERROR;
							} else {
								// Handle success scenario
								String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_SUCCESS_MESSAGE,
										UtilHttp.getLocale(request));
								ServiceUtil.getMessages(request, userAttemptAnswerMasterResult, successMessage);

							}

							String currentQuestionTopicId = oneQuestion.get(ConstantValues.TOPIC_ID).toString();
							totalExamMarks += (Integer) (oneQuestion.get(ConstantValues.QUES_ANS_VALUE));
							if (oneAnswer.get(ConstantValues.QUES_ANSWER).toString().equals(oneQuestion.get(ConstantValues.QUES_ANSWER))) {

								correctAnswerCount += 1;
								correctAnswerMark += (Integer) (oneQuestion.get(ConstantValues.QUES_ANS_VALUE));
								int topicMark = topic.get(currentQuestionTopicId)
										+ Integer.parseInt(oneQuestion.get(ConstantValues.QUES_ANS_VALUE).toString());
								topic.put(currentQuestionTopicId, topicMark);
							} else {
								System.out.println(oneQuestion.get(ConstantValues.EXAM_NEG_MARK));
								wrongAnswerCount += 1;
								wrongAnswerMark += Integer.parseInt(oneQuestion.get(ConstantValues.EXAM_NEG_MARK).toString());
								int topicMark = topic.get(currentQuestionTopicId)
										+ Integer.parseInt(oneQuestion.get(ConstantValues.EXAM_NEG_MARK).toString());
								topic.put(currentQuestionTopicId, topicMark);
							}

						}
					}
				}
				
			}
			if (!UtilValidate.isEmpty(topic)) {
				for (Entry<String, Integer> getOneTopic : topic.entrySet()) {
					String topicId = getOneTopic.getKey();

					List<GenericValue> userAttemptTopicMaster = EntityQuery.use(delegator)
							.from(ConstantValues.USER_ATTEMPT_TOPIC_MASTER).where(ConstantValues.TOPIC_ID, topicId).queryList();
					for (GenericValue oneUserTopicInformation : userAttemptTopicMaster) {
						Float totalQuestionsInThisTopic = Float
								.valueOf(oneUserTopicInformation.get(ConstantValues.USER_TOPIC_TOTAL_QUES).toString());

						Float correctQuestionsInThisTopic = Float.valueOf(getOneTopic.getValue());
						String correctQuestionsTopic = String.valueOf(correctQuestionsInThisTopic);
						String topicPassPercentageBigDecimal = oneUserTopicInformation.get(ConstantValues.USER_TOPIC_PASS_PERCENTAGE)
								.toString();
						Integer topicPassPercentage = new BigDecimal(topicPassPercentageBigDecimal).intValue();
						Float userTopicPercentageCalculate = (correctQuestionsInThisTopic / totalQuestionsInThisTopic)
								* 100;

						String userPassedThisTopic = "N";
						if (userTopicPercentageCalculate >= topicPassPercentage) {

							userPassedThisTopic = "Y";

						}
						BigDecimal userTopicPercentage = BigDecimal.valueOf(userTopicPercentageCalculate);
						Map<String, Object> userAttemptTopicMasterResult = dispatcher.runSync(
								ConstantValues.UPDATE_USER_ATTEMPT_TOPIC_MASTER,
								UtilMisc.toMap(ConstantValues.USER_TOPIC_PERFORMANCE_ID, performanceId,
										ConstantValues.TOPIC_ID, topicId, ConstantValues.USER_TOPIC_CRCT_QUES,
										correctQuestionsTopic, ConstantValues.USER_TOPIC_PERCENTAGE,
										userTopicPercentage, ConstantValues.USER_TOPIC_PASSED, userPassedThisTopic,
										EntityConstants.USER_LOGIN, userLogin));

						if (ServiceUtil.isError(userAttemptTopicMasterResult)) {
							String errorMessage = ServiceUtil.getErrorMessage(userAttemptTopicMasterResult);
							request.setAttribute(ConstantValues.ERROR_MESSAGE, errorMessage);
							Debug.logError(errorMessage, MODULE);
							return ConstantValues.ERROR;
						} else {
							// Handle success scenario
							String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_SUCCESS_MESSAGE,
									UtilHttp.getLocale(request));
							ServiceUtil.getMessages(request, userAttemptTopicMasterResult, successMessage);

						}

					}

				}
			}
			GenericValue oneExamInfo = EntityQuery.use(delegator).from(ConstantValues.EXAM_MASTER)
					.where(ConstantValues.EXAM_ID, examId).queryOne();
			if (UtilValidate.isEmpty(oneExamInfo)) {
				request.setAttribute(ConstantValues.ERROR_MESSAGE, ConstantValues.EXAM_RESULT_EMPTY_MESSAGE);
				return ConstantValues.ERROR;
			}
			int totalMarks = correctAnswerMark + wrongAnswerMark;
			Integer userMarkPercentage = (correctAnswerMark / totalMarks) * 100;
			String examPassPercentageString = oneExamInfo.get(ConstantValues.EXAM_PASS_PERCENTAGE).toString();
			Integer examPassPercentageCalculate = new BigDecimal(examPassPercentageString).intValue();
			String userPassed = "N";
			if (userMarkPercentage > examPassPercentageCalculate) {
				userPassed = "Y";
			}
			LocalDateTime completedDate = LocalDateTime.now();

			Map<String, Object> userAttemptMasterResult = dispatcher.runSync(ConstantValues.UPDATE_USER_ATTEMPT_MASTER,
					UtilMisc.toMap(ConstantValues.USER_ATTEMPT_PERFORMANCE_ID, performanceId,
							ConstantValues.USER_ATTEMPT_SCORE, correctAnswerMark,
							ConstantValues.USER_ATTEMPT_COMPLETED_DATE, Timestamp.valueOf(completedDate),
							ConstantValues.USER_ATTEMPT_TOTAL_CORRECT, correctAnswerCount,
							ConstantValues.USER_ATTEMPT_TOTAL_WRONG, wrongAnswerCount,
							ConstantValues.USER_ATTEMPT_PASSED, userPassed, EntityConstants.USER_LOGIN, userLogin));

			if (ServiceUtil.isError(userAttemptMasterResult)) {
				String errorMessage = ServiceUtil.getErrorMessage(userAttemptMasterResult);
				request.setAttribute(ConstantValues.ERROR, errorMessage);
				Debug.logError(errorMessage, MODULE);
				return ConstantValues.ERROR;
			} else {
				// Handle success scenario
				String successMessage = UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS, ConstantValues.SERVICE_SUCCESS_MESSAGE,
						UtilHttp.getLocale(request));
				ServiceUtil.getMessages(request, userAttemptMasterResult, successMessage);

			}

			Map<String, Object> updateResult = dispatcher.runSync(ConstantValues.UPDATE_EXAM_MASTER,
					UtilMisc.toMap(ConstantValues.EXAM_ID, examId, ConstantValues.FROM_DATE,
							Timestamp.valueOf(oneExamInfo.getString(ConstantValues.EXAM_CREATION_DATE)),
							ConstantValues.THRESHOLD_DATE, Timestamp.valueOf(LocalDateTime.now()),
							EntityConstants.USER_LOGIN, userLogin));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute(ConstantValues.ERROR, e);
			return ConstantValues.ERROR;
		}

		request.setAttribute(ConstantValues.SUCCESS,
				UtilMisc.toMap(ConstantValues.CORRECT_ANSWER_COUNT, correctAnswerCount, ConstantValues.CORRECT_ANSWER_MARK, correctAnswerMark,
						ConstantValues.WRONG_ANSWER_COUNT, wrongAnswerCount, ConstantValues.WRONG_ANSWER_MARK, wrongAnswerMark, ConstantValues.TOPIC, topic));
	
		return ConstantValues.SUCCESS;
	}
}
