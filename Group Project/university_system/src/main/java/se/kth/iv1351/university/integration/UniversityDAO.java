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

/**
 * This data access object (DAO) handles all calls between controller and the postgres database. 
 */
public class UniversityDAO {
    private static final String COURSE_INSTANCE_ID = "instance_id";
    private static final String COURSE_INSTANCE_PERIOD = "study_period";
    private static final String COURSE_CODE = "course_code";
    private static final String EMPLOYEE_ID = "employment_id";
    private static final String EMPLOYEE_SALARY = "salary";
    private static final String ACTIVITY_NAME = "activity_name";
    private static final String ACTIVITY_FACTOR = "factor";
    private static final String PLANNED_HOURS = "planned_hours";
    private static final String ALLOCATED_HOURS = "allocated_hours";
    private static final String EMPLOYEE_NAME = "Teachers name";

    private Connection connection;

    private PreparedStatement readAllEmployeeAllocationsByCourseInstanceStmt;
    private PreparedStatement readCourseByYearPeriodCourseCodeStmt;
    private PreparedStatement updateCourseInstanceSetNumStudentsStmt;
    private PreparedStatement createEmployeeAllocationStmt;
    private PreparedStatement readSurrogateKeysForEmployeeLoadAllocationStmt;
    private PreparedStatement deleteEmployeeAllocationStmt;
    private PreparedStatement createTeachingActivityStmt;
    private PreparedStatement readTeachingAllocationStmt;


    private static class Keys {
        private final int employeeId;
        private final int teachingActivityId;
        private final int courseInstanceId;


        private Keys(int courseInstanceId, int teachingActivityId, int employeeId) {
            this.employeeId = employeeId;
            this.teachingActivityId = teachingActivityId;
            this.courseInstanceId = courseInstanceId;
        }

        private int getCourseInstanceId() {
            return courseInstanceId;
        }


        private int getEmployeeId() {
            return employeeId;
        }


        private int getTeachingActivityId() {
            return teachingActivityId;
        }
    }


    /**
     * Creates a new instance of the UniversityDAO and connects to the university database.
     * Prepares necessary SQL statements for database interaction.
     *
     * @throws UniversityDBException If failed to connect to the database or prepare statements.
     */
    public UniversityDAO() throws UniversityDBException {
        try {
            connectToUniversityDB();
            prepareStatements();
        } catch (ClassNotFoundException | SQLException exception) {
            System.out.println(exception);
            // Re-throw as UniversityDBException for consistency in the DAO layer
            throw new UniversityDBException("Failed to initialize UniversityDAO", exception);
        }
    }

    /**
     * Establishes a connection to the university database.
     * Sets auto-commit to false and transaction isolation level to SERIALIZABLE.
     *
     * @throws ClassNotFoundException If the JDBC driver is not found.
     * @throws SQLException If a database access error occurs.
     */
    private void connectToUniversityDB() throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/university_db", "postgres", "postgres");

        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }


    /**
     * Reads all employee load allocations for a specific course instance, identified by its study year,
     * study period, and course code.
     *
     * @param studyYear The study year of the course instance.
     * @param studyPeriod The study period of the course instance (e.g., P1, P2).
     * @param courseCode The code of the course (e.g., IX1307).
     * @return A list of {@link EmployeeAllocation} objects for the specified course instance.
     * @throws UniversityDBException If a database error occurs while reading the allocations.
     */
    public List<EmployeeAllocation> readEmployeeAllocationByPeriodYearCourseCode(String studyYear, String studyPeriod, String courseCode)
            throws UniversityDBException {
        Course course = readCourseByYearPeriodCourseCode(studyYear, studyPeriod, courseCode);
        PreparedStatement stmtToExequte = readAllEmployeeAllocationsByCourseInstanceStmt;
        ResultSet results = null;
        List<EmployeeAllocation> employeeAllocations = new ArrayList<>();
        try {
            stmtToExequte.setString(1, course.getCourseInstanceId());
            results = stmtToExequte.executeQuery();
            while (results.next()) {
                employeeAllocations.add(new EmployeeAllocation
                        (
                                new Employee(
                                        results.getInt(EMPLOYEE_SALARY),
                                        results.getString(EMPLOYEE_ID),
                                        results.getString(EMPLOYEE_NAME)
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
        } catch (SQLException sqle) {
            handleException("Could not find employees allocated to course " + courseCode + " in period " + studyPeriod + " in year" + studyYear, sqle);
        } finally {
            closeResultSet(results);
        }
        return employeeAllocations;
    }


    /**
     * Updates the number of students enrolled in a specific course instance.
     *
     * @param studyYear The study year of the course instance.
     * @param studyPeriod The study period of the course instance.
     * @param courseCode The course code.
     * @param modifyAmount The new number of students (or the modification amount, depending on the SQL statement's logic).
     * @throws UniversityDBException If a database error occurs during the update or commit.
     */
    public void updateCourseInstanceNumberOfStudents(String studyYear, String studyPeriod, String courseCode, int modifyAmount)
            throws UniversityDBException {
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
        } catch (SQLException sqle) {
            handleException("Unable to update course instance " + courseCode + " at period " + studyPeriod + " in year " + studyYear, sqle);
        }
    }

    /**
     * Creates a new employee load allocation for a specific course activity.
     *
     * @param studyYear The study year of the course instance.
     * @param studyPeriod The study period of the course instance.
     * @param courseCode The course code.
     * @param activityName The name of the teaching activity (e.g., 'Lecture', 'Exercise').
     * @param employmentId The employment ID of the employee.
     * @param hoursAllocated The number of hours allocated to the employee for this activity.
     * @throws UniversityDBException If a database error occurs during the creation or commit.
     */
    public void createEmployeeAllocation(String studyYear, String studyPeriod, String courseCode, String activityName, String employmentId, double hoursAllocated)
            throws UniversityDBException {
        Course course = readCourseByYearPeriodCourseCode(studyYear, studyPeriod, courseCode);
        Keys keys = readSurrogateKeysForEmployeeLoadAllocation(course, activityName, employmentId);

        PreparedStatement stmtToExequte = createEmployeeAllocationStmt;

        try {
            stmtToExequte.setInt(1, keys.teachingActivityId);
            stmtToExequte.setInt(2, keys.courseInstanceId);
            stmtToExequte.setInt(3, keys.employeeId);
            stmtToExequte.setDouble(4, hoursAllocated);
            int updatedRows = stmtToExequte.executeUpdate();
            if (updatedRows != 1) {
                System.out.print("something went wrong");
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException("Could not allocate employee " + employmentId + " to " + courseCode + " for " + studyPeriod + " and " + studyYear, sqle);
        }
    }

    /**
     * Deletes an existing employee load allocation for a specific course activity.
     *
     * @param studyYear The study year of the course instance.
     * @param studyPeriod The study period of the course instance.
     * @param courseCode The course code.
     * @param activityName The name of the teaching activity.
     * @param employmentId The employment ID of the employee.
     * @throws UniversityDBException If a database error occurs during the deletion or commit.
     */
    public void deleteEmployeeAllocation(String studyYear, String studyPeriod, String courseCode, String activityName, String employmentId)
            throws UniversityDBException {
        Course course = readCourseByYearPeriodCourseCode(studyYear, studyPeriod, courseCode);
        Keys keys = readSurrogateKeysForEmployeeLoadAllocation(course, activityName, employmentId);

        PreparedStatement stmtToExequte = deleteEmployeeAllocationStmt;

        try {
            stmtToExequte.setInt(1, keys.courseInstanceId);
            stmtToExequte.setInt(2, keys.teachingActivityId);
            stmtToExequte.setInt(3, keys.employeeId);
            int deleteRows = stmtToExequte.executeUpdate();
            if (deleteRows < 1) {
                System.out.print("Something went poorly \n");
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException("Could not delete employee " + employmentId + " from " + courseCode + " for " + studyPeriod + " and " + studyYear, sqle);
        }
    }


    /**
     * Creates a new teaching activity type in the database.
     *
     * @param activityName The name of the new teaching activity.
     * @param factor The factor associated with this activity.
     * @throws UniversityDBException If a database error occurs during the creation or commit.
     */
    public void createTeachingActivity(String activityName, double factor)
            throws UniversityDBException {
        PreparedStatement stmtToExequte = createTeachingActivityStmt;

        try {
            stmtToExequte.setString(1, activityName);
            stmtToExequte.setDouble(2, factor);
            int insertedRows = stmtToExequte.executeUpdate();
            if (insertedRows != 1) {
                System.out.println("Something went wrong");
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException("Could not create new teaching " + activityName, sqle);
        }
    }

    public EmployeeAllocation readTeachersAllocation(String studyYear, String studyPeriod, String courseCode,String activityName, String employmentId)
    throws UniversityDBException
    {
        Course course = readCourseByYearPeriodCourseCode(studyYear, studyPeriod, courseCode);
        ResultSet result = null;
        PreparedStatement stmtToExequte = readTeachingAllocationStmt;
        EmployeeAllocation employeeAllocation = null;
        try {
            stmtToExequte.setString(1, activityName);
            stmtToExequte.setString(2, course.getCourseInstanceId());
            stmtToExequte.setString(3, employmentId);
            result = stmtToExequte.executeQuery();
            if(result.next()){
                employeeAllocation = new EmployeeAllocation(
                                new Employee(
                                        result.getInt(EMPLOYEE_SALARY),
                                        result.getString(EMPLOYEE_ID),
                                        result.getString(EMPLOYEE_NAME)
                                ),
                                new Activity(
                                        result.getString(ACTIVITY_NAME),
                                        result.getDouble(ACTIVITY_FACTOR),
                                        result.getDouble(PLANNED_HOURS),
                                        course.getCourseInstanceId()
                                ),
                                result.getDouble(ALLOCATED_HOURS));
            }
        } catch (SQLException sqle) {
            handleException("Could not find employee " + employmentId + " for " + courseCode + " in " + studyPeriod + " and " + studyYear + " in activity: "+activityName, sqle);
        }
        return employeeAllocation;
    }

    /**
     * Reads a specific course instance from the database using its identifying attributes.
     *
     * @param studyYear The study year of the course instance.
     * @param studyPeriod The study period of the course instance.
     * @param courseCode The course code.
     * @return The {@link Course} object representing the course instance, or null if not found.
     * @throws UniversityDBException If a database error occurs during the read operation.
     */
    private Course readCourseByYearPeriodCourseCode(String studyYear, String studyPeriod, String courseCode)
            throws UniversityDBException {
        PreparedStatement stmtToExequte = readCourseByYearPeriodCourseCodeStmt;
        ResultSet result = null;
        Course course = null;
        try {
            stmtToExequte.setString(1, studyYear);
            stmtToExequte.setString(2, studyPeriod);
            stmtToExequte.setString(3, courseCode);

            result = stmtToExequte.executeQuery();
            if (result.next()) {
                course = new Course(result.getString(COURSE_CODE),
                        result.getString(COURSE_INSTANCE_ID),
                        result.getString(COURSE_INSTANCE_PERIOD));
            }


        } catch (SQLException sqle) {
            handleException("Could not find the correct course by:" + studyYear + ", " + studyPeriod + ", " + courseCode, sqle);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            closeResultSet(result);
        }
        return course;
    }


    /**
     * Retrieves the database's internal surrogate keys (IDs) for a course instance,
     * teaching activity, and employee, required for creating or deleting an employee load allocation.
     *
     * @param course The {@link Course} object containing the course instance ID.
     * @param activityName The name of the teaching activity.
     * @param employmentId The employment ID of the employee.
     * @return A {@link Keys} object containing the courseInstanceId, teachingActivityId, and employeeId, or null if not found.
     * @throws UniversityDBException If a database error occurs during the read operation.
     */
    private Keys readSurrogateKeysForEmployeeLoadAllocation(Course course, String activityName, String employmentId)
            throws UniversityDBException {
        PreparedStatement stmtToExequte = readSurrogateKeysForEmployeeLoadAllocationStmt;
        ResultSet result = null;

        try {
            stmtToExequte.setString(1, course.getCourseInstanceId());
            stmtToExequte.setString(2, activityName);
            stmtToExequte.setString(3, employmentId);

            result = stmtToExequte.executeQuery();
            if (result.next()) {
                return new Keys(result.getInt(1), result.getInt(2), result.getInt(3));
            }
        } catch (SQLException sqle) {
            handleException("No keys were found for course " + course.toString() + " at activity" + activityName + " for employee " + employmentId, sqle);
        } finally {
            closeResultSet(result);
        }
        return null;
    }

    /**
     * Closes the provided {@link ResultSet}.
     *
     * @param result The {@link ResultSet} to close.
     * @throws UniversityDBException If failed to close the result set.
     */
    private void closeResultSet(ResultSet result)
            throws UniversityDBException {
        try {
            if (result != null) {
                result.close();
            }
        } catch (SQLException e) {
            handleException("unable to close resultSet", e);
        }
    }

    /**
     * Handles exceptions by rolling back the current transaction and throwing a new {@link UniversityDBException}.
     *
     * @param failureMsg A message describing the failure.
     * @param cause The underlying exception that caused the failure (can be null).
     * @throws UniversityDBException Always thrown with the complete failure message.
     */
    private void handleException(String failureMsg, Exception cause)
            throws UniversityDBException {
        String completeFailureMsg = failureMsg;
        try {
            connection.rollback();
        } catch (SQLException rollbackExc) {
            completeFailureMsg = completeFailureMsg +
                    ". Also failed to rollback transaction because of: " + rollbackExc.getMessage();
        }

        if (cause != null) {
            throw new UniversityDBException(completeFailureMsg + " " + cause.getMessage(), cause);
        } else {
            throw new UniversityDBException(failureMsg);
        }
    }


    /**
     * Initializes and prepares all SQL {@link PreparedStatement}s used by the DAO.
     *
     * @throws SQLException If a database access error occurs during statement preparation.
     */
    private void prepareStatements() throws SQLException {

        /**
         *
         * Task 1 find
         *
         */
        readCourseByYearPeriodCourseCodeStmt = connection.prepareStatement(
                "SELECT c." + COURSE_CODE + ", ci." + COURSE_INSTANCE_ID + ", ci." + COURSE_INSTANCE_PERIOD +
                        " FROM course_instance ci " +
                        "JOIN course_layout cl ON cl.id = ci.course_layout_id " +
                        "JOIN course c ON c.id = cl.course_id " +
                        "WHERE ci.study_year = ? AND ci.study_period = ? AND c.course_code = ?"
        );

        readAllEmployeeAllocationsByCourseInstanceStmt = connection.prepareStatement
                (
                        "SELECT ta." + ACTIVITY_NAME + ", ta." + ACTIVITY_FACTOR + ", pa." + PLANNED_HOURS + ", ci." + COURSE_INSTANCE_ID + ", e." + EMPLOYEE_ID + ", e." + EMPLOYEE_SALARY +", ela." + ALLOCATED_HOURS+
                        ", CONCAT(p.first_name, ' ', p.last_name) AS \"" + EMPLOYEE_NAME + "\" " +
                                " FROM teaching_activity ta " +
                                "JOIN planned_activity pa ON pa.teaching_activity_id = ta.id " +
                                "JOIN course_instance ci ON ci.id = pa.course_instance_id " +
                                "JOIN employee_load_allocation ela ON pa.course_instance_id = ela.course_instance_id AND pa.teaching_activity_id = ela.teaching_activity_id " +
                                "JOIN employee e ON e.id = ela.employee_id " +
                                "JOIN person p ON p.id = e.person_id "+
                                "WHERE ci.instance_id = ?"
                );

        /**
         *
         * Task 2 update course
         *
         */
        updateCourseInstanceSetNumStudentsStmt = connection.prepareStatement
                (
                        "UPDATE course_instance ci " +
                                "SET num_students = ? " +
                                "FROM course_layout cl, course c " +
                                "WHERE ci.course_layout_id = cl.id " +
                                "AND cl.course_id = c.id " +
                                "AND c.course_code = ? " +
                                "AND ci.study_period = ? " +
                                "AND ci.study_year = ?"
                );


        /*
         *
         * TASK 3 allocated employee
         *
         */

        readSurrogateKeysForEmployeeLoadAllocationStmt = connection.prepareStatement(
                "SELECT ci.id, ta.id , e.id " +
                        "FROM course_instance ci " +
                        "CROSS JOIN teaching_activity ta " +
                        "CROSS JOIN employee e " +
                        "WHERE ci.instance_id = ? " +
                        "AND ta.activity_name = ? " +
                        "AND e.employment_id = ? "
        );

        createEmployeeAllocationStmt = connection.prepareStatement
                (
                        "INSERT INTO employee_load_allocation(teaching_activity_id, course_instance_id, employee_id, allocated_hours)" +
                                "VALUES (?, ?, ?, ?)"
                );

        deleteEmployeeAllocationStmt = connection.prepareStatement
                (
                        "DELETE FROM employee_load_allocation ela " +
                                "WHERE ela.course_instance_id = ? AND ela.teaching_activity_id = ? AND ela.employee_id = ?"
                );

        /**
         *
         * TASK 4
         *
         */
        createTeachingActivityStmt = connection.prepareStatement
                (
                        "INSERT INTO teaching_activity (" + ACTIVITY_NAME + ", " + ACTIVITY_FACTOR + ") " +
                                "VALUES (?, ?)"
                );
        
        readTeachingAllocationStmt = connection.prepareStatement(
            "SELECT " +
            "  ta." + ACTIVITY_NAME + ", " +
            "  ta." + ACTIVITY_FACTOR + ", " +
            "  pa." + PLANNED_HOURS + ", " +
            "  ci." + COURSE_INSTANCE_ID + ", " +
            "  e." + EMPLOYEE_ID + ", " +
            "  e." + EMPLOYEE_SALARY + ", " +
            "  ela." + ALLOCATED_HOURS + ", " +
            "  c.course_code AS \"" + COURSE_CODE + "\", " +
            "  CONCAT(p.first_name, ' ', p.last_name) AS \"" + EMPLOYEE_NAME + "\" " +
            "FROM course c " +
            "JOIN course_layout cl ON cl.course_id = c.id " +
            "JOIN course_instance ci ON ci.course_layout_id = cl.id " +
            "JOIN employee_load_allocation ela ON ela.course_instance_id = ci.id " +
            "JOIN teaching_activity ta ON ta.id = ela.teaching_activity_id " +
            "JOIN employee e ON e.id = ela.employee_id " +
            "JOIN person p ON p.id = e.person_id " +
            "JOIN planned_activity pa ON pa.teaching_activity_id = ta.id " +
            "WHERE ta.activity_name = ? " +
            "AND ci.instance_id = ? " +
            "AND e.employment_id = ? " +
            "ORDER BY ci.instance_id"
        );
    }
}