package se.kth.iv1351.university.model;

import java.util.List;

public class Cost implements CostDTO{
    double plannedCost;
    double actualCost;
    String courseInstanceId;


    public Cost(List<EmployeeAllocation> employeeAllocations){
        for (EmployeeAllocation allocation : employeeAllocations) {
            this.plannedCost += allocation.getActivity().getPlannedHours()*300;
            this.actualCost += allocation.getEmployee().getSalary()*allocation.getHoursAllocated();
        }
        this.courseInstanceId = employeeAllocations.getFirst().getActivity().getCourseInstanceId();
    }

    public double getActualCost() {
        return this.actualCost;
    }

    public String getCourseInstanceId() {
        return this.courseInstanceId;
    }

    public double getPlannedCost() {
        return this.plannedCost;
    }
}
