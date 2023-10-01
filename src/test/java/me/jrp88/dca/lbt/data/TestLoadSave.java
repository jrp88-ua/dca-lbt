package me.jrp88.dca.lbt.data;

import me.jrp88.dca.lbt.LBT;
import me.jrp88.dca.lbt.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TestLoadSave {

    @BeforeAll
    public static void setUp() {
        Main.setUpIO();
    }

    @Test
    public void userManagerReadAfterWriteGeneratesTheSame() throws IOException {
        LBT original = new LBT();
        addUsers(original);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        original.userManager().writeOut(out);

        byte[] serialized = baos.toByteArray();

        LBT deserialized = new LBT();
        ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
        DataInputStream in = new DataInputStream(bais);
        deserialized.userManager().readIn(in);

        Assertions.assertEquals(original.userManager(), deserialized.userManager());

        bais.close();
        baos.close();
    }

    @Test
    public void issueManagerReadAfterWriteGeneratesTheSame() throws IOException {
        LBT original = new LBT();
        addIssues(original);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        original.issueManager().writeOut(out);

        byte[] serialized = baos.toByteArray();

        LBT deserialized = new LBT();
        ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
        DataInputStream in = new DataInputStream(bais);
        deserialized.issueManager().readIn(in);

        Assertions.assertEquals(original.issueManager(), deserialized.issueManager());

        bais.close();
        baos.close();
    }

    @Test
    public void allReadAfterWriteGeneratesTheSame() throws IOException {
        LBT original = new LBT();
        addUsers(original);
        addIssues(original);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        original.writeTo(out);

        byte[] serialized = baos.toByteArray();

        LBT deserialized = new LBT();
        ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
        DataInputStream in = new DataInputStream(bais);
        deserialized.readIn(in);

        Assertions.assertEquals(original.issueManager(), deserialized.issueManager());

        bais.close();
        baos.close();
    }

    private void addUsers(LBT lbt) {
        lbt.userManager().addUser(new User("Ally", "Allyta", true));
        lbt.userManager().addUser(new User("Juan", "JuanElLoko", false));
        lbt.userManager().addUser(new User("Pedro", "PedroELP", false));
        lbt.userManager().addUser(new User("Alberto", "Alberto no gana", false));
        lbt.userManager().addUser(new User("Miguel", "Unamuno", false));
        lbt.userManager().addUser(new User("Laura", "Laurii ÙwÚ", false));
    }

    private void addIssues(LBT lbt) {
        lbt.issueManager().addIssue(createIssue(
                1,
                "Juan",
                "No va",
                "Por algun motivo el deserializado no va o algo",
                LocalDateTime.now(),
                Issue.State.CLOSED,
                Arrays.asList(
                        "bug", "need info", "need help"
                ),
                Arrays.asList(
                        new IssueComment("Ally", LocalDateTime.now(), "¿Se puede proporcionar más información?"),
                        new IssueComment("Juan", LocalDateTime.now(), "Al parecer no estabamos serializando y deserializando los nanos de los LocalDateTime, además serializabamos con longs pero deserializabamos con ints.")
                )
        ));
    }

    private Issue createIssue(int id, String reporterName, String title, String description, LocalDateTime creationTime, Issue.State state, List<String> tags, List<IssueComment> comments) {
        Issue issue = new Issue(id, reporterName, title, description, creationTime);
        issue.setState(state);
        issue.comments().addAll(comments);
        issue.tags().addAll(tags);
        return issue;

    }

}
