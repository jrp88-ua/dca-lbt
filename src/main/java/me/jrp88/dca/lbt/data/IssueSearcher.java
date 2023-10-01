package me.jrp88.dca.lbt.data;

import java.util.Comparator;
import java.util.Scanner;
import java.util.function.Predicate;

public class IssueSearcher {

    private static final Comparator<Issue> SORTER = Comparator.comparingInt(Issue::id);

    private final IssueManager manager;

    public IssueSearcher(IssueManager manager) {
        this.manager = manager;
    }

    public int[] searchById(Scanner in) {
        System.out.print("Id to search by: ");
        String id = in.nextLine();
        return search(issue -> String.valueOf(issue.id()).contains(id));
    }

    public int[] searchByTitle(Scanner in) {
        System.out.print("Title to search by: ");
        String title = in.nextLine();
        return search(issue -> issue.title().contains(title));
    }

    public int[] searchByDescription(Scanner in) {
        System.out.print("Description to search by: ");
        String description = in.nextLine();
        return search(issue -> issue.description().contains(description));
    }

    public int[] searchByReporter(Scanner in) {
        System.out.print("Reporter to search by: ");
        String reporter = in.nextLine();
        return search(issue -> issue.reporterName().contains(reporter));
    }

    public int[] searchByState(Scanner in) {
        System.out.print("State to search by: ");
        String state = in.nextLine();
        return search(issue -> String.valueOf(issue.state()).contains(state));
    }

    public int[] searchByTag(Scanner in) {
        System.out.print("Tag to search by: ");
        String tag = in.nextLine();
        return search(issue -> issue.tags().contains(tag));
    }

    private int[] search(Predicate<Issue> filter) {
        return manager.issues().stream()
                .filter(filter)
                .sorted(SORTER)
                .mapToInt(Issue::id)
                .toArray();
    }

}
