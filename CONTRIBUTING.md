Contributing Guide
==================================

We welcome contributions from the community. This guide will walk you through the steps for getting started on our project.

- [Git Setup](#git-setup)
- [Issues](#issues)
  - [Good First Issues](#good-first-issues)
- [Code Style](#code-style)



## Git Setup
To contribute, you first need to fork the [jbossws-cxf](https://github.com/jbossws/jbossws-cxf) repository.
then clone your newly forked copy onto your local workspace.
And make sure you have set up your Git authorship correctly:
```
git config --global user.name "Your Full Name"
git config --global user.email your.email@example.com
```
To pull update from the jbossws-cxf upstream, the jbossws-cxf remote can be added to your local git repo :
```
git remote add upstream https://github.com/jbossws/jbossws-cxf.git
```
Before change/fix, always pull update from jbossws-cxf:
```
$ git checkout -f main
$ git pull --rebase upstream main
```

## Issues
JBssWS project uses JIRA to manage issues. All issues can be found [here](https://issues.redhat.com/projects/JBWS/issues).

To create a new issue, comment on an existing issue, or assign an issue to yourself, you'll need to first [create a JIRA account](https://issues.redhat.com/).


### Good First Issues

We added the project issues with `good-first-issue` label for good to start the contribution. These can be found [here](https://issues.redhat.com/issues/?filter=12406834).
When you start the issue you'd like to contribute, please make sure it isn't assigned to someone else. To assign the selected issue to yourself, click the "Assign" and check the
assignee name before finish.
It is highly recommended creating a topic git branch for one JIRA issue. A commit message with JIRA number is always a good 
practice to track which commit resolve which JIRA issue. Later the commit or PR number will be automatically linked to JIRA system.
After finish the JIRA issue task, creating a GitHub pull request based on main branch. We'll monitor the PR queus and 
get the PR reviewed and merged. 
If you encounter any problem during the contribution, please use [GitHub discussion](https://github.com/jbossws/jbossws-cxf/discussions) to let us know.

## Code Style
If you are using Eclipse then set your code style to eclipse/jboss-style.xml and check style to eclipse/jboss-format.xml.
And please make sure you're putting correct license headers to Java and XML files.
