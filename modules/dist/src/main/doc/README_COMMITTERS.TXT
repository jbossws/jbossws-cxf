The docbook xml files are automatically exported from https://docs.jboss.org/author/display/JBWS .
You select "Tools" -> "JBoss DocBook Export", then enable "Single book output" and export.
The generated files should differ from the SCM version of doc only by the changes applied online plus few minor tunings that needs to be re-applied manually to fix page titles starting with "*-" (eg. JAX-WS, WS-Addressing, WS-*, ...).
Finally a new entry in the revision docbook file should be added at each release.
Thanks
