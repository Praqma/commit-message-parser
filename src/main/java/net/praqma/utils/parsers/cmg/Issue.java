package net.praqma.utils.parsers.cmg;

import java.net.URL;
import java.util.logging.Logger;

public class Issue {
    private static final Logger log = Logger.getLogger(GitHub.class.getName());
    private String issue;
    private TransitionType transition;
    private URL url;

    public Issue(final TransitionType transition, final String issue, final URL url) {
        this.transition = transition;
        this.issue = issue;
        this.url = url;
    }

    public TransitionType getTransition() {
        return transition;
    }

    public String getIssue() {
        return issue;
    }

    public URL getUrl() {
        return url;
    }
}
