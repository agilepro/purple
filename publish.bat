@echo off
rem Builds, signs and uploads a release to Maven Central.
rem Requires the one-time setup described in Publish-Instructions.txt:
rem   a GPG key on the PATH, and a "central" server token in ~/.m2/settings.xml.
rem Bump <version> in purpleLib/pom.xml first -- Central refuses a version
rem that has already been published.
cd purpleLib
call mvn clean deploy -Prelease
pause
