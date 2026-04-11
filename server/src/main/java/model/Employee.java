package model;

public class Employee extends User {
    public Employee() {}
    public Employee(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash);
    }

    @Override public String  getRole() { return "EMPLOYEE"; }
    @Override public boolean canLogin() { return true; }
    @Override public boolean canManage() { return true; }
}