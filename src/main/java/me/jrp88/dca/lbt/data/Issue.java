package me.jrp88.dca.lbt.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class Issue {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public enum State {
        OPEN, CLOSED

    }

    private final int id;

    private final String reporterName;

    private String title;
    private String description;
    private LocalDateTime creationTime;
    private State state = State.OPEN;
    private final List<IssueComment> comments = new ArrayList<>();
    private final Set<String> tags = new HashSet<>();

    public Issue(int id, String reporterName, String title, String description, LocalDateTime creationTime) {
        this.id = id;
        this.reporterName = reporterName;
        this.title = title;
        this.description = description;
        this.creationTime = creationTime;
    }

    public Issue(int id, String reporterName, String title, String description) {
        this(id, reporterName, title, description, LocalDateTime.now());
    }

    public void toggleState() {
        setState(state() == State.OPEN ? State.CLOSED : State.OPEN);
    }

    public String resume(UserManager userManager) {
        return """
                #%d: %s (%s)
                \s\sReported by %s (%s) at %s
                \s\s%s""".formatted(id, title, state.name(),
                userManager.displayName(reporterName), reporterName,
                DATE_TIME_FORMATTER.format(creationTime), String.join(", ", tags));
    }

    public Issue open() {
        return setState(State.OPEN);
    }

    public Issue close() {
        return setState(State.CLOSED);
    }

    public int id() {
        return id;
    }

    public String reporterName() {
        return reporterName;
    }

    public String title() {
        return title;
    }

    public Issue setTitle(String title) {
        this.title = title;
        return this;
    }

    public String description() {
        return description;
    }

    public Issue setDescription(String description) {
        this.description = description;
        return this;
    }

    public LocalDateTime creationTime() {
        return creationTime;
    }

    public Issue setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public State state() {
        return state;
    }

    public Issue setState(State state) {
        this.state = state;
        return this;
    }

    public List<IssueComment> comments() {
        return comments;
    }

    public Set<String> tags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof Issue issue)) return false;

        if (id != issue.id) return false;
        if (! reporterName.equals(issue.reporterName)) return false;
        if (! title.equals(issue.title)) return false;
        if (! description.equals(issue.description)) return false;
        if (! creationTime.equals(issue.creationTime)) return false;
        if (state != issue.state) return false;
        if (! comments.equals(issue.comments)) return false;
        return tags.equals(issue.tags);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Issue.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("reporterName='" + reporterName + "'")
                .add("title='" + title + "'")
                .add("description='" + description + "'")
                .add("creationTime=" + creationTime)
                .add("state=" + state)
                .add("comments=" + comments)
                .add("tags=" + tags)
                .toString();
    }
}
