/*
 * Created on Feb 2, 2006
 */
package net.sourceforge.fenixedu.domain.curricularRules;

public enum CurricularRuleType {

    PRECEDENCY_APPROVED_DEGREE_MODULE,

    PRECEDENCY_ENROLED_DEGREE_MODULE,

    PRECEDENCY_BETWEEN_DEGREE_MODULES,

    CREDITS_LIMIT,

    DEGREE_MODULES_SELECTION_LIMIT,

    ENROLMENT_TO_BE_APPROVED_BY_COORDINATOR,

    EXCLUSIVENESS,

    ANY_CURRICULAR_COURSE,

    MINIMUM_NUMBER_OF_CREDITS_TO_ENROL,

    MAXIMUM_NUMBER_OF_CREDITS_FOR_ENROLMENT_PERIOD,

    PREVIOUS_YEARS_ENROLMENT,

    ASSERT_UNIQUE_APPROVAL_IN_CURRICULAR_COURSE_CONTEXTS,

    IMPROVEMENT_OF_APPROVED_ENROLMENT,

    ENROLMENT_IN_SPECIAL_SEASON_EVALUATION,

    MAXIMUM_NUMBER_OF_ECTS_IN_SPECIAL_SEASON_EVALUATION,

    CREDITS_LIMIT_IN_EXTERNAL_CYCLE,

    EVEN_ODD,

    MAXIMUM_NUMBER_OF_ECTS_IN_STANDALONE_CURRICULUM_GROUP,
    
    PHD_VALID_CURRICULAR_COURSES,
    
    SENIOR_STATUTE_SCOPE;

    public String getName() {
	return name();
    }

}
