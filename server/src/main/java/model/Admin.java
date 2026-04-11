package model;

public class Admin extends User {
    public Admin() {}
    public Admin(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash);
    }

    @Override public String  getRole() { return "ADMIN"; }
    @Override public boolean canLogin() { return true; }
    @Override public boolean canManage() { return true; }
    @Override public boolean isAdmin() { return true; }
}