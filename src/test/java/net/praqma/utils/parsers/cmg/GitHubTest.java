package net.praqma.utils.parsers.cmg;

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
        List<Issue> issues = parser.parse("Fix #123: my super fix");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        issues = parser.parse("mention #123 in my super fix");
        assertEquals(1, issues.size());
        assertEquals(TransitionType.REFERENCE, issues.get(0).getTransition());
        assertEquals("123", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(0).getUrl().toString());

        issues = parser.parse("Fix #345 and mention #123 in my super fix");
        assertEquals(2, issues.size());
        assertEquals(TransitionType.RESOLVE, issues.get(0).getTransition());
        assertEquals("345", issues.get(0).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/345").toString(), issues.get(0).getUrl().toString());
        assertEquals(TransitionType.REFERENCE, issues.get(1).getTransition());
        assertEquals("123", issues.get(1).getIssue());
        assertEquals(new URL(baseUrl, baseProject + "/issues/123").toString(), issues.get(1).getUrl().toString());

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