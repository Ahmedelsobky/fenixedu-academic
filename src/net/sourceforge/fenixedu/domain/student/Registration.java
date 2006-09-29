package net.sourceforge.fenixedu.domain.student;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.fenixedu.accessControl.AccessControl;
import net.sourceforge.fenixedu.domain.Attends;
import net.sourceforge.fenixedu.domain.CurricularCourse;
import net.sourceforge.fenixedu.domain.DegreeCurricularPlan;
import net.sourceforge.fenixedu.domain.Enrolment;
import net.sourceforge.fenixedu.domain.Evaluation;
import net.sourceforge.fenixedu.domain.Exam;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.ExecutionDegree;
import net.sourceforge.fenixedu.domain.ExecutionPeriod;
import net.sourceforge.fenixedu.domain.ExecutionYear;
import net.sourceforge.fenixedu.domain.GratuitySituation;
import net.sourceforge.fenixedu.domain.GratuityValues;
import net.sourceforge.fenixedu.domain.GuideEntry;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.Project;
import net.sourceforge.fenixedu.domain.RootDomainObject;
import net.sourceforge.fenixedu.domain.SchoolClass;
import net.sourceforge.fenixedu.domain.Shift;
import net.sourceforge.fenixedu.domain.ShiftType;
import net.sourceforge.fenixedu.domain.SpecialSeasonCode;
import net.sourceforge.fenixedu.domain.StudentCurricularPlan;
import net.sourceforge.fenixedu.domain.StudentKind;
import net.sourceforge.fenixedu.domain.Teacher;
import net.sourceforge.fenixedu.domain.Tutor;
import net.sourceforge.fenixedu.domain.WrittenEvaluation;
import net.sourceforge.fenixedu.domain.WrittenEvaluationEnrolment;
import net.sourceforge.fenixedu.domain.WrittenTest;
import net.sourceforge.fenixedu.domain.YearStudentSpecialSeasonCode;
import net.sourceforge.fenixedu.domain.candidacy.DegreeCandidacy;
import net.sourceforge.fenixedu.domain.candidacy.Ingression;
import net.sourceforge.fenixedu.domain.candidacy.StudentCandidacy;
import net.sourceforge.fenixedu.domain.curriculum.EnrollmentCondition;
import net.sourceforge.fenixedu.domain.curriculum.EnrollmentState;
import net.sourceforge.fenixedu.domain.degree.DegreeType;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.domain.finalDegreeWork.Group;
import net.sourceforge.fenixedu.domain.finalDegreeWork.GroupStudent;
import net.sourceforge.fenixedu.domain.gratuity.ReimbursementGuideState;
import net.sourceforge.fenixedu.domain.onlineTests.DistributedTest;
import net.sourceforge.fenixedu.domain.onlineTests.StudentTestQuestion;
import net.sourceforge.fenixedu.domain.organizationalStructure.AccountabilityTypeEnum;
import net.sourceforge.fenixedu.domain.organizationalStructure.Party;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.domain.person.IDDocumentType;
import net.sourceforge.fenixedu.domain.reimbursementGuide.ReimbursementGuideEntry;
import net.sourceforge.fenixedu.domain.serviceRequests.documentRequests.DocumentRequest;
import net.sourceforge.fenixedu.domain.space.OldRoom;
import net.sourceforge.fenixedu.domain.studentCurricularPlan.Specialization;
import net.sourceforge.fenixedu.domain.studentCurricularPlan.StudentCurricularPlanState;
import net.sourceforge.fenixedu.domain.teacher.Advise;
import net.sourceforge.fenixedu.domain.teacher.AdviseType;
import net.sourceforge.fenixedu.domain.transactions.InsuranceTransaction;
import net.sourceforge.fenixedu.util.EntryPhase;
import net.sourceforge.fenixedu.util.PeriodState;
import net.sourceforge.fenixedu.util.StudentState;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.joda.time.YearMonthDay;

public class Registration extends Registration_Base {

    private transient Double approvationRatio;

    private transient Double arithmeticMean;

    private transient Integer approvedEnrollmentsNumber = 0;

    public final static Comparator<Registration> NUMBER_COMPARATOR = new Comparator<Registration>() {
	public int compare(Registration o1, Registration o2) {
	    return o1.getNumber().compareTo(o2.getNumber());
	}
    };

    public Registration() {
	super();
	setRootDomainObject(RootDomainObject.getInstance());
    }

    @Deprecated
    // NOTE: use this for legacy code only
    public Registration(Person person, Integer studentNumber, StudentKind studentKind,
	    StudentState state, Boolean payedTuition, Boolean enrolmentForbidden, EntryPhase entryPhase) {
	this(person, studentNumber, studentKind, state, payedTuition, enrolmentForbidden, false, null);
	setEntryPhase(entryPhase);
    }

    @Deprecated
    // NOTE: use this for legacy code only
    public Registration(Person person, Integer studentNumber, StudentKind studentKind,
	    StudentState state, Boolean payedTuition, Boolean enrolmentForbidden, EntryPhase entryPhase,
	    DegreeCurricularPlan degreeCurricularPlan) {
	this(person, studentNumber, studentKind, state, payedTuition, enrolmentForbidden, false, null);

	// create scp
	StudentCurricularPlan.createBolonhaStudentCurricularPlan(this, degreeCurricularPlan,
		StudentCurricularPlanState.ACTIVE, new YearMonthDay(), ExecutionPeriod
			.readActualExecutionPeriod());

	setEntryPhase(entryPhase);
    }

    public Registration(Person person, StudentKind studentKind,
	    DegreeCurricularPlan degreeCurricularPlan, StudentCandidacy studentCandidacy) {

	this(person, null, studentKind, StudentState.INSCRITO_OBJ, true, false, false, studentCandidacy);

	// create scp
	StudentCurricularPlan.createBolonhaStudentCurricularPlan(this, degreeCurricularPlan,
		StudentCurricularPlanState.ACTIVE, new YearMonthDay(), ExecutionPeriod
			.readActualExecutionPeriod());
    }

    public Registration(Person person, Integer studentNumber, StudentKind studentKind,
	    StudentState state, Boolean payedTuition, Boolean enrolmentForbidden,
	    StudentCandidacy studentCandidacy) {
	this(person, studentNumber, studentKind, state, payedTuition, enrolmentForbidden, false,
		studentCandidacy);
    }

    public Registration(Person person, Integer studentNumber, StudentKind studentKind,
	    StudentState state, Boolean payedTuition, Boolean enrolmentForbidden,
	    final Boolean interruptedStudies, StudentCandidacy studentCandidacy) {
	this();
	setPayedTuition(payedTuition);
	setEnrollmentForbidden(enrolmentForbidden);
	if (person.hasStudent()) {
	    setStudent(person.getStudent());
	} else {
	    setStudent(new Student(person, studentNumber));
	}
	setState(state);
	// setNumber(studentNumber);
	setStudentKind(studentKind);
	setInterruptedStudies(interruptedStudies);

	setFlunked(Boolean.FALSE);
	setRequestedChangeDegree(Boolean.FALSE);
	setRequestedChangeBranch(Boolean.FALSE);
	setStudentCandidacy(studentCandidacy);
	setRegistrationYear(ExecutionYear.readCurrentExecutionYear());
    }

    public void delete() {

	for (; !getStudentCurricularPlans().isEmpty(); getStudentCurricularPlans().get(0).delete())
	    ;

	removeStudent();
	removeRootDomainObject();
	super.deleteDomainObject();
    }

    public StudentCurricularPlan getActiveStudentCurricularPlan() {
	for (final StudentCurricularPlan studentCurricularPlan : getStudentCurricularPlans()) {
	    if (studentCurricularPlan.getCurrentState() == StudentCurricularPlanState.ACTIVE) {
		return studentCurricularPlan;
	    }
	}
	return null;
    }

    public StudentCurricularPlan getLastStudentCurricularPlan() {
	return (StudentCurricularPlan) Collections.max(getStudentCurricularPlans(), new BeanComparator(
		"startDateYearMonthDay"));
    }

    public StudentCurricularPlan getFirstStudentCurricularPlan() {
	return (StudentCurricularPlan) Collections.min(getStudentCurricularPlans(), new BeanComparator(
		"startDateYearMonthDay"));
    }

    public StudentCurricularPlan getActiveOrConcludedStudentCurricularPlan() {
	StudentCurricularPlan concludedStudentCurricularPlan = null;
	for (final StudentCurricularPlan studentCurricularPlan : getStudentCurricularPlans()) {
	    if (studentCurricularPlan.getCurrentState() == StudentCurricularPlanState.ACTIVE) {
		return studentCurricularPlan;
	    }
	    if (concludedStudentCurricularPlan == null
		    && studentCurricularPlan.getCurrentState() == StudentCurricularPlanState.SCHOOLPARTCONCLUDED) {
		concludedStudentCurricularPlan = studentCurricularPlan;
	    }
	}
	return concludedStudentCurricularPlan;
    }

    public StudentCurricularPlan getActiveOrConcludedOrLastStudentCurricularPlan() {
	StudentCurricularPlan studentCurricularPlan = getActiveOrConcludedStudentCurricularPlan();
	if (studentCurricularPlan == null) {
	    studentCurricularPlan = getLastStudentCurricularPlan();
	}
	return studentCurricularPlan;
    }

    public boolean attends(final ExecutionCourse executionCourse) {
	for (final Attends attends : getAssociatedAttends()) {
	    if (attends.getDisciplinaExecucao() == executionCourse) {
		return true;
	    }
	}
	return false;
    }

    public List<WrittenEvaluation> getWrittenEvaluations(final ExecutionPeriod executionPeriod) {
	final List<WrittenEvaluation> result = new ArrayList<WrittenEvaluation>();
	for (final Attends attend : this.getAssociatedAttends()) {
	    if (attend.getDisciplinaExecucao().getExecutionPeriod() == executionPeriod) {
		for (final Evaluation evaluation : attend.getDisciplinaExecucao()
			.getAssociatedEvaluations()) {
		    if (evaluation instanceof WrittenEvaluation && !result.contains(evaluation)) {
			result.add((WrittenEvaluation) evaluation);
		    }
		}
	    }
	}
	return result;
    }

    public List<Exam> getEnroledExams(final ExecutionPeriod executionPeriod) {
	final List<Exam> result = new ArrayList<Exam>();
	for (final WrittenEvaluationEnrolment writtenEvaluationEnrolment : this
		.getWrittenEvaluationEnrolments()) {
	    if (writtenEvaluationEnrolment.getWrittenEvaluation() instanceof Exam
		    && writtenEvaluationEnrolment.isForExecutionPeriod(executionPeriod)) {
		result.add((Exam) writtenEvaluationEnrolment.getWrittenEvaluation());
	    }
	}
	return result;
    }

    public List<Exam> getUnenroledExams(final ExecutionPeriod executionPeriod) {
	final List<Exam> result = new ArrayList<Exam>();
	for (final Attends attend : this.getAssociatedAttends()) {
	    if (attend.getDisciplinaExecucao().getExecutionPeriod() == executionPeriod) {
		for (final Evaluation evaluation : attend.getDisciplinaExecucao()
			.getAssociatedEvaluations()) {
		    if (evaluation instanceof Exam && !this.isEnroledIn(evaluation)) {
			result.add((Exam) evaluation);
		    }
		}
	    }
	}
	return result;
    }

    public List<WrittenTest> getEnroledWrittenTests(final ExecutionPeriod executionPeriod) {
	final List<WrittenTest> result = new ArrayList<WrittenTest>();
	for (final WrittenEvaluationEnrolment writtenEvaluationEnrolment : this
		.getWrittenEvaluationEnrolments()) {
	    if (writtenEvaluationEnrolment.getWrittenEvaluation() instanceof WrittenTest
		    && writtenEvaluationEnrolment.isForExecutionPeriod(executionPeriod)) {
		result.add((WrittenTest) writtenEvaluationEnrolment.getWrittenEvaluation());
	    }
	}
	return result;
    }

    public List<WrittenTest> getUnenroledWrittenTests(final ExecutionPeriod executionPeriod) {
	final List<WrittenTest> result = new ArrayList<WrittenTest>();
	for (final Attends attend : this.getAssociatedAttends()) {
	    if (attend.getDisciplinaExecucao().getExecutionPeriod() == executionPeriod) {
		for (final Evaluation evaluation : attend.getDisciplinaExecucao()
			.getAssociatedEvaluations()) {
		    if (evaluation instanceof WrittenTest && !this.isEnroledIn(evaluation)) {
			result.add((WrittenTest) evaluation);
		    }
		}
	    }
	}
	return result;
    }

    public List<Project> getProjects(final ExecutionPeriod executionPeriod) {
	final List<Project> result = new ArrayList<Project>();
	for (final Attends attend : this.getAssociatedAttends()) {
	    if (attend.getDisciplinaExecucao().getExecutionPeriod() == executionPeriod) {
		for (final Evaluation evaluation : attend.getDisciplinaExecucao()
			.getAssociatedEvaluations()) {
		    if (evaluation instanceof Project) {
			result.add((Project) evaluation);
		    }
		}
	    }
	}
	return result;
    }

    public boolean isEnroledIn(final Evaluation evaluation) {
	for (final WrittenEvaluationEnrolment writtenEvaluationEnrolment : this
		.getWrittenEvaluationEnrolments()) {
	    if (writtenEvaluationEnrolment.getWrittenEvaluation() == evaluation) {
		return true;
	    }
	}
	return false;
    }

    public OldRoom getRoomFor(final WrittenEvaluation writtenEvaluation) {
	for (final WrittenEvaluationEnrolment writtenEvaluationEnrolment : this
		.getWrittenEvaluationEnrolments()) {
	    if (writtenEvaluationEnrolment.getWrittenEvaluation() == writtenEvaluation) {
		return writtenEvaluationEnrolment.getRoom();
	    }
	}
	return null;
    }

    public Double getApprovationRatio() {
	if (this.approvationRatio == null) {
	    calculateApprovationRatioAndArithmeticMeanIfActive(true);
	}
	return this.approvationRatio;
    }

    public Double getArithmeticMean() {
	if (this.arithmeticMean == null) {
	    calculateApprovationRatioAndArithmeticMeanIfActive(true);
	}
	return this.arithmeticMean;
    }

    public void calculateApprovationRatioAndArithmeticMeanIfActive(boolean onlyPreviousExecutionYear) {

	int enrollmentsNumber = 0;
	int approvedEnrollmentsNumber = 0;
	int actualApprovedEnrollmentsNumber = 0;
	int totalGrade = 0;

	ExecutionYear currentExecutionYear = ExecutionYear.readCurrentExecutionYear();
	ExecutionYear previousExecutionYear = currentExecutionYear.getPreviousExecutionYear();

	for (StudentCurricularPlan studentCurricularPlan : getStudentCurricularPlans()) {
	    for (Enrolment enrolment : studentCurricularPlan.getEnrolments()) {
		if (enrolment.getEnrolmentCondition() == EnrollmentCondition.INVISIBLE) {
		    continue;
		}

		ExecutionYear enrolmentExecutionYear = enrolment.getExecutionPeriod().getExecutionYear();
		if (onlyPreviousExecutionYear && (previousExecutionYear != enrolmentExecutionYear)) {
		    continue;
		}

		if (enrolmentExecutionYear != currentExecutionYear) {

		    enrollmentsNumber++;
		    if (enrolment.getEnrollmentState() == EnrollmentState.APROVED) {
			actualApprovedEnrollmentsNumber++;

			Integer finalGrade = enrolment.getFinalGrade();
			if (finalGrade != null) {
			    approvedEnrollmentsNumber++;
			    totalGrade += finalGrade;
			} else {
			    enrollmentsNumber--;
			}
		    }
		}
	    }
	}

	setApprovedEnrollmentsNumber(Integer.valueOf(actualApprovedEnrollmentsNumber));

	setApprovationRatio((enrollmentsNumber == 0) ? 0 : (double) approvedEnrollmentsNumber
		/ enrollmentsNumber);
	setArithmeticMean((approvedEnrollmentsNumber == 0) ? 0 : (double) totalGrade
		/ approvedEnrollmentsNumber);

    }

    private void setApprovationRatio(Double approvationRatio) {
	this.approvationRatio = approvationRatio;
    }

    private void setArithmeticMean(Double arithmeticMean) {
	this.arithmeticMean = arithmeticMean;
    }

    public Integer getApprovedEnrollmentsNumber() {
	if (this.approvedEnrollmentsNumber == null) {
	    calculateApprovationRatioAndArithmeticMeanIfActive(true);
	}
	return approvedEnrollmentsNumber;
    }

    private void setApprovedEnrollmentsNumber(Integer approvedEnrollmentsNumber) {
	this.approvedEnrollmentsNumber = approvedEnrollmentsNumber;
    }

    public List<Advise> getAdvisesByTeacher(final Teacher teacher) {
	return (List<Advise>) CollectionUtils.select(getAdvises(), new Predicate() {

	    public boolean evaluate(Object arg0) {
		Advise advise = (Advise) arg0;
		return advise.getTeacher() == teacher;
	    }
	});
    }

    public List<Advise> getAdvisesByType(final AdviseType adviseType) {
	return (List<Advise>) CollectionUtils.select(getAdvises(), new Predicate() {
	    public boolean evaluate(Object arg0) {
		Advise advise = (Advise) arg0;
		return advise.getAdviseType().equals(adviseType);
	    }
	});
    }

    public Set<Attends> getOrderedAttends() {
	final Set<Attends> result = new TreeSet<Attends>(Attends.ATTENDS_COMPARATOR);
	result.addAll(getAssociatedAttends());
	return result;
    }

    public int countCompletedCoursesForActiveUndergraduateCurricularPlan() {
	return getActiveStudentCurricularPlan().getAprovedEnrolments().size();
    }

    public List<StudentCurricularPlan> getStudentCurricularPlansByStateAndType(
	    StudentCurricularPlanState state, DegreeType type) {
	List<StudentCurricularPlan> result = new ArrayList<StudentCurricularPlan>();
	for (StudentCurricularPlan studentCurricularPlan : this.getStudentCurricularPlans()) {
	    if (studentCurricularPlan.getCurrentState().equals(state)
		    && studentCurricularPlan.getDegreeCurricularPlan().getDegree().getTipoCurso()
			    .equals(type)) {
		result.add(studentCurricularPlan);
	    }
	}
	return result;
    }

    public List<StudentCurricularPlan> getStudentCurricularPlansBySpecialization(
	    Specialization specialization) {
	List<StudentCurricularPlan> result = new ArrayList<StudentCurricularPlan>();
	for (StudentCurricularPlan studentCurricularPlan : this.getStudentCurricularPlans()) {
	    if (studentCurricularPlan.getSpecialization() != null
		    && studentCurricularPlan.getSpecialization().equals(specialization)) {
		result.add(studentCurricularPlan);
	    }
	}
	return result;
    }

    public List<StudentCurricularPlan> getStudentCurricularPlansBySpecializationAndState(
	    Specialization specialization, StudentCurricularPlanState state) {
	List<StudentCurricularPlan> result = new ArrayList<StudentCurricularPlan>();
	for (StudentCurricularPlan studentCurricularPlan : this.getStudentCurricularPlans()) {
	    if (studentCurricularPlan.getSpecialization() != null
		    && studentCurricularPlan.getSpecialization().equals(specialization)
		    && studentCurricularPlan.getCurrentState() != null
		    && studentCurricularPlan.getCurrentState().equals(state)) {
		result.add(studentCurricularPlan);
	    }
	}
	return result;
    }

    public Set<DistributedTest> getDistributedTestsByExecutionCourse(ExecutionCourse executionCourse) {
	Set<DistributedTest> result = new HashSet<DistributedTest>();
	for (StudentTestQuestion studentTestQuestion : this.getStudentTestsQuestions()) {
	    if (studentTestQuestion.getDistributedTest().getTestScope().getClassName().equals(
		    ExecutionCourse.class.getName())
		    && studentTestQuestion.getDistributedTest().getTestScope().getKeyClass().equals(
			    executionCourse.getIdInternal())) {
		result.add(studentTestQuestion.getDistributedTest());
	    }
	}
	return result;
    }

    public List<Attends> readAttendsInCurrentExecutionPeriod() {
	final List<Attends> attends = new ArrayList<Attends>();
	for (final Attends attend : this.getAssociatedAttendsSet()) {
	    if (attend.getDisciplinaExecucao().getExecutionPeriod().getState().equals(
		    PeriodState.CURRENT)) {
		attends.add(attend);
	    }
	}
	return attends;
    }

    public List<Attends> readAttendsByExecutionPeriod(ExecutionPeriod executionPeriod) {
	List<Attends> attends = new ArrayList<Attends>();
	for (Attends attend : this.getAssociatedAttends()) {
	    if (attend.getDisciplinaExecucao().getExecutionPeriod().equals(executionPeriod)) {
		attends.add(attend);
	    }
	}
	return attends;
    }

    public Attends readAttendByExecutionCourse(ExecutionCourse executionCourse) {
	for (Attends attend : this.getAssociatedAttends()) {
	    if (attend.getDisciplinaExecucao().equals(executionCourse)) {
		return attend;
	    }
	}
	return null;
    }

    public static Registration readByUsername(String username) {
	final Person person = Person.readPersonByUsername(username);
	if (person != null) {
	    for (final Registration registration : person.getStudentsSet()) {
		return registration;
	    }
	}
	return null;
    }

    public static Registration readStudentByNumberAndDegreeType(Integer number, DegreeType degreeType) {
	for (Registration registration : RootDomainObject.getInstance().getRegistrations()) {
	    if (registration.getNumber().equals(number)
		    && registration.getDegreeType().equals(degreeType)) {
		return registration;
	    }
	}
	return null;
    }

    public static Registration readByNumber(Integer number) {
	for (Registration registration : RootDomainObject.getInstance().getRegistrationsSet()) {
	    if(registration.getNumber().equals(number)) {
		return registration;
	    }
	}
	return null;
    }

    public static List<Registration> readMasterDegreeStudentsByNameDocIDNumberIDTypeAndStudentNumber(
	    String studentName, String docIdNumber, IDDocumentType idType, Integer studentNumber) {

	final List<Registration> students = new ArrayList();
	final String studentNameToMatch = (studentName == null) ? null : studentName.replaceAll("%",
		".*").toLowerCase();

	for (Registration registration : RootDomainObject.getInstance().getRegistrations()) {
	    Person person = registration.getPerson();
	    if ((registration.getDegreeType().equals(DegreeType.MASTER_DEGREE) || registration
		    .getDegreeType().equals(DegreeType.BOLONHA_ADVANCED_FORMATION_DIPLOMA))
		    && ((studentNameToMatch != null && person.getName().toLowerCase().matches(
			    studentNameToMatch)) || studentNameToMatch == null)
		    && ((docIdNumber != null && person.getDocumentIdNumber().equals(docIdNumber)) || docIdNumber == null)
		    && ((idType != null && person.getIdDocumentType().equals(idType)) || idType == null)
		    && ((studentNumber != null && registration.getNumber().equals(studentNumber)) || studentNumber == null)) {

		students.add(registration);
	    }
	}
	return students;
    }

    public static List<Registration> readAllStudentsBetweenNumbers(Integer fromNumber, Integer toNumber) {
	int fromNumberInt = fromNumber.intValue();
	int toNumberInt = toNumber.intValue();

	final List<Registration> students = new ArrayList<Registration>();
	for (final Registration registration : RootDomainObject.getInstance().getRegistrationsSet()) {
	    int studentNumberInt = registration.getNumber().intValue();
	    if (studentNumberInt >= fromNumberInt && studentNumberInt <= toNumberInt) {
		students.add(registration);
	    }
	}
	return students;
    }

    public static List<Registration> readStudentsByDegreeType(DegreeType degreeType) {
	final List<Registration> students = new ArrayList();
	for (final Registration registration : RootDomainObject.getInstance().getRegistrationsSet()) {
	    if (registration.getDegreeType().equals(degreeType)) {
		students.add(registration);
	    }
	}
	return students;
    }

    public static Integer generateStudentNumber(DegreeType degreeType) {
	Integer number = Integer.valueOf(0);
	List<Registration> students = readStudentsByDegreeType(degreeType);
	Collections.sort(students, new ReverseComparator(NUMBER_COMPARATOR));

	if (!students.isEmpty()) {
	    number = students.get(0).getNumber();
	}

	// FIXME: ISTO E UMA SOLUCAO TEMPORARIA DEVIDO A EXISTIREM ALUNOS
	// NA SECRETARIA QUE
	// POR UM MOTIVO OU OUTRO NAO SE ENCONTRAM NA BASE DE DADOS
	if (degreeType.equals(DegreeType.MASTER_DEGREE) && (number.intValue() < 5411)) {
	    number = Integer.valueOf(5411);
	}

	return Integer.valueOf(number.intValue() + 1);
    }

    public GratuitySituation readGratuitySituationByExecutionDegree(ExecutionDegree executionDegree) {
	GratuityValues gratuityValues = executionDegree.getGratuityValues();
	for (StudentCurricularPlan studentCurricularPlan : this.getStudentCurricularPlans()) {
	    GratuitySituation gratuitySituation = studentCurricularPlan
		    .getGratuitySituationByGratuityValues(gratuityValues);
	    if (gratuitySituation != null) {
		return gratuitySituation;
	    }
	}
	return null;
    }

    public Group findFinalDegreeWorkGroupForCurrentExecutionYear() {
	for (final GroupStudent groupStudent : getAssociatedGroupStudents()) {
	    final Group group = groupStudent.getFinalDegreeDegreeWorkGroup();
	    final ExecutionDegree executionDegree = group.getExecutionDegree();
	    final ExecutionYear executionYear = executionDegree.getExecutionYear();
	    if (executionYear.getState().equals(PeriodState.CURRENT)) {
		return group;
	    }
	}
	return null;
    }

    public List readAllInsuranceTransactionByExecutionYear(ExecutionYear executionYear) {
	List<InsuranceTransaction> insuranceTransactions = new ArrayList<InsuranceTransaction>();
	for (InsuranceTransaction insuranceTransaction : this.getInsuranceTransactions()) {
	    if (insuranceTransaction.getExecutionYear().equals(executionYear)) {
		insuranceTransactions.add(insuranceTransaction);
	    }
	}
	return insuranceTransactions;
    }

    public List<InsuranceTransaction> readAllNonReimbursedInsuranceTransactionsByExecutionYear(
	    ExecutionYear executionYear) {
	List<InsuranceTransaction> nonReimbursedInsuranceTransactions = new ArrayList<InsuranceTransaction>();
	for (InsuranceTransaction insuranceTransaction : this.getInsuranceTransactions()) {
	    if (insuranceTransaction.getExecutionYear().equals(executionYear)) {
		GuideEntry guideEntry = insuranceTransaction.getGuideEntry();
		if (guideEntry == null || guideEntry.getReimbursementGuideEntries().isEmpty()) {
		    nonReimbursedInsuranceTransactions.add(insuranceTransaction);
		} else {
		    boolean isReimbursed = false;
		    for (ReimbursementGuideEntry reimbursementGuideEntry : guideEntry
			    .getReimbursementGuideEntries()) {
			if (reimbursementGuideEntry.getReimbursementGuide()
				.getActiveReimbursementGuideSituation().getReimbursementGuideState()
				.equals(ReimbursementGuideState.PAYED)) {
			    isReimbursed = true;
			    break;
			}
		    }
		    if (!isReimbursed) {
			nonReimbursedInsuranceTransactions.add(insuranceTransaction);
		    }
		}
	    }
	}
	return nonReimbursedInsuranceTransactions;
    }

    public Enrolment findEnrolmentByEnrolmentID(final Integer enrolmentID) {
	for (final StudentCurricularPlan studentCurricularPlan : getStudentCurricularPlansSet()) {
	    final Enrolment enrolment = studentCurricularPlan.findEnrolmentByEnrolmentID(enrolmentID);
	    if (enrolment != null) {
		return enrolment;
	    }
	}
	return null;
    }

    public int countDistributedTestsByExecutionCourse(final ExecutionCourse executionCourse) {
	return getDistributedTestsByExecutionCourse(executionCourse).size();
    }

    public boolean hasSchoolRegistration(ExecutionYear executionYear) {
	for (final StudentCurricularPlan studentCurricularPlan : getStudentCurricularPlansSet()) {
	    if (studentCurricularPlan.hasSchoolRegistration(executionYear)) {
		return true;
	    }
	}
	return false;
    }

    public Collection<DocumentRequest> getDocumentRequests() {
	final Set<DocumentRequest> result = new HashSet<DocumentRequest>();

	for (final StudentCurricularPlan studentCurricularPlan : getStudentCurricularPlansSet()) {
	    result.addAll(studentCurricularPlan.getDocumentRequests());
	}

	return result;
    }

    // Special Season

    public SpecialSeasonCode getSpecialSeasonCodeByExecutionYear(ExecutionYear executionYear) {
	for (YearStudentSpecialSeasonCode yearStudentSpecialSeasonCode : getYearStudentSpecialSeasonCodesSet()) {
	    if (yearStudentSpecialSeasonCode.getExecutionYear() == executionYear) {
		return yearStudentSpecialSeasonCode.getSpecialSeasonCode();
	    }
	}
	return null;
    }

    public void setSpecialSeasonCode(ExecutionYear executionYear, SpecialSeasonCode specialSeasonCode) {
	if (specialSeasonCode == null) {
	    if (!getActiveStudentCurricularPlan().getSpecialSeasonEnrolments(executionYear).isEmpty()) {
		throw new DomainException("error.cannot.change.specialSeasonCode");
	    } else {
		deleteYearSpecialSeasonCode(executionYear);
	    }
	} else {
	    if (specialSeasonCode.getMaxEnrolments() < getActiveStudentCurricularPlan()
		    .getSpecialSeasonEnrolments(executionYear).size()) {
		throw new DomainException("error.cannot.change.specialSeasonCode");
	    } else {
		changeYearSpecialSeasonCode(executionYear, specialSeasonCode);
	    }
	}
    }

    private void changeYearSpecialSeasonCode(ExecutionYear executionYear,
	    SpecialSeasonCode specialSeasonCode) {
	YearStudentSpecialSeasonCode yearStudentSpecialSeasonCode = getYearStudentSpecialSeasonCodeByExecutionYear(executionYear);
	if (yearStudentSpecialSeasonCode != null) {
	    yearStudentSpecialSeasonCode.setSpecialSeasonCode(specialSeasonCode);
	} else {
	    new YearStudentSpecialSeasonCode(this, executionYear, specialSeasonCode);
	}
    }

    private YearStudentSpecialSeasonCode getYearStudentSpecialSeasonCodeByExecutionYear(
	    ExecutionYear executionYear) {
	for (YearStudentSpecialSeasonCode yearStudentSpecialSeasonCode : getYearStudentSpecialSeasonCodesSet()) {
	    if (yearStudentSpecialSeasonCode.getExecutionYear() == executionYear) {
		return yearStudentSpecialSeasonCode;
	    }
	}
	return null;
    }

    private void deleteYearSpecialSeasonCode(ExecutionYear executionYear) {
	for (YearStudentSpecialSeasonCode yearStudentSpecialSeasonCode : getYearStudentSpecialSeasonCodesSet()) {
	    if (yearStudentSpecialSeasonCode.getExecutionYear() == executionYear) {
		yearStudentSpecialSeasonCode.delete();
	    }
	}
    }

    // end Special Season

    // Improvement

    public List<Enrolment> getEnrolmentsToImprov(ExecutionPeriod executionPeriod) {

	if (executionPeriod == null) {
	    throw new DomainException("error.executionPeriod.notExist");
	}

	List<Enrolment> enrolmentsToImprov = new ArrayList<Enrolment>();
	for (StudentCurricularPlan scp : getStudentCurricularPlans()) {
	    if (scp.getDegreeCurricularPlan().getDegree().getTipoCurso().equals(DegreeType.DEGREE)) {
		enrolmentsToImprov.addAll(scp.getEnrolmentsToImprov(executionPeriod));
	    }
	}
	return enrolmentsToImprov;
    }

    public List<Enrolment> getEnroledImprovements() {
	List<Enrolment> enroledImprovements = new ArrayList<Enrolment>();
	for (StudentCurricularPlan scp : getStudentCurricularPlans()) {
	    if (scp.getDegreeCurricularPlan().getDegree().getTipoCurso().equals(DegreeType.DEGREE)) {
		enroledImprovements.addAll(scp.getEnroledImprovements());
	    }
	}
	return enroledImprovements;
    }

    public List<ExecutionCourse> getAttendingExecutionCoursesForCurrentExecutionPeriod() {
	final List<ExecutionCourse> result = new ArrayList<ExecutionCourse>();
	for (final Attends attends : getAssociatedAttendsSet()) {
	    if (attends.getDisciplinaExecucao().getExecutionPeriod().getState().equals(
		    PeriodState.CURRENT)) {
		result.add(attends.getDisciplinaExecucao());
	    }
	}
	return result;
    }

    public List<ExecutionCourse> getAttendingExecutionCoursesFor(final ExecutionPeriod executionPeriod) {
	final List<ExecutionCourse> result = new ArrayList<ExecutionCourse>();
	for (final Attends attends : getAssociatedAttendsSet()) {
	    if (attends.getDisciplinaExecucao().getExecutionPeriod() == executionPeriod) {
		result.add(attends.getDisciplinaExecucao());
	    }
	}
	return result;
    }

    public List<Shift> getShiftsForCurrentExecutionPeriod() {
	final List<Shift> result = new ArrayList<Shift>();
	for (final Shift shift : getShiftsSet()) {
	    if (shift.getDisciplinaExecucao().getExecutionPeriod().getState()
		    .equals(PeriodState.CURRENT)) {
		result.add(shift);
	    }
	}
	return result;
    }

    public List<Shift> getShiftsFor(final ExecutionPeriod executionPeriod) {
	final List<Shift> result = new ArrayList<Shift>();
	for (final Shift shift : getShiftsSet()) {
	    if (shift.getDisciplinaExecucao().getExecutionPeriod() == executionPeriod) {
		result.add(shift);
	    }
	}
	return result;
    }

    public List<Shift> getShiftsFor(final ExecutionCourse executionCourse) {
	final List<Shift> result = new ArrayList<Shift>();
	for (final Shift shift : getShiftsSet()) {
	    if (shift.getDisciplinaExecucao() == executionCourse) {
		result.add(shift);
	    }
	}
	return result;
    }

    private int countNumberOfDistinctExecutionCoursesOfShiftsFor(final ExecutionPeriod executionPeriod) {
	final Set<ExecutionCourse> result = new HashSet<ExecutionCourse>();
	for (final Shift shift : getShiftsSet()) {
	    if (shift.getDisciplinaExecucao().getExecutionPeriod() == executionPeriod) {
		result.add(shift.getDisciplinaExecucao());
	    }
	}
	return result.size();
    }

    public Integer getNumberOfExecutionCoursesWithEnroledShiftsFor(final ExecutionPeriod executionPeriod) {
	return getAttendingExecutionCoursesFor(executionPeriod).size()
		- countNumberOfDistinctExecutionCoursesOfShiftsFor(executionPeriod);
    }

    public Integer getNumberOfExecutionCoursesHavingNotEnroledShiftsFor(
	    final ExecutionPeriod executionPeriod) {
	int result = 0;
	final List<Shift> enroledShifts = getShiftsFor(executionPeriod);
	for (final ExecutionCourse executionCourse : getAttendingExecutionCoursesFor(executionPeriod)) {
	    for (final ShiftType shiftType : executionCourse.getOldShiftTypesToEnrol()) {
		if (!enroledShiftsContainsShiftWithSameExecutionCourseAndShiftType(enroledShifts,
			executionCourse, shiftType)) {
		    result++;
		    break;
		}
	    }
	}
	return Integer.valueOf(result);
    }

    private boolean enroledShiftsContainsShiftWithSameExecutionCourseAndShiftType(
	    final List<Shift> enroledShifts, final ExecutionCourse executionCourse,
	    final ShiftType shiftType) {

	return CollectionUtils.exists(enroledShifts, new Predicate() {
	    public boolean evaluate(Object object) {
		Shift enroledShift = (Shift) object;
		return enroledShift.getDisciplinaExecucao() == executionCourse
			&& enroledShift.getTipo() == shiftType;
	    }
	});
    }

    public Set<SchoolClass> getSchoolClassesToEnrol() {
	final Set<SchoolClass> result = new HashSet<SchoolClass>();
	for (final Attends attends : getAssociatedAttendsSet()) {
	    final ExecutionCourse executionCourse = attends.getDisciplinaExecucao();

	    if (executionCourse.getExecutionPeriod().getState().equals(PeriodState.CURRENT)) {
		result.addAll(getSchoolClassesToEnrolBy(executionCourse));
	    }
	}
	return result;
    }

    public Set<SchoolClass> getSchoolClassesToEnrolBy(final ExecutionCourse executionCourse) {
	Set<SchoolClass> schoolClasses = executionCourse
		.getSchoolClassesBy(getActiveStudentCurricularPlan().getDegreeCurricularPlan());
	return schoolClasses.isEmpty() ? executionCourse.getSchoolClasses() : schoolClasses;
    }

    public void addAttendsTo(final ExecutionCourse executionCourse) {

	checkIfReachedAttendsLimit();

	if (readAttendByExecutionCourse(executionCourse) == null) {
	    final Attends attends = new Attends(this, executionCourse);
	    findAndSetEnrollmentForAttend(getActiveStudentCurricularPlan(), executionCourse, attends);
	}
    }

    private void findAndSetEnrollmentForAttend(final StudentCurricularPlan studentCurricularPlan,
	    final ExecutionCourse executionCourse, final Attends attends) {

	for (final CurricularCourse curricularCourse : executionCourse
		.getAssociatedCurricularCoursesSet()) {
	    final Enrolment enrolment = studentCurricularPlan
		    .getEnrolmentByCurricularCourseAndExecutionPeriod(curricularCourse, executionCourse
			    .getExecutionPeriod());
	    if (enrolment != null) {
		attends.setEnrolment(enrolment);
		break;
	    }
	}
    }

    private static final int MAXIMUM_STUDENT_ATTENDS_PER_EXECUTION_PERIOD = 8;

    private void checkIfReachedAttendsLimit() {
	if (readAttendsInCurrentExecutionPeriod().size() >= MAXIMUM_STUDENT_ATTENDS_PER_EXECUTION_PERIOD) {
	    throw new DomainException("error.student.reached.attends.limit", String
		    .valueOf(MAXIMUM_STUDENT_ATTENDS_PER_EXECUTION_PERIOD));
	}
    }

    public void removeAttendFor(final ExecutionCourse executionCourse) {
	final Attends attend = readAttendByExecutionCourse(executionCourse);
	if (attend != null) {
	    checkIfHasEnrolmentFor(attend);
	    checkIfHasShiftsFor(executionCourse);
	    attend.delete();
	}
    }

    private void checkIfHasEnrolmentFor(final Attends attend) {
	if (attend.hasEnrolment()) {
	    throw new DomainException("errors.student.already.enroled");
	}
    }

    private void checkIfHasShiftsFor(final ExecutionCourse executionCourse) {
	if (!getShiftsFor(executionCourse).isEmpty()) {
	    throw new DomainException("errors.student.already.enroled.in.shift");
	}
    }

    public boolean isActive() {
	// FIXME: this sould not be hardcoded!!
	return true;
    }

    public Tutor getAssociatedTutor() {

	StudentCurricularPlan activeStudentCurricularPlan = this
		.getActiveOrConcludedStudentCurricularPlan();
	if (activeStudentCurricularPlan == null) {
	    activeStudentCurricularPlan = this.getLastStudentCurricularPlan();
	}

	return activeStudentCurricularPlan.getAssociatedTutor();
    }

    @Override
    public Integer getNumber() {
	return (super.getNumber() != null) ? super.getNumber() : getStudent().getNumber();
    }

    public Person getPerson() {
	return getStudent().getPerson();
    }

    // FIXME: remove this methods after migration to Candidacy
    @Override
    public EntryPhase getEntryPhase() {
	if (isBolonhaDegreeOrIntegratedMaster()) {
	    return getDegreeCandidacy().getEntryPhase();
	}
	return super.getEntryPhase();
    }

    @Override
    public void setEntryPhase(EntryPhase entryPhase) {
	if (isBolonhaDegreeOrIntegratedMaster()) {
	    getDegreeCandidacy().setEntryPhase(entryPhase);
	}
	super.setEntryPhase(entryPhase);
    }

    @Override
    public Double getEntryGrade() {
	if (isBolonhaDegreeOrIntegratedMaster()) {
	    return getDegreeCandidacy().getEntryGrade();
	}
	return super.getEntryGrade();
    }

    @Override
    public void setEntryGrade(Double entryGrade) {
	if (isBolonhaDegreeOrIntegratedMaster()) {
	    getDegreeCandidacy().setEntryGrade(entryGrade);
	}
	super.setEntryGrade(entryGrade);
    }

    @Override
    public String getIngression() {
	if (isBolonhaDegreeOrIntegratedMaster()) {
	    return getDegreeCandidacy().getIngression();
	}
	return super.getIngression();
    }
    
    public Ingression getIngressionEnum() {
	return Ingression.valueOf(getIngression());
    }

    @Override
    public void setIngression(String ingression) {
	if (isBolonhaDegreeOrIntegratedMaster()) {
	    getDegreeCandidacy().setIngression(ingression);
	}
	super.setIngression(ingression);
    }

    @Override
    public String getContigent() {
	if (isBolonhaDegreeOrIntegratedMaster()) {
	    return getDegreeCandidacy().getContigent();
	}
	return super.getContigent();
    }

    @Override
    public void setContigent(String contigent) {
	if (isBolonhaDegreeOrIntegratedMaster()) {
	    getDegreeCandidacy().setContigent(contigent);
	}
	super.setContigent(contigent);
    }

    // FIXME: this should be deducted from Degree/DegreeCurricularPlan
    @Override
    public String getIstUniversity() {
	if (isBolonhaDegreeOrIntegratedMaster()) {
	    return getDegreeCandidacy().getIstUniversity();
	}

	return super.getIstUniversity();
    }

    @Override
    public void setIstUniversity(String istUniversity) {
	if (isBolonhaDegreeOrIntegratedMaster()) {
	    getDegreeCandidacy().setIstUniversity(istUniversity);
	}
	super.setIstUniversity(istUniversity);
    }

    private DegreeCandidacy getDegreeCandidacy() {
	return ((DegreeCandidacy) getStudentCandidacy());
    }

    private boolean isBolonhaDegreeOrIntegratedMaster() {
	return (getDegreeType() == DegreeType.BOLONHA_DEGREE || getDegreeType() == DegreeType.BOLONHA_INTEGRATED_MASTER_DEGREE)
		&& (hasStudentCandidacy());

    }

    // FIXME: end of methods to remove after migration of this information
    // to Candidacy

    public DegreeType getDegreeType() {
	final StudentCurricularPlan scp = (getActiveOrConcludedStudentCurricularPlan() != null) ? getActiveOrConcludedStudentCurricularPlan()
		: getLastStudentCurricularPlan();
	return scp.getDegreeType();
    }

    public boolean isActiveForOffice(Unit office) {

	Set<Party> officeDegreeUnits = office.getChildParties(AccountabilityTypeEnum.ACADEMIC_STRUCTURE,
		Unit.class);

	StudentCurricularPlan activeStudentCurricularPlan = getActiveStudentCurricularPlan();
	if (activeStudentCurricularPlan != null) {
	    if (officeDegreeUnits.contains(activeStudentCurricularPlan.getDegreeCurricularPlan()
		    .getDegree().getUnit())) {
		return true;
	    }
	}
	return false;
    }

    public boolean isForOffice(Unit office) {

	Set<Party> officeDegreeUnits = office.getChildParties(AccountabilityTypeEnum.ACADEMIC_STRUCTURE,
		Unit.class);

	StudentCurricularPlan studentCurricularPlan = getActiveOrConcludedOrLastStudentCurricularPlan();
	if (officeDegreeUnits.contains(studentCurricularPlan.getDegreeCurricularPlan().getDegree()
		.getUnit())) {
	    return true;
	}

	return false;
    }

    public boolean getIsForOffice() {
	Unit workingPlace = AccessControl.getUserView().getPerson().getEmployee()
		.getCurrentWorkingPlace();
	return isForOffice(workingPlace);
    }

}
