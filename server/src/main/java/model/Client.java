package model;

public class Client extends User {
    public Client() {}
    public Client(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash);
    }

    @Override public String  getRole() { return "CLIENT"; }
    @Override public boolean canLogin() { return true; }
}