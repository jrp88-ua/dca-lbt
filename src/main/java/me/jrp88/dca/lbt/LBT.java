package me.jrp88.dca.lbt;

import me.jrp88.dca.lbt.data.Issue;
import me.jrp88.dca.lbt.data.IssueManager;
import me.jrp88.dca.lbt.data.UserManager;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.StringJoiner;

public class LBT implements Runnable {

    private final Scanner in;
    private final UserManager userManager;
    private final IssueManager issueManager;

    public LBT() {
        in = new Scanner(System.in);
        userManager = new UserManager(this);
        issueManager = new IssueManager(this);
    }

    public void runMainMenu(Scanner in) {
        while (true) {
            showMainMenu();
            String option = in.nextLine();
            if (option.isBlank() || option.length() != 1
                || option.charAt(0) < '1' || option.charAt(0) > '6') {
                System.out.println("-- Closing app");
                break;
            }
            switch (option.charAt(0)) {
                case '1' -> userManager.createNewUser(in);
                case '2' -> userManager.login(in);
                case '3' -> runIssuesMenu(in);
                case '4' -> userManager.showAllUsers();
                case '5' -> saveState();
                case '6' -> loadState();
                default -> throw new IllegalStateException("Unexpected value: " + option.charAt(0));
            }
        }
    }

    private void showMainMenu() {
        var loggedUser = userManager.loggedUser();
        System.out.println("-- Main menu");
        System.out.print("-- ");
        loggedUser.ifPresentOrElse(
                user -> System.out.printf("Logged in as %s (%s).%n", user.displayName(), user.userName()),
                () -> System.out.println("No user in session.")
        );
        System.out.print("""
                1: Create new user
                2: Log in into an user
                3: See issue menu
                4: Show all users
                5: Save state
                6: Load state
                Any other key: Close app
                Option:\s""");
    }

    public void runIssuesMenu(Scanner in) {
        while (true) {
            showIssuesMenu();
            String option = in.nextLine();
            if (option.isBlank() || option.length() != 1
                || option.charAt(0) < '1' || option.charAt(0) > '4') {
                System.out.println("-- Going back to main menu");
                break;
            }
            switch (option.charAt(0)) {
                case '1' -> issueManager.createIssue(in);
                case '2' -> runSearchIssueMenu(in);
                case '3' -> runIssueMenu(in);
                case '4' -> issueManager.showIssuesSummary();
                default -> throw new IllegalStateException("Unexpected value: " + option.charAt(0));
            }
        }
    }

    private void showIssuesMenu() {
        var loggedUser = userManager.loggedUser();
        System.out.print("-- Issues menu\n-- ");
        loggedUser.ifPresentOrElse(
                user -> System.out.printf("Logged in as %s (%s).%n", user.displayName(), user.userName()),
                () -> System.out.println("No user in session.")
        );
        System.out.print("""
                1: Create issue
                2: Search issue
                3: Go into issue
                4: Show all issues
                Any other key: Go back to main menu
                Option:\s""");
    }

    public void runSearchIssueMenu(Scanner in) {
        while (true) {
            showSearchIssueMenu();
            String option = in.nextLine();
            if (option.isBlank() || option.length() != 1
                || option.charAt(0) < '1' || option.charAt(0) > '6') {
                System.out.println("-- Going back to issues menu");
                break;
            }
            issueManager.showIssuesSummary(switch (option.charAt(0)) {
                case '1' -> issueManager.searcher().searchById(in);
                case '2' -> issueManager.searcher().searchByTitle(in);
                case '3' -> issueManager.searcher().searchByDescription(in);
                case '4' -> issueManager.searcher().searchByReporter(in);
                case '5' -> issueManager.searcher().searchByState(in);
                case '6' -> issueManager.searcher().searchByTag(in);
                default -> throw new IllegalStateException("Unexpected value: " + option.charAt(0));
            });
        }
    }

    private void showSearchIssueMenu() {
        var loggedUser = userManager.loggedUser();
        System.out.print("-- Search issues menu\n-- ");
        loggedUser.ifPresentOrElse(
                user -> System.out.printf("Logged in as %s (%s).%n", user.displayName(), user.userName()),
                () -> System.out.println("No user in session.")
        );
        System.out.print("""
                1: Search by id
                2: Search by title
                3: Search by description
                4: Search by reporter
                5: Search by state
                6: Search by tag
                Any other key: Go back to issues menu
                Option:\s""");
    }

    public void runIssueMenu(Scanner in) {
        Issue issue = null;
        while (issue == null) {
            System.out.print("Issue id: ");
            String sid = in.nextLine();
            int id;
            try {
                id = Integer.parseInt(sid);
            } catch (NumberFormatException e) {
                System.out.printf("%s is not a valid id.%n", sid);
                continue;
            }
            var oI = issueManager.issue(id);
            if (oI.isEmpty()) {
                System.out.println("No issue with the given id.");
                continue;
            }
            issue = oI.get();
        }
        while (true) {
            showIssueMenu(issue);
            String option = in.nextLine();
            if (option.isBlank() || option.length() != 1
                || option.charAt(0) < '1' || option.charAt(0) > '7') {
                System.out.println("-- Going back to issues menu");
                break;
            }
            switch (option.charAt(0)) {
                case '1' -> issueManager.showAllComments(issue.id());
                case '2' -> issueManager.addCommentTo(issue.id(), in);
                case '3', '4', '5', '6', '7' -> {
                    Issue finalIssue = issue;
                    boolean hasPermissionsOnIssue = userManager.loggedUser()
                            .map(u -> u.admin() || u.userName().equals(finalIssue.reporterName()))
                            .orElse(false);
                    if (! hasPermissionsOnIssue) {
                        System.out.println("Not enough permissions to do that.");
                        continue;
                    }
                    switch (option.charAt(0)) {
                        case '3' -> issue.toggleState();
                        case '4' -> issueManager.addTagTo(issue.id(), in);
                        case '5' -> issueManager.removeTagFrom(issue.id(), in);
                        case '6' -> issueManager.editTitle(issue.id(), in);
                        case '7' -> issueManager.editDescription(issue.id(), in);
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + option.charAt(0));
            }
        }
    }

    private void showIssueMenu(Issue issue) {
        var loggedUser = userManager.loggedUser();
        System.out.printf("-- Issue menu: #%d %s (%s)%n  Tags: %s%n-- ",
                issue.id(), issue.title(), issue.state().name(), String.join(", ", issue.tags()));
        loggedUser.ifPresentOrElse(
                user -> System.out.printf("Logged in as %s (%s).%n", user.displayName(), user.userName()),
                () -> System.out.println("No user in session.")
        );
        System.out.println("""
                1: See comments
                2: Add comment""");
        if (loggedUser.isPresent()
            && (issue.reporterName().equals(loggedUser.get().userName())
                || loggedUser.get().admin())) {
            System.out.println("3: "
                               + (issue.state() == Issue.State.OPEN ? "Close" : "Open")
                               + " issue"
            );
            System.out.print("""
                    4: Add tag
                    5: Remove tag
                    6: Edit title
                    7: Edit description
                    Any other key: Go back to issues menu
                    Option:\s""");
        }
    }

    public void saveState() {
        try {
            EventQueue.invokeAndWait(() -> {
                System.out.println("-- Saving app state");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Data file (*.dat)", "dat");
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(filter);

                int option = chooser.showSaveDialog(null);
                if (option != JFileChooser.APPROVE_OPTION) {
                    System.out.println("-- Cancelled saving state of app");
                    return;
                }
                File file = chooser.getSelectedFile();
                if (! file.getAbsolutePath().endsWith(".dat"))
                    file = new File(file.getAbsolutePath() + ".dat");
                Path path = file.toPath();
                try {
                    if (! Files.exists(path))
                        Files.createFile(path);
                    try (var out = new DataOutputStream(Files.newOutputStream(path))) {
                        writeTo(out);
                    }
                } catch (IOException e) {
                    System.out.println("-- Could not save app state: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.out.println("-- Could not save app state: " + e.getMessage());
        }
    }

    public void loadState() {
        try {
            EventQueue.invokeAndWait(() -> {
                System.out.println("-- Loading app state");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Data file (*.dat)", "dat");
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(filter);
                int option = chooser.showOpenDialog(null);
                if (option != JFileChooser.APPROVE_OPTION) {
                    System.out.println("-- Cancelled loading state of app");
                    return;
                }
                File file = chooser.getSelectedFile();
                if (! file.getAbsolutePath().endsWith(".dat"))
                    file = new File(file.getAbsolutePath() + ".dat");
                loadFrom(file.toPath());
            });
        } catch (Exception e) {
            System.out.println("-- Could not load app state: " + e.getMessage());
        }
    }

    void loadFrom(Path path) {
        try (var in = new DataInputStream(Files.newInputStream(path))) {
            readIn(in);
        } catch (IOException e) {
            System.out.println("-- Could not load app state: " + e.getMessage());
        }
    }

    public void writeTo(DataOutputStream out) throws IOException {
        userManager.writeOut(out);
        issueManager.writeOut(out);
    }

    public void readIn(DataInputStream in) throws IOException {
        userManager.readIn(in);
        issueManager.readIn(in);
    }

    public IssueManager issueManager() {
        return issueManager;
    }

    public UserManager userManager() {
        return userManager;
    }

    @Override
    public void run() {
        runMainMenu(in);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof LBT lbt)) return false;

        if (! userManager.equals(lbt.userManager)) return false;
        return issueManager.equals(lbt.issueManager);
    }

    @Override
    public int hashCode() {
        int result = userManager.hashCode();
        result = 31 * result + issueManager.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LBT.class.getSimpleName() + "[", "]")
                .add("userManager=" + userManager)
                .add("issueManager=" + issueManager)
                .toString();
    }
}
