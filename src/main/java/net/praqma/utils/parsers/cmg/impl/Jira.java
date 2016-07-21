package net.praqma.utils.parsers.cmg.impl;

import net.praqma.utils.parsers.cmg.api.Issue;
import net.praqma.utils.parsers.cmg.api.TransitionType;
import net.praqma.utils.parsers.cmg.api.CommitMessageParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Based on rules described here https://confluence.atlassian.com/fisheye/using-smart-commits-298976812.html
public class Jira implements CommitMessageParser {
    private static final Logger log = Logger.getLogger(Jira.class.getName());
    private static final Pattern issuePattern = Pattern.compile("([A-Za-z0-9]+\\-[0-9]+)[^#\\n\\-]*(#[a-z]+)?", Pattern.MULTILINE);
    private static final List<String> closingMagicWords = Arrays.asList("close", "resolve");
    private static final List<String> revertingMagicWords = Arrays.asList("revert", "reopen");
    private URL baseUrl = null;
    private String baseProject = null;

    // Base URL is url to Jira instance (public cloud or enterprise)
    // Base project is a project name
    public Jira(final URL baseUrl, final String baseProject) {
        this.baseUrl = baseUrl;
        this.baseProject = baseProject;
    }

    public List<Issue> parse(final String message) throws MalformedURLException {
        final List<Issue> issues = new ArrayList<>();
        final Matcher matcher =  issuePattern.matcher(message);
        log.fine("Parsing message:\n" + message);
        while (matcher.find()) {
            // We expect 2 groups to be found
            // #1 issue number
            // #2 contains the magic word
            log.fine("Match! Found the following:");
            for (int groupNumber = 0; groupNumber < matcher.groupCount(); groupNumber += 1) {
                log.fine("group " + groupNumber + ": " + matcher.group(groupNumber));
            }
            TransitionType type = TransitionType.REFERENCE;
            if (matcher.group(2) != null && ! matcher.group(2).isEmpty()) {
                final String magicWord = matcher.group(2).split("#")[1];
                if (closingMagicWords.contains(magicWord)) {
                    type = TransitionType.RESOLVE;
                }
                if (revertingMagicWords.contains(magicWord)) {
                    type = TransitionType.REVERT;
                }
            }
            final String issueNumber = matcher.group(1).toString();
            URL url = new URL(baseUrl, "/projects/" + baseProject + "/issues/" + issueNumber);
            final Issue issue = new Issue(type, issueNumber, url);
            log.fine("Found issue " + issue.getIssue() + " with transition type " + issue.getTransition() + " and URL to the issue " + issue.getUrl().toString());
            issues.add(issue);
        }
        return issues;
    }
}
