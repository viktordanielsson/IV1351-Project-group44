package se.kth.iv1351.university.model;

public class Activity implements ActivityDTO{
    
    private String activityName;
    private double factor;
    private double plannedHours;
    private String courseInstanceId;

    public Activity(String activityName, double factor, double plannedHours, String courseInstanceId){
        this.activityName = activityName;
        this.factor = factor;
        this.plannedHours = plannedHours;
        this.courseInstanceId = courseInstanceId;
    }

    public String getActivityName(){
        return this.activityName;
    }
    public double getFactor(){
        return this.factor;
    }

    public String getCourseInstanceId(){
        return this.courseInstanceId;
    }

    public double getPlannedHours(){
        return this.plannedHours;
    }

    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("Activity name: ");
        string.append(this.activityName);
        string.append("\t Activity factor: ");
        string.append(this.factor);
        string.append("\t Planned hours: ");
        string.append(this.plannedHours);
        string.append("\t Course instance ID: ");
        string.append(this.courseInstanceId);
        return string.toString();
    }
}
