package model;

public class Session {
    private static User loggedUser;
    private static String token;

    public static void login(User user, String jwt) { loggedUser = user; token = jwt; }
    public static void logout() { loggedUser = null; token = null; }
    public static User getLoggedUser() { return loggedUser; }
    public static String getToken() { return token; }
    public static boolean isLoggedIn() { return loggedUser != null; }
    public static boolean isAdmin() { return loggedUser != null && loggedUser.isAdmin(); }
    public static boolean isEmployee() { return loggedUser != null && loggedUser.isEmployee(); }
    public static boolean canManage() { return loggedUser != null && loggedUser.canManage(); }
}