package se.kth.iv1351.university.model;

public class Employee implements EmployeeDTO{
    private int salary;
    private String employment_id;
    private String name;

    public Employee(int salary, String employment_id, String name){
        this.salary = salary;
        this.employment_id = employment_id;
        this.name = name;
    }

    public int getSalary() {
        return this.salary;
    }

    public String getEmploymentId() {
        return this.employment_id;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("name: ");
        string.append(this.name);
        string.append("Employment ID: ");
        string.append(this.employment_id);
        string.append("\t Employee salary: ");
        string.append(this.salary);
        return string.toString();
    }
}
