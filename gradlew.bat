@rem Gradle wrapper for Windows
@if "%DEBUG%"=="" @echo off
setlocal
set CLASSPATH=%~dp0gradle\wrapper\gradle-wrapper.jar
"%JAVA_HOME%\bin\java.exe" "-Xmx64m" "-Xms64m" "-Dorg.gradle.appname=gradlew" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
