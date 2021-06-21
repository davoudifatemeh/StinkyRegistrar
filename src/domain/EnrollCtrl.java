package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
    public void enroll(Student student, List<CSE> courses) throws EnrollmentRulesViolationException {
        checkEnrollmentConditions(student, courses);
        student.submitEnrollment(courses);
    }

    private void checkEnrollmentConditions(Student student, List<CSE> courses) throws EnrollmentRulesViolationException {
        Map<Term, Map<Course, Double>> transcript = student.getTranscript();
        for (CSE offering : courses) {
            checkCourseIsPassed(student, offering);
            checkPrerequisites(student, offering);
            checkExamTime(courses, offering);
            checkDuplicateEnrollment(courses, offering);
        }
        checkUnitsLimit(courses, student);
    }

    private void checkUnitsLimit(List<CSE> courses, Student student) throws EnrollmentRulesViolationException {
        int unitsRequested = calcRequestedUnits(courses);
        double gpa = student.getGPA();
        if ((gpa < 12 && unitsRequested > 14) ||
                (gpa < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, gpa));
    }

    private int calcRequestedUnits(List<CSE> courses) {
        int unitsRequested = 0;
        for (CSE offering : courses)
            unitsRequested += offering.getCourse().getUnits();
        return unitsRequested;
    }

    private void checkDuplicateEnrollment(List<CSE> courses, CSE offering) throws EnrollmentRulesViolationException {
        for (CSE _offering : courses) {
            if (offering == _offering)
                continue;
            if (offering.getCourse().equals(_offering.getCourse()))
                throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", offering.getCourse().getName()));
        }
    }

    private void checkExamTime(List<CSE> courses, CSE offering) throws EnrollmentRulesViolationException {
        for (CSE _offering : courses) {
            if (offering == _offering)
                continue;
            if (offering.getExamTime().equals(_offering.getExamTime()))
                throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", offering, _offering));
        }
    }

    private void checkPrerequisites(Student student, CSE offering) throws EnrollmentRulesViolationException {
        List<Course> preReqs = offering.getCourse().getPrerequisites();
        for (Course preReq : preReqs) {
            if(!student.courseIsPassed(preReq))
                throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", preReq.getName(), offering.getCourse().getName()));
        }
    }

    private void checkCourseIsPassed(Student student, CSE offering) throws EnrollmentRulesViolationException {
        if(student.courseIsPassed(offering.getCourse()))
            throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", offering.getCourse().getName()));
    }

}
