package de.nikolauswinter.biography.tools.thumbnails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.Test;

public class ThumbnailGeneratorTest {

    private File baseFolder = new File(getClass().getResource("/tools/thumbnails/").getPath());

    @Test()
    public void testGenerateThumbnailFromVideoSrcDoesNotExist() throws ThumbnailGeneratorException {
        assertThrows(IllegalArgumentException.class, () -> {
            File source = new File(baseFolder, "not_existing.mov");
            File target = new File(baseFolder, "not_existing.jpg");
            ThumbnailGenerator.generateThumbnailFromVideo(source, target, 300);
        });
    }

    @Test()
    public void testGenerateThumbnailFromVideoTargetDoesExist() throws ThumbnailGeneratorException {
        assertThrows(IllegalArgumentException.class, () -> {
            File source = new File(baseFolder, "mov.mov");
            File target = new File(baseFolder, "existing_target.jpg");
            ThumbnailGenerator.generateThumbnailFromVideo(source, target, 300);
        });
    }

    @Test
    public void testGenerateThumbnailForMov() throws ThumbnailGeneratorException {
        File source = new File(baseFolder, "mov.mov");
        File target = new File(baseFolder, "mov.jpg");
        assertThat(target).doesNotExist();
        ThumbnailGenerator.generateThumbnailFromVideo(source, target, 300);
        assertThat(target).exists();
    }

    @Test
    public void testGenerateThumbnailForMp4() throws ThumbnailGeneratorException {
        File source = new File(baseFolder, "mp4.mp4");
        File target = new File(baseFolder, "mp4.jpg");
        assertThat(target).doesNotExist();
        ThumbnailGenerator.generateThumbnailFromVideo(source, target, 300);
        assertThat(target).exists();
    }

    @Test
    public void testGenerateThumbnailForAvi() throws ThumbnailGeneratorException {
        File source = new File(baseFolder, "avi.avi");
        File target = new File(baseFolder, "avi.jpg");
        assertThat(target).doesNotExist();
        ThumbnailGenerator.generateThumbnailFromVideo(source, target, 300);
        assertThat(target).exists();
    }

    @Test
    public void testGenerateThumbnailForMpeg() throws ThumbnailGeneratorException {
        File source = new File(baseFolder, "mpg.mpg");
        File target = new File(baseFolder, "mpg.jpg");
        assertThat(target).doesNotExist();
        ThumbnailGenerator.generateThumbnailFromVideo(source, target, 300);
        assertThat(target).exists();
    }

    @Test()
    public void testGenerateThumbnailFromImageSrcDoesNotExist() throws ThumbnailGeneratorException {
        assertThrows(IllegalArgumentException.class, () -> {
            File source = new File(baseFolder, "not_existing.jpg");
            File target = new File(baseFolder, "thumbnail.jpg");
            ThumbnailGenerator.generateThumbnailFromImage(source, target, 300);
        });
    }

    @Test()
    public void testGenerateThumbnailFromImageTargetDoesExist() throws ThumbnailGeneratorException {
        assertThrows(IllegalArgumentException.class, () -> {
            File source = new File(baseFolder, "NikonD70.jpg");
            File target = new File(baseFolder, "existing_target.jpg");
            ThumbnailGenerator.generateThumbnailFromImage(source, target, 300);
        });
    }

    @Test
    public void testGenerateThumbnailForImageLandscape() throws ThumbnailGeneratorException {
        File source = new File(baseFolder, "NikonD70.jpg");
        File target = new File(baseFolder, "thumbnail_landscape.jpg");
        assertThat(target).doesNotExist();
        ThumbnailGenerator.generateThumbnailFromImage(source, target, 300);
        assertThat(target).exists();
    }

    @Test
    public void testGenerateThumbnailForImagePortrait() throws ThumbnailGeneratorException {
        File source = new File(baseFolder, "iPhone5s.jpg");
        File target = new File(baseFolder, "thumbnail_portrait.jpg");
        assertThat(target).doesNotExist();
        ThumbnailGenerator.generateThumbnailFromImage(source, target, 300);
        assertThat(target).exists();
    }
}
