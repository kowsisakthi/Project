package com.exam.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import javax.servlet.http.HttpSession;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.common.login.LoginServices;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.entity.util.EntityUtilProperties;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.exam.util.ConstantValues;
import com.exam.util.EntityConstants;

/**
 * 
 * @author DELL
 *
 */
public class ReportService {
	public static final String module = ReportService.class.getName();
	public static final Map<String, Object> errorMap = new HashMap<>();

	/**
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> fetchUserReport(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> dataResult = new HashMap<String, Object>();
		GenericValue userLogin = (GenericValue) context.get(EntityConstants.USER_LOGIN);
		Random rand = new Random();
		Map<String, Object> combinedMap = (Map<String, Object>) context.get(ConstantValues.COMBINED_MAP);
		Delegator delegator = dctx.getDelegator();
		String partyId = userLogin.getString(ConstantValues.PARTY_ID);
		try {
			List<Map<String, Object>> examList = new ArrayList<>();
			Stack<Map<String, Object>> examInfoList = new Stack<>();
			List<GenericValue> examInfo = EntityQuery.use(delegator).from(ConstantValues.EXAM_MASTER).queryList();
			if (UtilValidate.isEmpty(examInfo)) {
				errorMap.put(ConstantValues.ERROR_MESSAGE, ConstantValues.EXAM_RESULT_EMPTY_MESSAGE);
				return errorMap;
			}
			for (GenericValue oneExamInfo : examInfo) {
				if (oneExamInfo.getString(ConstantValues.THRESHOLD_DATE) == null
						|| oneExamInfo.getString(ConstantValues.THRESHOLD_DATE) == "null") {
					continue;
				}
				Map<String, Object> oneExamInfoMap = UtilMisc.toMap(ConstantValues.EXAM_ID,
						oneExamInfo.getString(ConstantValues.EXAM_ID), ConstantValues.EXAM_NAME,
						oneExamInfo.getString(ConstantValues.EXAM_NAME), ConstantValues.EXAM_TOTAL_QUES,
						oneExamInfo.getString(ConstantValues.EXAM_TOTAL_QUES));
				examInfoList.add(oneExamInfoMap);

			}
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			Map<String, Map<String, Object>> examWisePerformance = new HashMap<>();
			GenericValue latestperformaceId;
			LocalDateTime latestPerformaceTime;
			while (!examInfoList.isEmpty()) {
				latestperformaceId = null;
				latestPerformaceTime = null;
				Map<String, Object> oneExamId = examInfoList.pop();
				Map<String, Object> oneExamPerformanceMap = new HashMap<>();
				List<GenericValue> performanceIdList = EntityQuery.use(delegator)
						.from(ConstantValues.USER_ATTEMPT_MASTER).where(ConstantValues.EXAM_ID,
								oneExamId.get(ConstantValues.EXAM_ID).toString(), ConstantValues.PARTY_ID, partyId)
						.queryList();
				if (performanceIdList.size() <= 0) {

					continue;
				}
				examList.add(oneExamId);
				for (GenericValue onePerformaceIdList : performanceIdList) {
					String thresholdDateString = onePerformaceIdList
							.getString(ConstantValues.USER_ATTEMPT_COMPLETED_DATE);
					LocalDateTime thresholdDate = LocalDateTime
							.parse(thresholdDateString.substring(0, thresholdDateString.length() - 7), formatter);
					if (latestPerformaceTime == null) {
						latestPerformaceTime = thresholdDate;
						latestperformaceId = onePerformaceIdList;
					} else {
						if (thresholdDate.isAfter(latestPerformaceTime)) {
							latestPerformaceTime = thresholdDate;
							latestperformaceId = onePerformaceIdList;
						}
					}
				}
				if (latestperformaceId == null) {
					errorMap.put(ConstantValues.ERROR_MESSAGE, "Latest Performance Search Error");
					return errorMap;
				}
				String performanceIdString = latestperformaceId.getString(ConstantValues.USER_ANSWER_PERFORMANCE_ID);
				Integer performanceId = Integer.parseInt(performanceIdString);
				GenericValue userAttemptMasterEntries = EntityQuery.use(delegator).from(ConstantValues.USER_ATTEMPT_MASTER)
						.where(ConstantValues.USER_ANSWER_PERFORMANCE_ID, performanceId).queryOne();
				if (UtilValidate.isEmpty(userAttemptMasterEntries)) {
					errorMap.put(ConstantValues.ERROR_MESSAGE, "fetch userAttemptMasterEntries has failed");
					return errorMap;
				}
				Map<String, Object> userAttemptMasterMap = UtilMisc.toMap(ConstantValues.USER_ANSWER_PERFORMANCE_ID,
						performanceId, ConstantValues.USER_ATTEMPT_NUMBER,
						userAttemptMasterEntries.getString(ConstantValues.USER_ATTEMPT_NUMBER), ConstantValues.PARTY_ID,
						userAttemptMasterEntries.getString(ConstantValues.PARTY_ID), ConstantValues.EXAM_ID,
						userAttemptMasterEntries.getString(ConstantValues.EXAM_ID), ConstantValues.USER_ATTEMPT_SCORE,
						userAttemptMasterEntries.getString(ConstantValues.USER_ATTEMPT_SCORE),
						ConstantValues.EXAM_TOTAL_QUES,
						userAttemptMasterEntries.getString(ConstantValues.EXAM_TOTAL_QUES),
						ConstantValues.USER_ATTEMPT_TOTAL_CORRECT,
						userAttemptMasterEntries.getString(ConstantValues.USER_ATTEMPT_TOTAL_CORRECT),
						ConstantValues.USER_ATTEMPT_TOTAL_WRONG,
						userAttemptMasterEntries.getString(ConstantValues.USER_ATTEMPT_TOTAL_WRONG),
						ConstantValues.USER_ATTEMPT_PASSED,
						userAttemptMasterEntries.getString(ConstantValues.USER_ATTEMPT_PASSED));
				List<GenericValue> userAttemptTopicMasterEntries = EntityQuery.use(delegator)
						.from(ConstantValues.USER_ATTEMPT_TOPIC_MASTER)
						.where(ConstantValues.USER_ANSWER_PERFORMANCE_ID, performanceId).cache().queryList();
				if (UtilValidate.isEmpty(userAttemptTopicMasterEntries)) {
					errorMap.put(ConstantValues.ERROR_MESSAGE, "fetch userAttemptTopicMasterEntries has failed");
					return errorMap;
				}
				List<String> topicNameList = new ArrayList<>();
				List<Map<String, Object>> userAttemptTopicMasterList = new ArrayList<>();
				for (GenericValue oneUserAttemptTopicMasterEntry : userAttemptTopicMasterEntries) {
					Map<String, Object> userAttemptTopicMasterEntryMap = UtilMisc.toMap(
							ConstantValues.USER_TOPIC_PERFORMANCE_ID,
							oneUserAttemptTopicMasterEntry.getString(ConstantValues.USER_TOPIC_PERFORMANCE_ID),
							ConstantValues.TOPIC_ID, oneUserAttemptTopicMasterEntry.getString(ConstantValues.TOPIC_ID),
							ConstantValues.EXAM_TOPIC_PASS_PERCENTAGE,
							oneUserAttemptTopicMasterEntry.getString(ConstantValues.EXAM_TOPIC_PASS_PERCENTAGE),
							ConstantValues.USER_TOPIC_TOTAL_QUES,
							oneUserAttemptTopicMasterEntry.getString(ConstantValues.USER_TOPIC_TOTAL_QUES),
							ConstantValues.USER_TOPIC_CRCT_QUES,
							oneUserAttemptTopicMasterEntry.getString(ConstantValues.USER_TOPIC_CRCT_QUES),
							ConstantValues.USER_TOPIC_PERCENTAGE,
							oneUserAttemptTopicMasterEntry.getString(ConstantValues.USER_TOPIC_PERCENTAGE),
							ConstantValues.USER_TOPIC_PASSED,
							oneUserAttemptTopicMasterEntry.getString(ConstantValues.USER_TOPIC_PASSED));
					GenericValue oneTopicEntry = EntityQuery.use(delegator).from(ConstantValues.TOPIC_MASTER)
							.where(ConstantValues.TOPIC_ID, userAttemptTopicMasterEntryMap.get(ConstantValues.TOPIC_ID))
							.queryOne();
					if (UtilValidate.isNotEmpty(oneTopicEntry)) {
						topicNameList.add(oneTopicEntry.getString(ConstantValues.TOPIC_NAME));
					}
					userAttemptTopicMasterList.add(userAttemptTopicMasterEntryMap);
				}
				List<GenericValue> userAttemptAnswerEntries = EntityQuery.use(delegator)
						.from(ConstantValues.USER_ATTEMPT_ANSWER_MASTER)
						.where(ConstantValues.USER_ANSWER_PERFORMANCE_ID, performanceId.toString()).queryList();
				if (UtilValidate.isEmpty(userAttemptAnswerEntries)) {
					errorMap.put(ConstantValues.ERROR_MESSAGE, "fetch userAttemptAnswerEntries has failed");
					return errorMap;
				}
				List<Map<String, Object>> userAttemptAnswerMasterList = new ArrayList<>();
				List<Map<String, Object>> questions = new ArrayList<>();
				for (GenericValue oneUserAttemptAnswerEntry : userAttemptAnswerEntries) {
					Map<String, Object> userAttemptAnswerEntryMap = UtilMisc.toMap(ConstantValues.QUES_ID,
							oneUserAttemptAnswerEntry.getString(ConstantValues.QUES_ID),
							ConstantValues.USER_TOPIC_PERFORMANCE_ID,
							oneUserAttemptAnswerEntry.getString(ConstantValues.USER_TOPIC_PERFORMANCE_ID),
							ConstantValues.USER_ANSWER_SUBMITTED,
							oneUserAttemptAnswerEntry.getString(ConstantValues.USER_ANSWER_SUBMITTED),
							ConstantValues.USER_ANSWER_FLAGGED,
							oneUserAttemptAnswerEntry.getString(ConstantValues.USER_ANSWER_FLAGGED),
							ConstantValues.USER_ANSWER_SEQUENCE_ID,
							oneUserAttemptAnswerEntry.getString(ConstantValues.USER_ANSWER_SEQUENCE_ID));
					userAttemptAnswerMasterList.add(userAttemptAnswerEntryMap);
					GenericValue oneQuestion = EntityQuery.use(delegator).from(ConstantValues.QUESTION_MASTER)
							.where(ConstantValues.QUES_ID,
									Long.valueOf(oneUserAttemptAnswerEntry.getString(ConstantValues.QUES_ID)))
							.cache().queryOne();
					Map<String, Object> questionMap = new HashMap<String, Object>();
					questionMap.put(ConstantValues.QUES_ID, oneQuestion.get(ConstantValues.QUES_ID));
					questionMap.put(ConstantValues.QUES_DETAIL, oneQuestion.get(ConstantValues.QUES_DETAIL));
					questionMap.put(ConstantValues.QUES_OPTION_A, oneQuestion.get(ConstantValues.QUES_OPTION_A));
					questionMap.put(ConstantValues.QUES_OPTION_B, oneQuestion.get(ConstantValues.QUES_OPTION_B));
					questionMap.put(ConstantValues.QUES_OPTION_C, oneQuestion.get(ConstantValues.QUES_OPTION_C));
					questionMap.put(ConstantValues.QUES_OPTION_D, oneQuestion.get(ConstantValues.QUES_OPTION_D));
					questionMap.put(ConstantValues.QUES_OPTION_E, oneQuestion.get(ConstantValues.QUES_OPTION_E));
					questionMap.put(ConstantValues.QUES_ANSWER, oneQuestion.get(ConstantValues.QUES_ANSWER));
					questionMap.put(ConstantValues.QUES_NUM_ANS, oneQuestion.get(ConstantValues.QUES_NUM_ANS));
					questionMap.put(ConstantValues.QUES_TYPE, oneQuestion.get(ConstantValues.QUES_TYPE));
					questionMap.put(ConstantValues.QUES_DIFFICULTY_LEVEL,
							oneQuestion.get(ConstantValues.QUES_DIFFICULTY_LEVEL));
					questionMap.put(ConstantValues.QUES_ANS_VALUE, oneQuestion.get(ConstantValues.QUES_ANS_VALUE));
					questionMap.put(ConstantValues.QUES_TOPIC_ID, oneQuestion.get(ConstantValues.QUES_TOPIC_ID));
					questionMap.put(ConstantValues.QUES_NEG_MARK, oneQuestion.get(ConstantValues.QUES_NEG_MARK));
					questions.add(questionMap);

				}
				oneExamPerformanceMap.put(ConstantValues.USER_ATTEMPT_MASTER_MAP, userAttemptMasterMap);
				oneExamPerformanceMap.put(ConstantValues.USER_ATTEMPT_TOPIC_MASTER_LIST, userAttemptTopicMasterList);
				oneExamPerformanceMap.put(ConstantValues.USER_ATTEMPT_ANSWER_MASTER_LIST, userAttemptAnswerMasterList);
				oneExamPerformanceMap.put(ConstantValues.QUESTIONS_, questions);
				oneExamPerformanceMap.put(ConstantValues.TOPIC_NAME_LIST, topicNameList);
				examWisePerformance.put(oneExamId.get(ConstantValues.EXAM_ID).toString(), oneExamPerformanceMap);
			}
			result.put(ConstantValues.EXAM_WISE_PERFORMANCE_, examWisePerformance);
			result.put(ConstantValues.EXAM_LIST_, examList);
			return result;
		} catch (Exception e) {
			Debug.logError(e, module);
			return ServiceUtil.returnError("Error in Login Service java class ........Invalid email" + module);
		}
	}
}
