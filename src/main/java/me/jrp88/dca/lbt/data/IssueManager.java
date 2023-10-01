package me.jrp88.dca.lbt.data;

import me.jrp88.dca.lbt.LBT;
import me.jrp88.dca.lbt.util.IOUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.function.Predicate;

public class IssueManager {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IssueSearcher searcher = new IssueSearcher(this);
    private final Map<Integer, Issue> issues = new HashMap<>();
    private final LBT lbt;
    private int nextId = 1;

    public IssueManager(LBT lbt) {
        this.lbt = lbt;
    }

    public int createIssue(Scanner in) {
        int id = nextId;
        User reporter = lbt.userManager().loggedUserOrLogin(in);
        LocalDateTime creationTime = LocalDateTime.now();
        String title;
        String description;
        System.out.printf("""
                -- Create a new issue
                Id: %d
                Reporter: %s (%s)
                Creation date: %s
                Title:\s""", id, reporter.userName(), reporter.displayName(), DATE_TIME_FORMATTER.format(creationTime));
        while (true) {
            title = in.nextLine();
            if (isValidTitle(title))
                break;
            System.out.print("""
                    That title is not valid.
                    Title:\s""");
        }
        while (true) {
            System.out.print("Description: ");
            description = in.nextLine();
            if (isValidDescription(description))
                break;
            System.out.println("That description is not valid.");
        }
        nextId++;
        Issue issue = new Issue(id, reporter.userName(), title, description, creationTime);
        issues.put(id, issue);
        return id;
    }

    public void showAllComments(int id) {
        Issue issue = issue(id).orElseThrow();
        issue.comments().stream()
                .sorted(IssueComment::compareTo)
                .map(IssueComment::show)
                .forEach(System.out::println);
    }

    public void addCommentTo(int id, Scanner in) {
        Issue issue = issue(id).orElseThrow();
        User loggedUser = lbt.userManager().loggedUserOrLogin(in);
        System.out.printf("""
                -- Add comment to issue %s
                -- Logged in as %s (%s)
                Comment:\s""", issue.title(), loggedUser.displayName(), loggedUser.userName());
        String comment = in.nextLine();
        if (! isValidComment(comment)) {
            System.out.println("Invalid comment.");
        } else {
            issue.comments().add(new IssueComment(loggedUser.userName(), LocalDateTime.now(), comment));
        }
    }

    public void addTagTo(int id, Scanner in) {
        Issue issue = issue(id).orElseThrow();
        System.out.printf("""
                -- Add tag to issue %s
                -- Use commas (',') to separate each tag
                Tags:\s""", issue.title());
        String tags = in.nextLine();
        if (tags.isBlank()) {
            System.out.println("Invalid tags.");
        } else {
            Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(this::isValidTag)
                    .forEach(issue.tags()::add);
        }
    }

    public void removeTagFrom(int id, Scanner in) {
        Issue issue = issue(id).orElseThrow();
        System.out.printf("""
                -- Add tag to issue %s
                -- Use commas (',') to separate each tag
                -- Can use the tag index or the tag itself%n""", issue.title());
        String[] tags = issue.tags().toArray(String[]::new);
        for (int i = 1; i < tags.length + 1; i++)
            System.out.printf("%d: %s%n", i, tags[i - 1]);
        System.out.print("Tags: ");
        String[] toRemove = in.nextLine().split(",");
        toRemove = Arrays.stream(toRemove)
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .toArray(String[]::new);
        for (String tagOrIndex : toRemove) {
            try {
                int index = Integer.parseInt(tagOrIndex);
                if (index >= 0 && index < tags.length)
                    issue.tags().remove(tags[index]);
            } catch (NumberFormatException e) {
                issue.tags().remove(tagOrIndex);
            }
        }
    }

    public void editTitle(int id, Scanner in) {
        Issue issue = issue(id).orElseThrow();
        System.out.printf("""
                -- Edit title of issue %s
                New title:\s""", issue.title());
        String title = in.nextLine();
        if (! isValidTitle(title)) {
            System.out.println("Invalid title.");
        } else {
            issue.setTitle(title);
        }
    }

    public void editDescription(int id, Scanner in) {
        Issue issue = issue(id).orElseThrow();
        System.out.printf("""
                -- Edit description of issue %s
                New description:\s""", issue.title());
        String description = in.nextLine();
        if (! isValidDescription(description)) {
            System.out.println("Invalid description.");
        } else {
            issue.setDescription(description);
        }
    }

    public void showIssuesSummary(int... issues) {
        if (issues.length == 0)
            issues = issues().stream()
                    .mapToInt(Issue::id)
                    .toArray();
        System.out.printf("-- Issue list: Showing %s issues%n",
                issues.length == this.issues.size() ? "all" : String.valueOf(issues.length));
        Arrays.stream(issues)
                .sorted()
                .mapToObj(this::issue)
                .map(o -> o.map(Issue::resume))
                .map(o -> o.orElse("Unknown issue"))
                .forEach(System.out::println);
    }

    public boolean isValidTitle(String title) {
        return title != null && ! title.isBlank();
    }

    public boolean isValidDescription(String description) {
        return description != null;
    }

    public boolean isValidComment(String comment) {
        return comment != null && ! comment.isBlank();
    }

    public boolean isValidTag(String tag) {
        return tag != null && ! tag.isBlank();
    }

    public void writeOut(DataOutputStream out) throws IOException {
        out.writeInt(nextId);
        IOUtil.writeAll(Issue.class, out, issues.values());
    }

    public void readIn(DataInputStream in) throws IOException {
        nextId = in.readInt();
        Arrays.asList(IOUtil.readAll(Issue.class, in))
                .forEach(issue -> issues.put(issue.id(), issue));
    }

    public Optional<Issue> issue(int id) {
        return Optional.ofNullable(issues.get(id));
    }

    public Collection<Issue> issues() {
        return issues.values();
    }

    public IssueSearcher searcher() {
        return searcher;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof IssueManager that)) return false;

        if (nextId != that.nextId) return false;
        return issues.equals(that.issues);
    }

    @Override
    public int hashCode() {
        int result = issues.hashCode();
        result = 31 * result + nextId;
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", IssueManager.class.getSimpleName() + "[", "]")
                .add("searcher=" + searcher)
                .add("issues=" + issues)
                .add("nextId=" + nextId)
                .toString();
    }

    void addIssue(Issue issue) {
        issues.put(issue.id(), issue);
    }
}