package se.kth.iv1351.university.controller;

import java.util.List;

import se.kth.iv1351.university.integration.UniversityDAO;
import se.kth.iv1351.university.integration.UniversityDBException;
import se.kth.iv1351.university.model.Cost;
import se.kth.iv1351.university.model.CostDTO;
import se.kth.iv1351.university.model.EmployeeAllocation;
import se.kth.iv1351.university.model.EmployeeAllocationDTO;
public class Controller {
    private final UniversityDAO uniDB;

    public Controller() throws UniversityDBException{
        try {
            uniDB = new UniversityDAO();
        } catch (Exception e) {
            throw new UniversityDBException("null");
        }
    }

    public void getAllEmployees(){
        uniDB.toString();
    }


    //TASK 1 
    public CostDTO calculateTeachingCost(String studyYear, String studyPeriod, String courseCode)
    throws UniversityDBException
    {
        try {
            List<EmployeeAllocation> employeeAllocations = uniDB.readEmployeeAllocationByPeriodYearCourseCode(studyYear,studyPeriod,courseCode);
            CostDTO costDTO = new Cost(employeeAllocations);
            return costDTO;
        } catch (UniversityDBException udbe) {
            throw new UniversityDBException(udbe.toString());
        }
    }
    

    //TASK 2
    public String modifyCourseInstanceStudentNumber(String studyYear, String studyPeriod, String courseCode, int modifyAmount)
    throws UniversityDBException
    {
        try {
            uniDB.updateCourseInstanceNumberOfStudents(studyYear, studyPeriod, courseCode, modifyAmount);
        } catch (UniversityDBException udbe) {
            throw new UniversityDBException(udbe.toString());
        }
        return "Update executed";
    }

    //TASK 3
    public String allocatedTeacherToCourseInstance(String studyYear, String studyPeriod, String courseCode, String activityName, String employmentId, double hoursAllocated)
    throws UniversityDBException {  
        try {
            uniDB.createEmployeeAllocation(studyYear, studyPeriod, courseCode, activityName, employmentId, hoursAllocated);
            return "Employee allocated";
        } catch (UniversityDBException udbe) {
            throw new UniversityDBException(udbe.toString());
        } 
    }

    public String deallocateTeacherToCourseActivity(String studyYear, String studyPeriod, String courseCode, String activityName,String employmentId)
    throws UniversityDBException
    {
        try {
            uniDB.deleteEmployeeAllocation(studyYear, studyPeriod, courseCode, activityName, employmentId);
            return "Deletion completed";
        } catch (UniversityDBException udbe) {
            throw new UniversityDBException(udbe.toString());
        }
    }

    //TASK 4
    public String addNewTeachingActivity(String activityName, double factor)
    throws UniversityDBException
    {
        try {
            uniDB.createTeachingActivity(activityName, factor);
        } catch (UniversityDBException udbe) {
            throw new UniversityDBException(udbe.toString());
        }
        return "";
    }

    public EmployeeAllocationDTO displayTeacherAllocation(String studyYear, String studyPeriod, String courseCode,String activityName,String employmentId)
    throws UniversityDBException
    {
        try {
            return uniDB.readTeachersAllocation(studyYear, studyPeriod, courseCode, activityName,employmentId);
        } catch (UniversityDBException udbe) {
            throw new UniversityDBException(udbe.toString());
        }
    }
}
