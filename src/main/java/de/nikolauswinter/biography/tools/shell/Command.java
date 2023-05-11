package de.nikolauswinter.biography.tools.shell;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

public class Command {

    private Command() {
        super();
    }

    public static Result run(String command, long timeoutSeconds) throws CommandTimeoutException {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh", "-c", command);
            Process process = builder.start();
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                throw new CommandTimeoutException(timeoutSeconds);
            }
            String stdOut = IOUtils.readLines(process.getInputStream(), StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
            String stdError = IOUtils.readLines(process.getErrorStream(), StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
            return new Result(process.exitValue(), stdOut, stdError);
        } catch (IOException | InterruptedException e) {
            throw new CommandException(e);
        }
    }

    public static class Result {
        private int exitCode;
        private String stdOut;
        private String stdError;

        private Result(int exitCode, String stdOut, String stdError) {
            super();
            this.exitCode = exitCode;
            this.stdOut = stdOut;
            this.stdError = stdError;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getStdOut() {
            return stdOut;
        }

        public String getStdError() {
            return stdError;
        }
    }
}
