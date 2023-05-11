package de.nikolauswinter.biography.tools.shell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import de.nikolauswinter.biography.tools.shell.Command.Result;

class CommandTest {

    @Test
    void test() {
        Result result = Command.run("echo Hello", 5);

        assertThat(result).isNotNull();
        assertThat(result.getExitCode()).isEqualTo(0);
        assertThat(result.getStdOut()).isEqualTo("Hello");
        assertThat(result.getStdError()).isEmpty();
    }

    @Test()
    void testTimeout() {
        assertThrows(CommandTimeoutException.class, () -> {
            Command.run("sleep 10", 2);
        });
    }

    @Test
    void testFailure() {
        Result result = Command.run("sleep", 5);

        assertThat(result).isNotNull();
        assertThat(result.getExitCode()).isNotEqualTo(0);
        assertThat(result.getStdOut()).isEmpty();
        assertThat(result.getStdError()).isNotEmpty();
    }

}
