package de.nikolauswinter.biography.tools.thumbnails;

import java.io.File;
import java.text.MessageFormat;
import java.util.Objects;

import de.nikolauswinter.biography.tools.shell.Command;
import de.nikolauswinter.biography.tools.shell.Command.Result;
import de.nikolauswinter.biography.tools.shell.CommandException;
import de.nikolauswinter.biography.tools.shell.CommandTimeoutException;

public class ThumbnailGenerator {
    
    private static final int TIMEOUT_SECONDS = 10;

    public static void generateThumbnailFromVideo(File source, File target, int height) throws ThumbnailGeneratorException {
        Objects.requireNonNull(source, "The source file must not be null.");
        Objects.requireNonNull(target, "The target file must not be null.");
        if (!source.exists()) {
            throw new IllegalArgumentException(String.format("The source file %s does not exist.", source));
        }
        if (target.exists()) {
            throw new IllegalArgumentException(String.format("The target file %s does already exist.", target));
        }
        if (height < 0 || height > 1000) {
            throw new IllegalArgumentException(String.format("The height is %d but must be between 0 and 999", height));
        }
        try {
            String command = String.format("ffmpeg -y -i %s -ss 00:00:00 -vf scale=-1:%d -vframes 1 %s", source, height, target);
            Result result = Command.run(command, TIMEOUT_SECONDS);
            if(result.getExitCode()!= 0) {
                throw new ThumbnailGeneratorException(String.format("ffmpeg exited with code %d:%n%n%s%n", result.getExitCode(), result.getStdError()));
            }
        } catch (CommandTimeoutException e) {
            throw new ThumbnailGeneratorException(MessageFormat.format("System call to ffmpeg timed out after {0} seconds.", TIMEOUT_SECONDS), e);
        } catch (CommandException e) {
            throw new ThumbnailGeneratorException("System call to ffmpeg failed.", e);
        }
        if (!target.exists()) {
            throw new ThumbnailGeneratorException("No thumbnail was generated for unknown reasons.");
        }
    }

    public static void generateThumbnailFromImage(File source, File target, int height) throws ThumbnailGeneratorException {
        Objects.requireNonNull(source, "The source file must not be null.");
        Objects.requireNonNull(target, "The target file must not be null.");
        if (!source.exists()) {
            throw new IllegalArgumentException(String.format("The source file %s does not exist.", source));
        }
        if (target.exists()) {
            throw new IllegalArgumentException(String.format("The target file %s does already exist.", target));
        }
        if (height < 0 || height > 1000) {
            throw new IllegalArgumentException(String.format("The height is %d but must be between 0 and 999", height));
        }
        try {
            String command = String.format("convert %s -auto-orient -resize x%d\\> %s", source, height, target);
            Result result = Command.run(command, TIMEOUT_SECONDS);
            if(result.getExitCode()!= 0) {
                throw new ThumbnailGeneratorException(String.format("imagemagick exited with code %d:%n%n%s%n", result.getExitCode(), result.getStdError()));
            }
        } catch (CommandTimeoutException e) {
            throw new ThumbnailGeneratorException(MessageFormat.format("System call to imagemagick timed out after {0} seconds.", TIMEOUT_SECONDS), e);
        } catch (CommandException e) {
            throw new ThumbnailGeneratorException("System call to imagemagick failed.", e);
        }
        if (!target.exists()) {
            throw new ThumbnailGeneratorException("No thumbnail was generated for unknown reasons.");
        }
    }

}
