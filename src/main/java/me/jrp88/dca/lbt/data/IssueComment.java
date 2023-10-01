package me.jrp88.dca.lbt.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public class IssueComment implements Comparable<IssueComment> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String authorName;
    private final LocalDateTime creationTime;
    private String content;

    public IssueComment(String authorName, LocalDateTime creationTime, String content) {
        this.authorName = authorName;
        this.creationTime = creationTime;
        this.content = content;
    }

    public String show(UserManager userManager) {
        return """
                %s (%s) commented at %s:
                  %s
                """.formatted(userManager.displayName(authorName), authorName,
                DATE_TIME_FORMATTER.format(creationTime), content);
    }

    @Override
    public int compareTo(IssueComment o) {
        return creationTime.compareTo(o.creationTime);
    }

    public String authorName() {
        return authorName;
    }

    public LocalDateTime creationTime() {
        return creationTime;
    }

    public String content() {
        return content;
    }

    public IssueComment setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof IssueComment that)) return false;

        if (! authorName.equals(that.authorName)) return false;
        if (! creationTime.equals(that.creationTime)) return false;
        return content.equals(that.content);
    }

    @Override
    public int hashCode() {
        int result = authorName.hashCode();
        result = 31 * result + creationTime.hashCode();
        result = 31 * result + content.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", IssueComment.class.getSimpleName() + "[", "]")
                .add("authorName='" + authorName + "'")
                .add("creationTime=" + creationTime)
                .add("content='" + content + "'")
                .toString();
    }
}
