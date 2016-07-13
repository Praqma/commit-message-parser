package net.praqma.utils.parsers.cmg;

import java.net.MalformedURLException;
import java.util.List;

public interface CommitMessageParser {

    public List<Issue> parse(final String message) throws MalformedURLException;
}
