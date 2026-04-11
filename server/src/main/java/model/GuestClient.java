package model;

public class GuestClient extends User {
    public GuestClient() {}

    public GuestClient(int id) {
        super(id, "Gość" + id, null, null);
    }

    public GuestClient(int id, String email) {
        super(id, "Gość" + id, email, null);
    }

    @Override public String getRole() { return "GUEST"; }
    @Override public boolean canLogin() { return false; }
}