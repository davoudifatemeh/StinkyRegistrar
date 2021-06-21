package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
    public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        checkEnrollmentConditions(s, courses);
        s.submitEnrollment(courses);
    }

    private void checkEnrollmentConditions(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        Map<Term, Map<Course, Double>> transcript = s.getTranscript();
        for (CSE o : courses) {
            checkCourseIsPassed(s, o);
            checkPrerequisites(s, o);
            checkExamTime(courses, o);
            checkDuplicateEnrollment(courses, o);
        }
        checkUnitsLimit(courses, s);
    }

    private void checkUnitsLimit(List<CSE> courses, Student s) throws EnrollmentRulesViolationException {
        int unitsRequested = calcRequestedUnits(courses);
        double gpa = s.getGPA();
        if ((gpa < 12 && unitsRequested > 14) ||
                (gpa < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, gpa));
    }

    private int calcRequestedUnits(List<CSE> courses) {
        int unitsRequested = 0;
        for (CSE o : courses)
            unitsRequested += o.getCourse().getUnits();
        return unitsRequested;
    }

    private void checkDuplicateEnrollment(List<CSE> courses, CSE o) throws EnrollmentRulesViolationException {
        for (CSE o2 : courses) {
            if (o == o2)
                continue;
            if (o.getCourse().equals(o2.getCourse()))
                throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
        }
    }

    private void checkExamTime(List<CSE> courses, CSE o) throws EnrollmentRulesViolationException {
        for (CSE o2 : courses) {
            if (o == o2)
                continue;
            if (o.getExamTime().equals(o2.getExamTime()))
                throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
        }
    }

    private void checkPrerequisites(Student s, CSE o) throws EnrollmentRulesViolationException {
        List<Course> prereqs = o.getCourse().getPrerequisites();
        for (Course pre : prereqs) {
            if(!s.courseIsPassed(pre))
                throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
        }
    }

    private void checkCourseIsPassed(Student s, CSE o) throws EnrollmentRulesViolationException {
        if(s.courseIsPassed(o.getCourse()))
            throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
    }

}
