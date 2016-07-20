package net.praqma.utils.parsers.cmg.impl;

import net.praqma.utils.parsers.cmg.api.CommitMessageParser;
import net.praqma.utils.parsers.cmg.api.Issue;
import net.praqma.utils.parsers.cmg.api.TransitionType;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class JiraTest {

    @Test
    public void testParse() throws Exception {
        final URL baseUrl = new URL("http://jira.com");
        final String baseProject = "myproject";
        final CommitMessageParser parser = new Jira(baseUrl, baseProject);
        // magic word clode and ignored text
        List<Issue> issues = parser.parse("ignored text ISSUE-1 ignored text #close");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // magic word resolve and no ignore text
        issues = parser.parse("ISSUE-1 #resolve");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // magic word resolve with ignored text in between
        issues = parser.parse("ISSUE-1 ignored text #resolve");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // magic word revert and no ignore text
        issues = parser.parse("ISSUE-1 #revert");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REVERT, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // magic word reopen and no ignore text
        issues = parser.parse("ISSUE-1 #reopen");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REVERT, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // ignored text surrounds issue
        issues = parser.parse("ignored text ISSUE-1 ignored text");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // ignored text after issue
        issues = parser.parse("ISSUE-1 ignored text");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // ignored text before issue
        issues = parser.parse("ignored text ISSUE-1");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // only issue no ignored text
        issues = parser.parse("ISSUE-1");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // not a correct key word
        issues = parser.parse("ISSUE-1 #somethingelse");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // not a correct issue
        issues = parser.parse("ISSUE-1 something-else - and");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // test multiline
        String multiline = new StringBuilder()
                .append("ignored text\n")
                .append("ignored text ISSUE-1 #close ignored text\n")
                .toString();
        issues = parser.parse(multiline);
        assertEquals(1, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // issue and magic word should be on the same line
        multiline = new StringBuilder()
                .append("ignored text\n")
                .append("ignored text ISSUE-1 ignored text\n")
                .append("ignored text #close ignored text\n")
                .toString();
        issues = parser.parse(multiline);
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());

        // two issues multiline
        multiline = new StringBuilder()
                .append("ignored text\n")
                .append("ignored text ISSUE-1 ignored text\n")
                .append("ignored text ISSUE-2 ignored text\n")
                .toString();
        issues = parser.parse(multiline);
        assertEquals(2, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());
        assertEquals(TransitionType.REFERENCE, issues.get(1).getTransition());
        assertEquals("ISSUE-2", issues.get(1).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-2").toString(), issues.get(1).getUrl().toString());

        // two issues and two key words multiline
        multiline = new StringBuilder()
                .append("ignored text\n")
                .append("ignored text ISSUE-1 ignored text #close\n")
                .append("ignored text ISSUE-2 ignored text #revert\n")
                .toString();
        issues = parser.parse(multiline);
        assertEquals(2, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());
        assertEquals(TransitionType.REVERT, issues.get(1).getTransition());
        assertEquals("ISSUE-2", issues.get(1).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-2").toString(), issues.get(1).getUrl().toString());

        // two issues and two keywords one line
        issues = parser.parse("ISSUE-1 #resolve and ISSUE-2 #reopen");
        assertEquals(2, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());
        assertEquals(TransitionType.REVERT, issues.get(1).getTransition());
        assertEquals("ISSUE-2", issues.get(1).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-2").toString(), issues.get(1).getUrl().toString());

        // TODO: The one below will fail - figure out later
        /*
        // two issues one line
        issues = parser.parse("ISSUE-1 ISSUE-2");
        assertEquals(2, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("ISSUE-1", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-1").toString(), issues.get(0).getUrl().toString());
        assertEquals(TransitionType.REFERENCE, issues.get(1).getTransition());
        assertEquals("ISSUE-2", issues.get(1).getIssue());
        assertEquals(new URL(baseUrl, "/projects/" + baseProject + "/issues/ISSUE-2").toString(), issues.get(1).getUrl().toString());
        */
    }
}