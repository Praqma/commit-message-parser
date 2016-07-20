package net.praqma.utils.parsers.cmg.impl;

import net.praqma.utils.parsers.cmg.api.CommitMessageParser;
import net.praqma.utils.parsers.cmg.api.Issue;
import net.praqma.utils.parsers.cmg.api.TransitionType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class GitHubTest {

    @org.junit.Test
    public void testParse() throws MalformedURLException {
        final URL baseUrl = new URL("http://github.com");
        final String baseProject = "user/repo";
        final CommitMessageParser parser = new GitHub(baseUrl, baseProject);
        // magic word fix and ignored text
        List<Issue> issues = parser.parse("Fix #123: my super fix");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        // magic word close and no ignore text
        issues = parser.parse("close #123");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        // magic word close surrounded by ignored text
        issues = parser.parse("ignored text close #123 ignored text");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        // magic word revert surrounded by ignored text
        issues = parser.parse("ignored text revert #123 ignored text");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REVERT, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        // magic word resolve surrounded by ignored text
        issues = parser.parse("ignored text revert #123 ignored text");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REVERT, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        // only issue
        issues = parser.parse("#123");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        // only issue and ignored text
        issues = parser.parse("text #123");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        // incorrect issue
        issues = parser.parse("fix #a123");
        assertEquals(0, issues.size());

        // test multiline
        String multiline = new StringBuilder()
                .append("ignored text\n")
                .append("ignored text resolve #123 ignored text\n")
                .toString();
        issues = parser.parse(multiline);
        assertEquals(1, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        // issue and magic word should be on the same line
        multiline = new StringBuilder()
                .append("ignored text\n")
                .append("ignored text fix\n")
                .append("#123 ignored text\n")
                .toString();
        issues = parser.parse(multiline);
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        // two issues multiline
        multiline = new StringBuilder()
                .append("ignored text\n")
                .append("ignored text #123 ignored text\n")
                .append("ignored text #345 ignored text\n")
                .toString();
        issues = parser.parse(multiline);
        assertEquals(2, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());
        assertEquals(TransitionType.REFERENCE, issues.get(1).getTransition());
        assertEquals("345", issues.get(1).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/345").toString(), issues.get(1).getUrl().toString());

        // two issues and two key words multiline
        multiline = new StringBuilder()
                .append("ignored text\n")
                .append("ignored text resolves #123 ignored text\n")
                .append("ignored text reopen #345 text #revert\n")
                .toString();
        issues = parser.parse(multiline);
        assertEquals(2, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());
        assertEquals(TransitionType.REVERT, issues.get(1).getTransition());
        assertEquals("345", issues.get(1).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/345").toString(), issues.get(1).getUrl().toString());

        // two issues and two keywords one line
        issues = parser.parse("Fix #345 and mention #123 in my super fix");
        assertEquals(2, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("345", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/345").toString(), issues.get(0).getUrl().toString());
        assertEquals(TransitionType.REFERENCE, issues.get(1).getTransition());
        assertEquals("123", issues.get(1).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(1).getUrl().toString());

        // refer other project
        issues = parser.parse("Fix #345 and mention otheruser/repo#123 in my super fix");
        assertEquals(2, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("345", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/345").toString(), issues.get(0).getUrl().toString());
        assertEquals(TransitionType.REFERENCE, issues.get(1).getTransition());
        assertEquals("123", issues.get(1).getIssue());
        assertEquals(new URL(baseUrl, "otheruser/repo/issues/123").toString(), issues.get(1).getUrl().toString());
    }
}