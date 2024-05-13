package com.exam.util;

public interface ConstantValues {
//	Enumeration Entity
	String ENUM_ID = "enumId";
	String ENUM_SEQUENCE_ID = "sequenceId";
	String ENUM_TYPE_ID = "enumTypeId";
	String ENUM_DESCRIPTION = "description";
	String THRESHOLD_DATE = "thresholdDate";
	String FROM_DATE = "fromDate";

//	TopicMaster Entity
	String TOPIC_ID = "topicId";
	String TOPIC_NAME = "topicName";

//	ExamMaster Entity
	String EXAM_ID = "examId";
	String EXAM_NAME = "examName";
	String EXAM_DESCRIPTION = "description";
	String EXAM_CREATION_DATE = "creationDate";
	String EXAM_EXPIRATION_DATE = "expirationDate";
	String EXAM_TOTAL_QUES = "noOfQuestions";
	String EXAM_DURATION = "durationMinutes";
	String EXAM_PASS_PERCENTAGE = "passPercentage";
	String EXAM_QUES_RANDOMIZED = "questionsRandomized";
	String EXAM_ANS_MUST = "answersMust";
	String EXAM_ENABLE_NEG_MARK = "enableNegativeMark";
	String EXAM_NEG_MARK = "negativeMarkValue";

// QuestionMaster Entity
	String QUES_ID = "questionId";
	String QUES_DETAIL = "questionDetail";
	String QUES_OPTION_A = "optionA";
	String QUES_OPTION_B = "optionB";
	String QUES_OPTION_C = "optionC";
	String QUES_OPTION_D = "optionD";
	String QUES_OPTION_E = "optionE";
	String QUES_ANSWER = "answer";
	String QUES_NUM_ANS = "numAnswers";
	String QUES_TYPE = "questionType";
	String QUES_DIFFICULTY_LEVEL = "difficultyLevel";
	String QUES_ANS_VALUE = "answerValue";
	String QUES_TOPIC_ID = "topicId";
	String QUES_NEG_MARK = "negativeMarkValue";

// ExamTopicMapping Entity
	String EXAMTOPIC_EXAM_ID = "examId";
	String EXAMTOPIC_TOPIC_ID = "topicId";
	String EXAM_TOPIC_PERCENTAGE = "percentage";
	String EXAM_TOPIC_PASS_PERCENTAGE = "topicPassPercentage";
	String TOPIC_QUES_PER_EXAM = "questionsPerExam";

// UserAttemptMaster Entity
	String USER_ATTEMPT_PERFORMANCE_ID = "performanceId";
	String USER_ATTEMPT_NUMBER = "attemptNumber";
	String USER_ATTEMPT_PARTY_ID = "partyId";
	String USER_ATTEMPT_EXAM_ID = "examId";
	String USER_ATTEMPT_SCORE = "score";
	String USER_ATTEMPT_COMPLETED_DATE = "completedDate";
	String USER_ATTEMPT_TOTAL_QUES = "noOfQuestions";
	String USER_ATTEMPT_TOTAL_CORRECT = "totalCorrect";
	String USER_ATTEMPT_TOTAL_WRONG = "totalWrong";
	String USER_ATTEMPT_PASSED = "userPassed";

// UserAttemptTopicMaster Entity
	String USER_TOPIC_PERFORMANCE_ID = "performanceId";
	String USER_TOPIC_TOPIC_ID = "topicId";
	String USER_TOPIC_PASS_PERCENTAGE = "topicPassPercentage";
	String USER_TOPIC_TOTAL_QUES = "totalQuestionsInThisTopic";
	String USER_TOPIC_CRCT_QUES = "correctQuestionsInThisTopic";
	String USER_TOPIC_PERCENTAGE = "userTopicPercentage";
	String USER_TOPIC_PASSED = "userPassedThisTopic";

// UserAttemptAnswerMaster Entity
	String USER_ANSWER_PERFORMANCE_ID = "performanceId";
	String USER_ANSWER_QUESTION_ID = "questionId";
	String USER_ANSWER_SEQUENCE_ID = "sequenceNum";
	String USER_ANSWER_SUBMITTED = "submittedAnswer";
	String USER_ANSWER_FLAGGED = "isFlagged";

// UserExamMapping Entity
	String PARTY_ID = "partyId";
	String USEREXAM_EXAM_ID = "examId";
	String USEREXAM_ALLOWED_ATTEMPTS = "allowedAttempts";
	String USEREXAM_NO_OF_ATTEMPTS = "noOfAttempts";
	String USEREXAM_LAST_DATE = "lastPerformanceDate";
	String USEREXAM_TIMEOUT_DAYS = "timeoutDays";
	String USEREXAM_PASSWORD_CHANGE = "passwordChangesAuto";
	String USEREXAM_SPLIT = "canSplitExams";
	String USEREXAM_SEE_RESULT = "canSeeDetailedResults";
	String USEREXAM_MAX_SPLIT = "maxSplitAttempts";

	// OnlineExamPortalUiLabels
	String ONLINE_EXAM_UI_LABELS = "OnlineExamPortalUiLabels";

	// EnumerationEntityListEvent
	String Q_TYPE = "Q_TYPE";
	String QUESTION_TYPE_LIST = "QuestionTypeList";
	String QUESTION_TYPE_INFO = "QuestionTypeInfo";
	// error,success
	String ERROR = "error";
	String SUCCESS = "success";
	String ERROR_MESSAGE = "ERROR_MESSAGE";
	String SUCCESS_MESSAGE = "SUCCESS_MESSAGE";
	// ExamInformation
	String REQUEST = "request";
	String EXAMS = "exams";

	// EntityConstant
	String EXAM_MASTER = "ExamMaster";
	String TOPIC_MASTER = "TopicMaster";
	String EXAM_TOPIC_MAPPING = "ExamTopicMapping";
	String USER_ATTEMPT_MASTER = "UserAttemptMaster";
	String USER_ATTEMPT_TOPIC_MASTER = "UserAttemptTopicMaster";
	String USER_ATTEMPT_ANSWER_MASTER = "UserAttemptAnswerMaster";
	String USER_EXAM_MAPPING = "UserExamMapping";
	String QUESTION_MASTER = "QuestionMaster";
	String PERSON = "Person";
	String USER_LOGIN="userLogin";
	// Service

	String CREATE_EXAM_MASTER = "createExamMaster";
	String UPDATE_EXAM_MASTER = "updateExamMaster";
	String GET_EXAM_INFORMATION = "getExamInformation";
	String CREATE_EXAM_TOPIC_MAPPING = "createExamTopicMapping";
	String UPDATE_EXAM_TOPIC_MAPPING_MASTER = "updateExamTopicMappingMaster";
	String CREATE_USER_ATTEMPT_MASTER = "createUserAttemptMaster";
	String CREATE_USER_ATTEMPT_TOPIC_MASTER = "createUserAttemptTopicMaster";
	String UPDATE_USEREXAM_MAPPING_NO_OF_ATTEMPTS = "updateUserExamMappingnoOfAttempts";
	String GET_QUESTION_INFORMATION = "getQuestionInformation";
	String CREATE_USER_ATTEMPT_ANSWER_MASTER = "createUserAttemptAnswerMaster";
	String DELETE_QUESTION_MASTER = "deleteQuestionMaster";
	String UPDATE_QUESTION_MASTER = "updateQuestionMaster";
	String CREATE_QUESTION_MASTER = "createQuestionMaster";
	String CREATE_PERSON_AND_USER_LOGIN = "createPersonAndUserLogin";
	String ASSIGN_PARTY_ROLE = "assignPartyRole";
	String FETCH_EXAM_RESULT = "fetchExamResult";
	String CREATE_TOPIC_MASTER = "createTopicMaster";
	String UPDATE_TOPIC_MASTER = "updateTopicMaster";
	String DELETE_TOPIC_MASTER = "deleteTopicMaster";
	String CREATE_USER_EXAM_MAPPING = "createUserExamMapping";
	String UPDATE_USER_EXAM_MAPPING = "updateUserExamMapping";
	String UPDATE_USER_ATTEMPT_MASTER="updateUserAttemptMaster";
	String UPDATE_USER_ATTEMPT_TOPIC_MASTER="updateUserAttemptTopicMaster";
	String UPDATE_USER_ATTEMPT_ANSWER_MASTER="updateUserAttemptAnswerMaster";
	// ExamMasterEvents
	String EXAM_LIST = "ExamList";
	String EXAM_INFO = "ExamInfo";
	
	// UILABLESKEY
	String ENUMERATION_ENTITY_RECORD_NOT_FOUND_ERROR = "EnumerationEntityRecordNotFoundError";
	String MANDATORY_FIELD_ERRORMSG_LOGINFORM = "MandatoryFieldErrMsgLoginForm";
	String ONLINE_EXAM_PORTAL_COMPANYNAME = "OnlineExamPortalCompanyName";
	String ONLINE_EXAM_PORTAL_COMPANY_SUBTITLE = "OnlineExamPortalCompanySubtitle";
	String SERVICE_CALLING_ERROR = "ServiceCallingError";
	String SERVICE_SUCCESS_MESSAGE = "ServiceSuccessMessage";
	String FETCH_SUCCESS_MESSAGE = "FetchSuccessMessage";
	String ERROR_IN_FETCHING_DATA = "ErrorInFetchingData";
	String LOGIN_SUCCESS_MESSAGE = "LoginSuccessMessage";
	String DELETE_ERROR_MESSAGE = "DeleteErrorMessage";
	String EMPTY_VARIABLE_MESSAGE = "EmptyVariableMessage";
	String EXAM_FOR_TOPIC_SUCCESS_MESSAGE = "ExamForTopicSuccessMessage";
	String EXAM_FOR_TOPIC_WARNING_MESSAGE = "ExamForTopicWarningMessage";
	String PERCENTAGE_WARNING_MESSAGE = "PercentageWarningMessage";
	String PERCENTAGE_SUCCESS_MESSAGE = "PercentageSuccessMessage";
	String LOGIN_ERROR = "LoginError";
	String ATTEMPT_ERROR = "AttemptError";
	String QUESTION_INFORMATION_SUCCESS_MESSAGE = "QuestionInformationSuccessMessage";
	String EXAM_RESULT_ERROR_MESSAGE = "ExamResultErrorMessage";
	String EXAM_RESULT_EMPTY_MESSAGE="ExamResultEmptyMessage";
	// ExamTopicMappingEvents
	String SELECTED_TOPICS = "SelectedTopics";
	String SELECTED_TOPICS_INFO = "SelectedTopicsInfo";
	String MESSAGE = "message";
	String PERCENTAGE = "percentage";
	String QUESTIONS = "questions";
	String TOPIC_QUESTIONS_PEREXAM = "topicQuestionsPerExam";
	String EXAM_TOPIC_LIST = "ExamTopicList";
	String EXAM_TOPICS_INFO = "ExamTopicsInfo";

	// LoginEvent
	String PARTY_ROLE = "PartyRole";
	String ROLE = "Role";
	String ROLE_TYPE_ID = "roleTypeId";
	String RESULT = "result";
	String HIBERNAT_RESULT = "hibernatresult";

	// QuestionInformation
	String TOPIC_INFORMATION = "TopicInformation";
	String QUESTION = "question";
	String SELECTED_QUESTIONS = "selectedQuestions";
	String USER_ATTEMPT_ANSWER_MASTER_QUESINFO = "userAttemptAnswerMaster";
	String USER_ATTEMPT = "userAttempt";

	// QuestionMasterEvents
	String QUESTION_LIST = "QuestionList";
	String QUESTION_INFO = "QuestionInfo";

	// ReportEvent
	String COMBINED_MAP = "combinedMap";
	String EXAM_LIST_REPORT = "examList";
	String EXAM_WISE_PERFORMANCE = "examWisePerformance";

	// StudentListEvents
	String USER = "user";
	String STUDENT_LIST = "StudentList";
	String STUDENT_LIST_INFO = "StudentListInfo";

	// RegisterError
	String REGISTER_ERROR = "RegisterError";
	String ROLE_RESULT = "roleResult";

	// TopicMasterEvents
	String TOPIC_MASTER_ = "TopicMaster";
	String TOPIC_LIST = "TopicList";
	String TOPIC_INFO = "TopicInfo";

	// UserExamMappingEvents
	String USER_EXAM_LIST = "UserExamList";
	String USER_EXAM_INFO = "UserExamInfo";
	
	//UserResult
	String USER_QUESTIONS="questions";
	String USER_SELECTED_ANSWER="selectionAnswerResult";
	String CORRECT_ANSWER_COUNT="correctAnswerCount";
	String CORRECT_ANSWER_MARK="correctAnswerMark";
	String WRONG_ANSWER_COUNT="wrongAnswerCount";
	String WRONG_ANSWER_MARK="wrongAnswerMark";
	String TOPIC="topic";
	
	//ExamInfoService
	String EXAM_LIST_="examList";
	String STATUS="status";
	
	//QuestionInfoService
	String EXAM_QUESTION ="examquestion";
	String TOPIC_QUESTIONS="topicQuestions";
	
	//ReportService
	String USER_ATTEMPT_MASTER_MAP="userAttemptMasterMap";
	String USER_ATTEMPT_TOPIC_MASTER_LIST="userAttemptTopicMasterList";
	String USER_ATTEMPT_ANSWER_MASTER_LIST="userAttemptAnswerMasterList";
	String QUESTIONS_="questions";
	String TOPIC_NAME_LIST="TopicNameList";
	String EXAM_WISE_PERFORMANCE_="examWisePerformance";
	
}
