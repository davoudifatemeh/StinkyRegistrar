package domain;

import java.util.List;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
    public void enroll(Student student, List<CSE> courses) throws EnrollmentRulesViolationException {
        checkEnrollmentConditions(student, courses);
        student.submitEnrollment(courses);
    }

    private void checkEnrollmentConditions(Student student, List<CSE> courses) throws EnrollmentRulesViolationException {
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
        String msg = "Number of units (%d) requested does not match GPA of %f";
        double gpa = student.getGPA();
        if ((gpa < 12 && unitsRequested > 14) ||
                (gpa < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format(msg, unitsRequested, gpa));
    }

    private int calcRequestedUnits(List<CSE> courses) {
        int unitsRequested = 0;
        for (CSE offering : courses)
            unitsRequested += offering.getCourse().getUnits();
        return unitsRequested;
    }

    private void checkDuplicateEnrollment(List<CSE> courses, CSE offering) throws EnrollmentRulesViolationException {
        String msg = "%s is requested to be taken twice";
        for (CSE _offering : courses) {
            if (offering == _offering)
                continue;
            if (offering.getCourse().equals(_offering.getCourse()))
                throw new EnrollmentRulesViolationException(String.format(msg, offering.getCourse().getName()));
        }
    }

    private void checkExamTime(List<CSE> courses, CSE offering) throws EnrollmentRulesViolationException {
        String msg = "Two offerings %s and %s have the same exam time";
        for (CSE _offering : courses) {
            if (offering == _offering)
                continue;
            if (offering.getExamTime().equals(_offering.getExamTime()))
                throw new EnrollmentRulesViolationException(String.format(msg, offering, _offering));
        }
    }

    private void checkPrerequisites(Student student, CSE offering) throws EnrollmentRulesViolationException {
        String msg = "The student has not passed %s as a prerequisite of %s";
        List<Course> preReqs = offering.getCourse().getPrerequisites();
        for (Course preReq : preReqs) {
            if(!student.courseIsPassed(preReq))
                throw new EnrollmentRulesViolationException(String.format(msg, preReq.getName(), offering.getCourse().getName()));
        }
    }

    private void checkCourseIsPassed(Student student, CSE offering) throws EnrollmentRulesViolationException {
        String msg = "The student has already passed %s";
        if(student.courseIsPassed(offering.getCourse()))
            throw new EnrollmentRulesViolationException(String.format(msg, offering.getCourse().getName()));
    }

}
