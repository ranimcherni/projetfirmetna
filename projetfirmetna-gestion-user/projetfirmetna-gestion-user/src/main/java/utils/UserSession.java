package utils;

/**
 * Singleton class to manage the current user session across the application.
 */
public class UserSession {
    private static UserSession instance;
    private models.User user;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public models.User getUser() {
        return user;
    }

    public String getUserName() {
        return (user != null) ? user.getPrenom() : null;
    }

    public void setUser(models.User user) {
        this.user = user;
    }

    public void cleanUserSession() {
        user = null;
    }

    public boolean isAdmin() {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
