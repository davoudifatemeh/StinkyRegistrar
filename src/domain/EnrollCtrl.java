package domain;

import java.util.List;

public class EnrollCtrl {
    public boolean enroll(Student student, List<CSE> courses) {
        boolean result = checkEnrollmentConditions(student, courses);
        if(result)
            student.submitEnrollment(courses);
        return result;
    }

    private boolean checkEnrollmentConditions(Student student, List<CSE> courses) {
        boolean result = true;
        for (CSE offering : courses) {
            result = result && checkCourseIsPassed(student, offering);
            result = result && checkPrerequisites(student, offering);
            result = result && checkExamTime(courses, offering);
            result = result && checkDuplicateEnrollment(courses, offering);
        }
        result = result && checkUnitsLimit(courses, student);
        return result;
    }

    private boolean checkUnitsLimit(List<CSE> courses, Student student) {
        int unitsRequested = calcRequestedUnits(courses);
        String msg = "Number of units (%d) requested does not match GPA of %f";
        double gpa = student.getGPA();
        if ((gpa < 12 && unitsRequested > 14) ||
                (gpa < 16 && unitsRequested > 16) ||
                (unitsRequested > 20)) {
            System.out.println(String.format(msg, unitsRequested, gpa));
            return false;
        }
        return true;
    }

    private int calcRequestedUnits(List<CSE> courses) {
        int unitsRequested = 0;
        for (CSE offering : courses)
            unitsRequested += offering.getCourse().getUnits();
        return unitsRequested;
    }

    private boolean checkDuplicateEnrollment(List<CSE> courses, CSE offering) {
        String msg = "%s is requested to be taken twice";
        for (CSE _offering : courses) {
            if (offering == _offering)
                continue;
            if (offering.getCourse().equals(_offering.getCourse())) {
                System.out.println(String.format(msg, offering.getCourse().getName()));
                return false;
            }
        }
        return true;
    }

    private boolean checkExamTime(List<CSE> courses, CSE offering) {
        String msg = "Two offerings %s and %s have the same exam time";
        for (CSE _offering : courses) {
            if (offering == _offering)
                continue;
            if (offering.getExamTime().equals(_offering.getExamTime())) {
                System.out.println(String.format(msg, offering, _offering));
                return false;
            }
        }
        return true;
    }

    private boolean checkPrerequisites(Student student, CSE offering) {
        String msg = "The student has not passed %s as a prerequisite of %s";
        List<Course> preReqs = offering.getCourse().getPrerequisites();
        for (Course preReq : preReqs) {
            if(!student.courseIsPassed(preReq)) {
                System.out.println(String.format(msg, preReq.getName(), offering.getCourse().getName()));
                return false;
            }
        }
        return true;
    }

    private boolean checkCourseIsPassed(Student student, CSE offering) {
        String msg = "The student has already passed %s";
        if(student.courseIsPassed(offering.getCourse())) {
            System.out.println(String.format(msg, offering.getCourse().getName()));
            return false;
        }
        return true;
    }

}
