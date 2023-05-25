public class User {
    String login;
    String password;
    int id;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public boolean isAdmin() {
        if (login.equals("admin") && password.equals("admin")) {
            return true;
        }
        return false;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
