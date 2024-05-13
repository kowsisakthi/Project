package com.exam.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;

import com.exam.util.ConstantValues;

/**
 * 
 * @author DELL
 *
 */
public class ExamInfoService {
	public static final String MODULE_NAME = ExamInfoService.class.getName();
	public static final Map<String, Object> emptyMap = UtilMisc.toMap(ConstantValues.STATUS, ConstantValues.ERROR);

	// private static final String RES_ERR = "OnlineExamPortalUiLabels";
	/**
	 * Service method to get exam information based on user login ID
	 * 
	 * @param dispatchContext
	 * @param context
	 * @return
	 */

	public static Map<String, Object> getExamInfo(DispatchContext dispatchContext,
			Map<String, ? extends Object> context) {
		HttpServletRequest request = (HttpServletRequest) context.get(ConstantValues.REQUEST);

		Delegator delegator = dispatchContext.getDelegator();

		try {
			// Get partyId from UserLogin entity
			String partyId = ((GenericValue) context.get(ConstantValues.USER_LOGIN)).getString(ConstantValues.PARTY_ID);

			// Check if partyId is empty
			if (UtilValidate.isEmpty(partyId)) {
				String errMsg = ConstantValues.PARTY_ID
						+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
								ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
				Debug.log(errMsg);
				return emptyMap; // Return early if partyId is empty
			}

			// Query UserExamMapping entity
			List<GenericValue> userExamMappingList = EntityQuery.use(delegator).from(ConstantValues.USER_EXAM_MAPPING)
					.where(ConstantValues.PARTY_ID, partyId).queryList();

			// Check if UserExamMapping entity is empty
			if (UtilValidate.isEmpty(userExamMappingList)) {
				String errMsg = ConstantValues.USER_EXAM_MAPPING
						+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
								ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
				Debug.log(errMsg);
				return emptyMap; // Return early if UserExamMapping entity is empty
			}

			List<Map<String, Object>> examList = new ArrayList<>();
			// Process each UserExamMapping
			for (GenericValue userExamMapping : userExamMappingList) {
				String examId = userExamMapping.getString(ConstantValues.EXAM_ID);
				String allowedAttempts = userExamMapping.getString(ConstantValues.USEREXAM_ALLOWED_ATTEMPTS);
				String noOfAttempts = userExamMapping.getString(ConstantValues.USEREXAM_NO_OF_ATTEMPTS);
				Integer allowedAttemptInt = Integer.parseInt(allowedAttempts);
				Integer attemptCount = Integer.parseInt(noOfAttempts);

				// Check if examId is empty
				if (UtilValidate.isEmpty(examId)) {
					String errMsg = ConstantValues.EXAM_ID
							+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
									ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
					Debug.log(errMsg);
					return emptyMap; // Return early if examId is empty
				}
				if (allowedAttemptInt > attemptCount) {
//					

					// Query ExamMaster entity

					GenericValue examMasterEntity = EntityQuery.use(delegator).from(ConstantValues.EXAM_MASTER)
							.where(ConstantValues.EXAM_ID, examId).queryOne();

					// Check if ExamMaster entity is empty
					if (UtilValidate.isEmpty(examMasterEntity)) {
						String errMsg = ConstantValues.EXAM_MASTER
								+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
										ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
						Debug.log(errMsg);
						return emptyMap; // Return early if ExamMaster entity is empty
					}
					// Expiration Date
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					String expirationDateString = examMasterEntity.getString(ConstantValues.EXAM_EXPIRATION_DATE);
					LocalDateTime expirationDate = LocalDateTime
							.parse(expirationDateString.substring(0, expirationDateString.length() - 2), formatter);
					if (UtilValidate.isEmpty(expirationDate)) {
						String errMsg = ConstantValues.EXAM_EXPIRATION_DATE
								+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
										ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
						Debug.log(errMsg);
						return emptyMap;
					}
					if (expirationDate.isAfter(LocalDateTime.now())) {
						// Create a map to store exam details
						Map<String, Object> examDetailsMap = new HashMap<>();
						examDetailsMap.put(ConstantValues.EXAM_EXPIRATION_DATE, expirationDate.toString());
						examDetailsMap.put(ConstantValues.EXAM_ID, examId);

						// Exam Name
						String examName = (String) examMasterEntity.getString(ConstantValues.EXAM_NAME);
						if (UtilValidate.isEmpty(examName)) {
							String errMsg = ConstantValues.EXAM_NAME
									+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
											ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
							Debug.log(errMsg);
							return emptyMap;
						}
						examDetailsMap.put(ConstantValues.EXAM_NAME, examName);

						// Description
						String description = (String) examMasterEntity.getString(ConstantValues.EXAM_DESCRIPTION);
						if (UtilValidate.isEmpty(description)) {
							String errMsg = ConstantValues.EXAM_DESCRIPTION
									+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
											ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
							Debug.log(errMsg);
							return emptyMap;
						}
						examDetailsMap.put(ConstantValues.EXAM_DESCRIPTION, description);

						// Creation Date
						String creationDate = (String) examMasterEntity.getString(ConstantValues.EXAM_CREATION_DATE);
						if (UtilValidate.isEmpty(creationDate)) {
							String errMsg = ConstantValues.EXAM_CREATION_DATE
									+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
											ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
							Debug.log(errMsg);
							return emptyMap;
						}
						examDetailsMap.put(ConstantValues.EXAM_CREATION_DATE, creationDate);

						// No Of Questions
						String noOfQuestions = (String) examMasterEntity.getString(ConstantValues.EXAM_TOTAL_QUES);
						if (UtilValidate.isEmpty(noOfQuestions)) {
							String errMsg = ConstantValues.EXAM_TOTAL_QUES
									+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
											ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
							Debug.log(errMsg);
							return emptyMap;
						}
						examDetailsMap.put(ConstantValues.EXAM_TOTAL_QUES, noOfQuestions);

						// Duration Minutes
						String durationMinutes = (String) examMasterEntity.getString(ConstantValues.EXAM_DURATION);
						if (UtilValidate.isEmpty(durationMinutes)) {
							String errMsg = ConstantValues.EXAM_DURATION
									+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
											ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
							Debug.log(errMsg);
							return emptyMap;
						}
						examDetailsMap.put(ConstantValues.EXAM_DURATION, durationMinutes);

						// Pass Percentage
						String passPercentage = (String) examMasterEntity
								.getString(ConstantValues.EXAM_PASS_PERCENTAGE);
						if (UtilValidate.isEmpty(passPercentage)) {
							String errMsg = ConstantValues.EXAM_PASS_PERCENTAGE
									+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
											ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
							Debug.log(errMsg);
							return emptyMap;
						}
						examDetailsMap.put(ConstantValues.EXAM_PASS_PERCENTAGE, passPercentage);

						// Questions Randomized
						String questionsRandomized = (String) examMasterEntity
								.getString(ConstantValues.EXAM_QUES_RANDOMIZED);
						if (UtilValidate.isEmpty(questionsRandomized)) {
							String errMsg = ConstantValues.EXAM_QUES_RANDOMIZED
									+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
											ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
							Debug.log(errMsg);
							return emptyMap;
						}
						examDetailsMap.put(ConstantValues.EXAM_QUES_RANDOMIZED, questionsRandomized);

						// Answers Must
						String answersMust = (String) examMasterEntity.getString(ConstantValues.EXAM_ANS_MUST);
						if (UtilValidate.isEmpty(answersMust)) {
							String errMsg = ConstantValues.EXAM_ANS_MUST
									+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
											ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
							Debug.log(errMsg);
							return emptyMap;
						}
						examDetailsMap.put(ConstantValues.EXAM_ANS_MUST, answersMust);

						// Enable Negative Mark
						String enableNegativeMark = (String) examMasterEntity
								.getString(ConstantValues.EXAM_ENABLE_NEG_MARK);
						if (UtilValidate.isEmpty(enableNegativeMark)) {
							String errMsg = ConstantValues.EXAM_ENABLE_NEG_MARK
									+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
											ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
							Debug.log(errMsg);
							return emptyMap;
						}
						examDetailsMap.put(ConstantValues.EXAM_ENABLE_NEG_MARK, enableNegativeMark);

						// Negative Mark Value
						String negativeMarkValue = (String) examMasterEntity.getString(ConstantValues.EXAM_NEG_MARK);
						if (UtilValidate.isEmpty(negativeMarkValue)) {
							String errMsg = ConstantValues.EXAM_NEG_MARK
									+ UtilProperties.getMessage(ConstantValues.ONLINE_EXAM_UI_LABELS,
											ConstantValues.EMPTY_VARIABLE_MESSAGE, UtilHttp.getLocale(request));
							Debug.log(errMsg);
							return emptyMap;
						}
						examDetailsMap.put(ConstantValues.EXAM_NEG_MARK, negativeMarkValue);

						// Add the exam details map to the list
						examList.add(examDetailsMap);
					}
				}
			}

			// Create a result map and return it
			Map<String, Object> result = new HashMap<>();
			result.put(ConstantValues.EXAM_LIST_, examList);

			return result;

		} catch (Exception e) {
			// Log any exceptions that may occur
			Debug.logError(e, MODULE_NAME);
			return emptyMap;
		}

	}
}
