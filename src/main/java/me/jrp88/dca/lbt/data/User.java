package me.jrp88.dca.lbt.data;

import java.util.StringJoiner;

public class User {

    private String displayName;
    private String userName;
    private boolean admin;

    public User(String userName, String displayName, boolean admin) {
        this.userName = userName;
        this.displayName = displayName;
        this.admin = admin;
    }

    public String displayName() {
        return displayName;
    }

    public User setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String userName() {
        return userName;
    }

    public User setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public boolean admin() {
        return admin;
    }

    public User setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof User user)) return false;

        if (admin != user.admin) return false;
        if (! displayName.equals(user.displayName)) return false;
        return userName.equals(user.userName);
    }

    @Override
    public int hashCode() {
        int result = displayName.hashCode();
        result = 31 * result + userName.hashCode();
        result = 31 * result + (admin ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                .add("displayName='" + displayName + "'")
                .add("userName='" + userName + "'")
                .add("admin=" + admin)
                .toString();
    }
}
