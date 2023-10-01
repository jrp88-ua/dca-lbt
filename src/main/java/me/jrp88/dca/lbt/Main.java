package me.jrp88.dca.lbt;

import me.jrp88.dca.lbt.data.Issue;
import me.jrp88.dca.lbt.data.IssueComment;
import me.jrp88.dca.lbt.data.User;
import me.jrp88.dca.lbt.util.IOUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        setUpIO();
        var lbt = new LBT();
        if(args.length == 1) {
            Path path = Path.of(args[0]);
            lbt.loadFrom(path);
        }
        lbt.run();
    }

    public static void setUpIO() {
        IOUtil.addHandler(User.class, new IOUtil.IOHandler<>(User.class) {
            @Override
            public void write(DataOutputStream out, User value) throws IOException {
                IOUtil.writeString(out, value.userName());
                IOUtil.writeString(out, value.displayName());
                out.writeBoolean(value.admin());
            }

            @Override
            public User read(DataInputStream in) throws IOException {
                String userName = IOUtil.readString(in);
                String displayName = IOUtil.readString(in);
                boolean admin = in.readBoolean();
                return new User(userName, displayName, admin);
            }
        });
        IOUtil.addHandler(Issue.class, new IOUtil.IOHandler<>(Issue.class) {
            @Override
            public void write(DataOutputStream out, Issue value) throws IOException {
                out.writeInt(value.id());
                IOUtil.writeString(out, value.reporterName());
                IOUtil.writeString(out, value.title());
                IOUtil.writeString(out, value.description());
                IOUtil.write(LocalDateTime.class, out, value.creationTime());
                IOUtil.write(Issue.State.class, out, value.state());
                IOUtil.writeAll(String.class, out, value.tags());
                IOUtil.writeAll(IssueComment.class, out, value.comments());
            }

            @Override
            public Issue read(DataInputStream in) throws IOException {
                int id = in.readInt();
                String reporterName = IOUtil.readString(in);
                String title = IOUtil.readString(in);
                String description = IOUtil.readString(in);
                LocalDateTime creationTime = IOUtil.read(LocalDateTime.class, in);
                Issue.State state = IOUtil.read(Issue.State.class, in);
                List<String> tags = Arrays.asList(IOUtil.readAll(String.class, in));
                List<IssueComment> comments = Arrays.asList(IOUtil.readAll(IssueComment.class, in));
                Issue issue = new Issue(id, reporterName, title, description, creationTime);
                issue.setState(state);
                issue.comments().addAll(comments);
                issue.tags().addAll(tags);
                return issue;
            }
        });
        IOUtil.addHandler(IssueComment.class, new IOUtil.IOHandler<>(IssueComment.class) {
            @Override
            public void write(DataOutputStream out, IssueComment value) throws IOException {
                IOUtil.writeString(out, value.authorName());
                IOUtil.write(LocalDateTime.class, out, value.creationTime());
                IOUtil.writeString(out, value.content());
            }

            @Override
            public IssueComment read(DataInputStream in) throws IOException {
                String authorName = IOUtil.readString(in);
                LocalDateTime creationTime = IOUtil.read(LocalDateTime.class, in);
                String content = IOUtil.readString(in);
                return new IssueComment(authorName, creationTime, content);
            }
        });
        IOUtil.addHandler(String.class, new IOUtil.IOHandler<>(String.class) {
            @Override
            public void write(DataOutputStream out, String value) throws IOException {
                IOUtil.writeString(out, value);
            }

            @Override
            public String read(DataInputStream in) throws IOException {
                return IOUtil.readString(in);
            }
        });
        IOUtil.addHandler(LocalDateTime.class, new IOUtil.IOHandler<>(LocalDateTime.class) {
            @Override
            public void write(DataOutputStream out, LocalDateTime value) throws IOException {
                out.writeInt(value.getYear());
                out.writeInt(value.getMonthValue());
                out.writeInt(value.getDayOfMonth());
                out.writeInt(value.getHour());
                out.writeInt(value.getMinute());
                out.writeInt(value.getSecond());
                out.writeInt(value.getNano());
            }

            @Override
            public LocalDateTime read(DataInputStream in) throws IOException {
                int year = in.readInt();
                int month = in.readInt();
                int day = in.readInt();
                int hour = in.readInt();
                int minute = in.readInt();
                int second = in.readInt();
                int nano = in.readInt();
                return LocalDateTime.of(year, month, day, hour, minute, second, nano);
            }
        });
        IOUtil.addHandler(Issue.State.class, new IOUtil.EnumIOHandler<>(Issue.State.class));
    }

}