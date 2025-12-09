package se.kth.iv1351.university.model;

public class Employee implements EmployeeDTO{
    private int salary;
    private String employment_id;

    public Employee(int salary, String employment_id){
        this.salary = salary;
        this.employment_id = employment_id;
    }

    public int getSalary() {
        return this.salary;
    }

    public String getEmploymentId() {
        return this.employment_id;
    }
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("Employment ID: ");
        string.append(employment_id);
        string.append("\t Employee salary: ");
        string.append(salary);
        return string.toString();
    }
}
