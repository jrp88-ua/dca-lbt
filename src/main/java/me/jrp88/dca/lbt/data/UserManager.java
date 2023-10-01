package me.jrp88.dca.lbt.data;

import me.jrp88.dca.lbt.LBT;
import me.jrp88.dca.lbt.util.IOUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.StringJoiner;

public class UserManager {

    private final Map<String, User> users = new HashMap<>();
    private final LBT lbt;
    private String loggedUser;

    public UserManager(LBT lbt) {
        this.lbt = lbt;
    }

    public boolean isUserNameAvailable(String userName) {
        return !users.containsKey(userName);
    }

    public boolean isValidUserName(String userName) {
        return userName != null && ! userName.isBlank();
    }

    public void showAllUsers() {
        System.out.println("-- All users");
        int i = 0;
        for (User user : users.values()
                .stream()
                .sorted(Comparator.comparing(User::userName))
                .toList()) {
            System.out.printf("%d. %s (%s) %s %s%n",
                    ++ i,
                    user.displayName(),
                    user.userName(),
                    user.admin() ? "[ADMIN]" : "",
                    user.userName().equals(loggedUser) ? "(CURRENT)" : "");
        }
    }

    public User login(Scanner in) {
        if (users.isEmpty()) {
            loggedUser = createNewUser(in);
            User user = users.get(loggedUser);
            System.out.printf("Logged in as %s (%s).%n", user.displayName(), user.userName());
            return user;
        }
        loggedUser = null;
        while (true) {
            System.out.print("User name: ");
            String userName = in.nextLine();
            if (! users.containsKey(userName))
                System.out.println("No user with that user name.");
            else {
                loggedUser = userName;
                User user = users.get(loggedUser);
                System.out.printf("Logged in as %s (%s).%n", user.displayName(), user.userName());
                return user;
            }
        }
    }

    public User loggedUserOrLogin(Scanner in) {
        return loggedUser().orElseGet(() -> login(in));
    }

    public String createNewUser(Scanner in) {
        String userName;
        String displayName;
        boolean admin;
        System.out.println("-- Create a new user");
        while (true) {
            System.out.print("User name: ");
            userName = in.nextLine();
            if (! isValidUserName(userName))
                System.out.println("That is not a valid user name.");
            else if (! isUserNameAvailable(userName))
                System.out.println("That user name is already taken.");
            else break;
        }
        System.out.print("Display name: ");
        displayName = in.nextLine();
        System.out.print("Is admin [N]? ");
        admin = in.nextLine().equals("Y");
        users.put(userName, new User(userName, displayName, admin));
        return userName;
    }

    public Optional<User> loggedUser() {
        if (loggedUser == null)
            return Optional.empty();
        return user(loggedUser);
    }

    public Optional<User> user(String userName) {
        return Optional.ofNullable(users.get(userName));
    }

    public String displayName(String userName) {
        return user(userName).map(User::displayName).orElse("Unknown");
    }

    public void writeOut(DataOutputStream out) throws IOException {
        IOUtil.writeAll(User.class, out, users.values());
    }

    public void readIn(DataInputStream in) throws IOException {
        loggedUser = null;
        users.clear();
        Arrays.asList(IOUtil.readAll(User.class, in))
                .forEach(user -> users.put(user.userName(), user));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof UserManager that)) return false;

        if (! users.equals(that.users)) return false;
        return Objects.equals(loggedUser, that.loggedUser);
    }

    @Override
    public int hashCode() {
        int result = users.hashCode();
        result = 31 * result + (loggedUser != null ? loggedUser.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserManager.class.getSimpleName() + "[", "]")
                .add("users=" + users)
                .add("loggedUser='" + loggedUser + "'")
                .toString();
    }

    void addUser(User user) {
        users.put(user.userName(), user);
    }

    void setLoggedUser(String user) {
        loggedUser = user;
    }
}
