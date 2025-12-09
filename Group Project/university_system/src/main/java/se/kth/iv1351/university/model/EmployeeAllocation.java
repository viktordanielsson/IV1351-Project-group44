package se.kth.iv1351.university.model;

public class EmployeeAllocation {
    Employee employee;
    Activity activity;
    Double hoursAllocated;

    public EmployeeAllocation(Employee employee, Activity activity, double hoursAllocated){
        this.employee = employee;
        this.activity = activity;
        this.hoursAllocated = hoursAllocated;
    }
    
    public Activity getActivity() {
        return this.activity;
    }
    public Employee getEmployee() {
        return this.employee;
    }

    public Double getHoursAllocated() {
        return this.hoursAllocated;
    }
}

