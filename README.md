[![License](https://img.shields.io/badge/license-New%20BSD-blue.svg)](LICENSE) [![Build Status](https://api.travis-ci.org/Praqma/commit-message-parser.svg?branch=master)](https://travis-ci.org/Praqma/commit-message-parser) [![Release](https://jitpack.io/v/Praqma/commit-message-parser.svg)](https://jitpack.io/#Praqma/commit-message-parser)

# Collection of parsers to extract issues from commit messages

## GitHub issues

Parser follows [guidelines outline by GitHub](https://help.github.com/articles/closing-issues-via-commit-messages/)
In addition parser supports to additional key words - reopen and revert. In this case issue reference will have type REVERT
which might be handy to point out that change was reverted

```
    final URL baseUrl = new URL("http://github.com");
    final String baseProject = "user/repo";
    final CommitMessageParser parser = new GitHub(baseUrl, baseProject);
    List<Issue> issues = parser.parse("Fix #123: my super fix");
```
Example logs

```
    Jul 20, 2016 8:20:56 PM net.praqma.utils.parsers.cmg.impl.GitHub parse
    INFO: Found issue 123 with transition type RESOLVE and URL to the issue http://github.com/user/repo/issues/123
```

## Atlassian Jira

Parser follows [guidelines outlined by Atlassian](https://confluence.atlassian.com/fisheye/using-smart-commits-298976812.html) (See workflow section)
Supported key words - close and resolve.
In addition parser supports to additional key words - reopen and revert. In this case issue reference will have type REVERT
which might be handy to point out that change was reverted

```
    final URL baseUrl = new URL("http://jira.com");
    final String baseProject = "myproject";
    final CommitMessageParser parser = new Jira(baseUrl, baseProject);
    List<Issue> issues = parser.parse("ignored text ISSUE-1 ignored text #close");
```

Example logs

```
    Jul 20, 2016 8:20:57 PM net.praqma.utils.parsers.cmg.impl.Jira parse
    INFO: Found issue ISSUE-1 with transition type RESOLVE and URL to the issue http://jira.com/projects/myproject/issues/ISSUE-1
```

## Get the library

Follow [JitPack institutions](https://jitpack.io/#Praqma/commit-message-parser)

## Known usages

* [Tracey version of Eiffel protocol implementation](https://github.com/praqma/tracey-protocol-eiffel)

...

## Definition of Done for the contribution

Your pull request should

* Pass Travis CI check (simply run ./gradlew build locally to test before push)
* Documentation update
* New or updated unit tests

