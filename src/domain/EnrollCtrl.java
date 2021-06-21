package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
    public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        checkEnrollmentConditions(s, courses);
        submitEnrollment(s, courses);
    }

    private void submitEnrollment(Student s, List<CSE> courses) {
        for (CSE o : courses)
            s.takeCourse(o.getCourse(), o.getSection());
    }

    private void checkEnrollmentConditions(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        Map<Term, Map<Course, Double>> transcript = s.getTranscript();
        for (CSE o : courses) {
            checkCourseIsPassed(transcript, o);
            checkPrerequisites(transcript, o);
            checkExamTime(courses, o);
            checkDuplicateEnrollment(courses, o);
        }
        checkUnitsLimit(courses, transcript);
    }

    private void checkUnitsLimit(List<CSE> courses, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        int unitsRequested = calcRequestedUnits(courses);
        double gpa = getGPA(transcript);
        if ((gpa < 12 && unitsRequested > 14) ||
                (gpa < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, gpa));
    }

    private double getGPA(Map<Term, Map<Course, Double>> transcript) {
        double points = 0;
        int totalUnits = 0;
        for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
            for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                points += r.getValue() * r.getKey().getUnits();
                totalUnits += r.getKey().getUnits();
            }
        }
        double gpa = points / totalUnits;
        return gpa;
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

    private void checkPrerequisites(Map<Term, Map<Course, Double>> transcript, CSE o) throws EnrollmentRulesViolationException {
        List<Course> prereqs = o.getCourse().getPrerequisites();
        nextPre:
        for (Course pre : prereqs) {
            if(!courseIsPassed(transcript, pre))
                throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
        }
    }

    private void checkCourseIsPassed(Map<Term, Map<Course, Double>> transcript, CSE o) throws EnrollmentRulesViolationException {
        if(courseIsPassed(transcript, o.getCourse()))
            throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
    }
    public boolean courseIsPassed(Map<Term, Map<Course, Double>> transcript, Course o) {
        for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
            for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                if (r.getKey().equals(o) && r.getValue() >= 10)
                    return true;
            }
        }
        return false;
    }
}
