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

// Based on rules described here https://help.github.com/articles/closing-issues-via-commit-messages/
public class GitHub implements CommitMessageParser {
    private static final Logger log = Logger.getLogger(GitHub.class.getName());
    private static final Pattern issuePattern = Pattern.compile("(?<magicWord>[A-Za-z]+)?(?:[\\s+&&[^\\n]])*?(?<otherProject>[A-Za-z0-9\\_\\-]*/?[A-Za-z0-9\\_\\-]*)?(?<issue>(#{1}[0-9]+))", Pattern.MULTILINE);
    private static final List<String> closingMagicWords = Arrays.asList("close", "closes", "closed", "fix", "fixes", "fixed", "resolve", "resolves", "resolved");
    private static final List<String> revertingMagicWords = Arrays.asList("revert", "reopen");
    private URL baseUrl = null;
    private String baseProject = null;

    // Base URL is url to GitHub instance (public or enterprise)
    // Base project is a project name user/repo
    public GitHub (final URL baseUrl, final String baseProject) {
        this.baseUrl = baseUrl;
        this.baseProject = baseProject;
    }

    public List<Issue> parse(final String message) throws MalformedURLException {
        final List<Issue> issues = new ArrayList<>();
        final Matcher matcher =  issuePattern.matcher(message);
        log.fine("Parsing message:\n" + message);
        while (matcher.find()) {
            // We expect 3 groups to be found
            // #1 Contains magic word
            // #2 Contains full issue number including another repo (ex. user/repo#issue) if present
            // #3 Contains another repo name if present
            log.fine("Match! Found the following:");
            for (int groupNumber = 0; groupNumber < matcher.groupCount(); groupNumber += 1) {
                log.fine("group " + groupNumber + ": " + matcher.group(groupNumber));
            }
            TransitionType type = TransitionType.REFERENCE;
            if (matcher.group("magicWord") != null && ! matcher.group("magicWord").isEmpty()) {
                final String magicWord = matcher.group("magicWord");
                if (closingMagicWords.contains(magicWord.toLowerCase())) {
                    type = TransitionType.RESOLVE;
                }
                if (revertingMagicWords.contains(magicWord.toLowerCase())) {
                    type = TransitionType.REVERT;
                }
            }
            final String issueNumber = matcher.group("issue").split("#")[1];
            URL url = null;
            if (matcher.group("otherProject").toString() != null && ! matcher.group("otherProject").toString().isEmpty()) {
                url = new URL(baseUrl, matcher.group("otherProject").toString() + "/issues/" + issueNumber);
            } else {
                url = new URL(baseUrl, baseProject + "/issues/" + issueNumber);
            }
            final Issue issue = new Issue(type, issueNumber, url);
            log.fine("Found issue " + issue.getIssue() + " with transition type " + issue.getTransition() + " and URL to the issue " + issue.getUrl().toString());
            issues.add(issue);
        }
        return issues;
    }
}
