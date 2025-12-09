package se.kth.iv1351.university.integration;

import se.kth.iv1351.university.model.Activity;
import se.kth.iv1351.university.model.Course;
import se.kth.iv1351.university.model.Employee;
import se.kth.iv1351.university.model.EmployeeAllocation;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class UniversityDAO {
    private static final String COURSE_INSTANCE_ID = "instance_id";
    private static final String COURSE_INSTANCE_PERIOD = "study_period";
    private static final String COURSE_CODE = "course_code";
    private static final String EMPLOYEE_ID = "employment_id";
    private static final String EMPLOYEE_SALARY = "salary";
    private static final String ACTIVITY_NAME = "activity_name";
    private static final String ACTIVITY_FACTOR = "factor";
    private static final String PLANNED_HOURS  = "planned_hours";
    private static final String ALLOCATED_HOURS = "allocated_hours";
    
    private Connection connection;

    private PreparedStatement findAllEmployeeAllocationsByCourseInstanceStmt;
    private PreparedStatement findCourseByYearPeriodCourseCodeStmt;
    private PreparedStatement updateCourseInstanceSetNumStudentsStmt;
    private PreparedStatement createEmployeeAllocationStmt;
    private PreparedStatement findSurrogateKeysForEmployeeLoadAllocation;
    
    public UniversityDAO(){
        try {
            connectToUniversityDB();
            prepareStatements();
        } catch (ClassNotFoundException | SQLException exception) {
            System.out.println(exception);
        }
    }

    private void connectToUniversityDB() throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/university_db", "postgres", "postgres");

        connection.setAutoCommit(false);
    }
    

    public List<EmployeeAllocation> findEmployeeAllocationByPeriodYearCourseCode(String studyYear, String studyPeriod, String courseCode){
        PreparedStatement stmtToExequte = findCourseByYearPeriodCourseCodeStmt;
        ResultSet result = null;
        Course course = null;
        try {
            stmtToExequte.setString(1, studyYear);
            stmtToExequte.setString(2, studyPeriod);
            stmtToExequte.setString(3, courseCode);

            result = stmtToExequte.executeQuery();
            if(result.next()){
                course = new Course(    result.getString(COURSE_CODE), 
                                        result.getString(COURSE_INSTANCE_ID),
                                        result.getString(COURSE_INSTANCE_PERIOD));
            }
            
        } catch (Exception e) {
            
        }
        finally{
            closeResultSet(result);
        }
        stmtToExequte = findAllEmployeeAllocationsByCourseInstanceStmt;
        ResultSet results = null;
        List<EmployeeAllocation> employeeAllocations = new ArrayList<>();
        try{
            stmtToExequte.setString(1, course.getCourseInstanceId());
            results = stmtToExequte.executeQuery();
            while (results.next()) {
            //if(results.next()){
                employeeAllocations.add(new EmployeeAllocation
                                            (
                                            new Employee(
                                                        results.getInt(EMPLOYEE_SALARY), 
                                                        results.getString(EMPLOYEE_ID)
                                                        ), 
                                            new Activity(
                                                        results.getString(ACTIVITY_NAME), 
                                                        results.getDouble(ACTIVITY_FACTOR), 
                                                        results.getDouble(PLANNED_HOURS), 
                                                        course.getCourseInstanceId()
                                                        ),
                                            results.getDouble(ALLOCATED_HOURS)
                                            )
                                        );
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
        finally{
            closeResultSet(results);
        }
        return employeeAllocations;
    }

    public void updateCourseInstanceNumberOfStudents(String studyYear, String studyPeriod, String courseCode, int modifyAmount){
        try {
            updateCourseInstanceSetNumStudentsStmt.setInt(1, modifyAmount);
            updateCourseInstanceSetNumStudentsStmt.setString(2, courseCode);
            updateCourseInstanceSetNumStudentsStmt.setString(3, studyPeriod);
            updateCourseInstanceSetNumStudentsStmt.setString(4, studyYear);
            int updatedRows = updateCourseInstanceSetNumStudentsStmt.executeUpdate();
            if (updatedRows != 1) {
                System.out.print("something went wrong");
            }
            connection.commit();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void createEmployeeAllocation(String studyYear, String studyPeriod, String courseCode, String employmentId, String activityName){
        PreparedStatement stmtToExequte = findCourseByYearPeriodCourseCodeStmt;
        ResultSet result = null;
        Course course = null;
        try {
            stmtToExequte.setString(1, studyYear);
            stmtToExequte.setString(2, studyPeriod);
            stmtToExequte.setString(3, courseCode);

            result = stmtToExequte.executeQuery();
            if(result.next()){
                course = new Course(    result.getString(COURSE_CODE), 
                                        result.getString(COURSE_INSTANCE_ID),
                                        result.getString(COURSE_INSTANCE_PERIOD));
            }
            
        } catch (Exception e) {
            
        }
        finally{
            closeResultSet(result);
        }

        stmtToExequte = findSurrogateKeysForEmployeeLoadAllocation;
        result = null;
        int employeeId = 0;
        int teachingActivityId = 0;
        int courseInstanceId = 0;

        try{
            stmtToExequte.setString(1, course.getCourseInstanceId());
            stmtToExequte.setString(2, employmentId);
            stmtToExequte.setString(3, activityName);

            result = stmtToExequte.executeQuery();
            if(result.next()){
                courseInstanceId = result.getInt("ci.id");
                employeeId = result.getInt("e.id");
                teachingActivityId = result.getInt("ta.id");
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
        finally{
            closeResultSet(result);
        }
    }



    private void closeResultSet(ResultSet result) {
        try {
            result.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void prepareStatements() throws SQLException{

        findCourseByYearPeriodCourseCodeStmt = connection.prepareStatement(
            "SELECT ci."+COURSE_INSTANCE_ID+", ci."+COURSE_INSTANCE_PERIOD+", c."+COURSE_CODE+
            " FROM course_instance ci " + 
            "JOIN course_layout cl ON cl.id = ci.course_layout_id " +
            "JOIN course c ON c.id = cl.course_id " +
            "WHERE ci.study_year = ? AND ci.study_period = ? AND c.course_code = ?"
        );
        
        findAllEmployeeAllocationsByCourseInstanceStmt = connection.prepareStatement
        (
            "SELECT ta."+ACTIVITY_NAME+", ta."+ACTIVITY_FACTOR+", pa."+PLANNED_HOURS+", ci."+COURSE_INSTANCE_ID+", e."+EMPLOYEE_ID+", e."+EMPLOYEE_SALARY+", ela."+ALLOCATED_HOURS+
            " FROM teaching_activity ta " +
            "JOIN planned_activity pa ON pa.teaching_activity_id = ta.id " + 
            "JOIN course_instance ci ON ci.id = pa.course_instance_id " + 
            "JOIN employee_load_allocation ela ON pa.course_instance_id = ela.course_instance_id AND pa.teaching_activity_id = ela.teaching_activity_id "+
            "JOIN employee e ON e.id = ela.employee_id "+
            "WHERE ci.instance_id = ?"
        );

        /**
         * 
         * TASk 2 update course
         * 
         */
        updateCourseInstanceSetNumStudentsStmt = connection.prepareStatement
        ( 
            "UPDATE course_instance ci " +
            "SET num_students = ? "+
            "FROM course_layout cl, course c "+
            "WHERE ci.course_layout_id = cl.id "+
            "AND cl.course_id = c.id "+
            "AND c.course_code = ? "+
            "AND ci.study_period = ? "+
            "AND ci.study_year = ?"
        );


        /*
        *
        * TASK 3 allocated employee
        * 
        */

        findSurrogateKeysForEmployeeLoadAllocation = connection.prepareStatement(
            "SELECT ci.id, ta.id, e.id "+
            "FROM employee_load_allocation ela "+
            "JOIN course_instance ci ON ci.id = ela.course_instance_id "+
            "JOIN teaching_activity ta ON ta.id = ela.teaching_activity_id "+
            "JOIN employee e ON e.id = ela.employee_id "+
            "WHERE ci.instance_id = ? "+
            "AND e.employment_id = ? "+
            "AND ta.activity_name = ?"
        );

        createEmployeeAllocationStmt = connection.prepareStatement
        (
            "INSERT INTO employee_load_allocation() VALUES"
        );
        /*
       findEmployeeLoadAllocationByEmploymentIdStmt = connection.prepareStatement
       (
            "SELECT ta."+ACTIVITY_NAME+", ta."+ACTIVITY_FACTOR+", pa."+PLANNED_HOURS+", ci."+COURSE_INSTANCE_ID+", e."+EMPLOYEE_ID+", e."+EMPLOYEE_SALARY+", ela."+ALLOCATED_HOURS+
            " FROM teaching_activity ta " +
            "JOIN planned_activity pa ON pa.teaching_activity_id = ta.id " + 
            "JOIN course_instance ci ON ci.id = pa.course_instance_id " + 
            "JOIN employee_load_allocation ela ON pa.course_instance_id = ela.course_instance_id AND pa.teaching_activity_id = ela.teaching_activity_id "+
            "JOIN employee e ON e.id = ela.employee_id "+
            "WHERE ci.instance_id = ? AND e.employment_id = ?"
       );
        */
    }
}
