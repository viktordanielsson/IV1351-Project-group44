package se.kth.iv1351.university.model;

public class Course implements CourseDTO{
    private String courseCode;
    private String courseInstanceId;
    private String period;

    public Course(String courseCode, String courseInstanceId, String period){
        this.courseCode = courseCode;
        this.courseInstanceId = courseInstanceId;
        this.period = period;
    }
    
    public String getCourseCode(){
        return this.courseCode;
    }
    public String getCourseInstanceId(){
        return this.courseInstanceId;
    }
    public String getPeriod(){
        return this.period;
    }
    @Override
    public String toString(){
        StringBuilder string = new StringBuilder();
        string.append("Course Code: ");
        string.append(this.courseCode);
        string.append("\t Course instance: ");
        string.append(this.courseInstanceId);
        string.append("\t Period: ");
        string.append( this.period);
        return string.toString();
    }
}   