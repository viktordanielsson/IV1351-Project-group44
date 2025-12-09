package se.kth.iv1351.university.controller;

import java.util.List;

import se.kth.iv1351.university.integration.UniversityDAO;

import se.kth.iv1351.university.model.EmployeeAllocation;
public class Controller {
    private final UniversityDAO uniDB;
    private final int TEACHER_PLANNED_SALARY = 300;

    public Controller(){
        uniDB = new UniversityDAO();
    }

    public void getAllEmployees(){
        uniDB.toString();
    }


    //TASK 1 
    public String calculateTeachingCost(String studyYear, String studyPeriod, String courseCode){
        List<EmployeeAllocation> employeeAllocations = uniDB.findEmployeeAllocationByPeriodYearCourseCode(studyYear,studyPeriod,courseCode);

        StringBuilder sb = new StringBuilder();
        sb.append("Course Code\t Course Instance\tPeriod\tplanned cost\t actual cost\n");
        sb.append(courseCode+"\t\t "+
        employeeAllocations.getFirst().getActivity().getCourseInstanceId()+"\t\t "+
        studyPeriod+"\t");  

        int plannedCost = 0;
        int actualCost = 0;

        for (EmployeeAllocation allocation : employeeAllocations) {
            plannedCost += allocation.getActivity().getPlannedHours()*TEACHER_PLANNED_SALARY ;
            actualCost += allocation.getEmployee().getSalary()*allocation.getHoursAllocated();
        }

        sb.append(plannedCost+ "\t\t   "+actualCost);

        return sb.toString();
    }
    

    //TASK 2
    public String modifyCourseInstanceStudentNumber(String studyYear, String studyPeriod, String courseCode, int modifyAmount){
        uniDB.updateCourseInstanceNumberOfStudents(studyYear, studyPeriod, courseCode, modifyAmount);
        return "Update executed";
    }

    //TASK 3
    public void allocatedTeacherToCourseInstance(String studyYear, String studyPeriod, String courseCode, String employmentId, String activityName, double hoursAllocated){  
        uniDB.createEmployeeAllocation(studyYear, studyPeriod, courseCode, employmentId, activityName);
    }
}
