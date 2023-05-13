package de.nikolauswinter.biography.tools.exif;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExifToolTest {

    private File someEmptyFolder;
    
    @BeforeEach
    public void setUp() throws IOException {
        someEmptyFolder = Files.createTempDirectory("someEmptyFolder").toFile();
        someEmptyFolder.deleteOnExit();
    }
    
    @Test
    public void testReadOneFileNull() {
        assertThrows(NullPointerException.class, () -> {
            ExifTool.read((File)null);
        });
    }

    @Test
    public void testReadOneFileThatDoesNotExist() {
        assertThrows(IllegalStateException.class, () -> {
            ExifTool.read(new File(someEmptyFolder, "noimage.jpg"));
        });
    }

    @Test
    public void testReadOneCorruptFile() {
        assertThrows(ExifMappingException.class, () -> {
            ExifTool.read(new File(getClass().getResource("/tools/exiftool/folderC/NikonD70_corrupt.jpg").getPath()));
        });
    }

    @Test
    public void testReadOneNonExifFile() {
        File file = new File(getClass().getResource("/tools/exiftool/folderB/noimage").getPath());
        
        ExifData exifData = ExifTool.read(file);
        assertThat(exifData).isNotNull();
        assertThat(exifData.getDateTimeOriginal()).isNotPresent();
        assertThat(exifData.getSubsecTimeOriginal()).isNotPresent();
        assertThat(exifData.getImageDescription()).isNotPresent();
        assertThat(exifData.getCameraMake()).isNotPresent();
        assertThat(exifData.getCameraModel()).isNotPresent();
        assertThat(exifData.getUserComment()).isNotPresent();
    }

    @Test
    public void testReadOneFile() {
        File file = new File(getClass().getResource("/tools/exiftool/folderA/NikonD70.jpg").getPath());
        
        ExifData exifData = ExifTool.read(file);
        assertThat(exifData).isNotNull();
        assertThat(exifData.getDateTimeOriginal()).isPresent();
        assertThat(exifData.getDateTimeOriginal()).contains(LocalDateTime.of(2005,2,22,13,51,32));
        assertThat(exifData.getSubsecTimeOriginal()).isPresent();
        assertThat(exifData.getSubsecTimeOriginal()).contains(Integer.valueOf(80));
        assertThat(exifData.getImageDescription()).isPresent();
        assertThat(exifData.getImageDescription()).contains("Christchurch; auf dem Cathedral Square");
        assertThat(exifData.getCameraMake()).isPresent();
        assertThat(exifData.getCameraMake()).contains("NIKON CORPORATION");
        assertThat(exifData.getCameraModel()).isPresent();
        assertThat(exifData.getCameraModel()).contains("NIKON D70");
        assertThat(exifData.getUserComment()).isNotPresent();
    }

    @Test
    public void testReadManyFilesNull() {
        assertThrows(NullPointerException.class, () -> {
            ExifTool.read((String)null);
        });
    }
    
    @Test
    public void testReadManyFilesEmptyFolder() {
        Map<File, ExifData> files = ExifTool.read(this.someEmptyFolder.getAbsolutePath());
        assertThat(files).isNotNull().isEmpty();
    }
    
    @Test
    public void testReadManyFilesNonExistingFolder() {
        assertThrows(IllegalStateException.class, () -> {
            ExifTool.read((new File(this.someEmptyFolder, "nonExistingFolder")).getAbsolutePath());
        });
    }
    
    @Test
    public void testReadManyFiles() {
        Map<File, ExifData> files = ExifTool.read(getClass().getResource("/tools/exiftool/").getPath());
        assertThat(files).isNotNull().hasSize(2);

        File file;
        ExifData exifData;

        // Files which are not recognized as image files by exiftool are not included in the bulk read operation.
        file = new File(getClass().getResource("/tools/exiftool/folderB/noimage").getPath());
        assertThat(files).doesNotContainKey(file);

        // Files with corrupt metadata are not included in the bulk read operation
        file = new File(getClass().getResource("/tools/exiftool/folderC/NikonD70_corrupt.jpg").getPath());
        assertThat(files).doesNotContainKey(file);
        
        file = new File(getClass().getResource("/tools/exiftool/folderA/NikonD70.jpg").getPath());
        assertThat(files).containsKey(file);
        
        exifData = files.get(file);
        assertThat(exifData).isNotNull();
        assertThat(exifData.getDateTimeOriginal()).isPresent();
        assertThat(exifData.getDateTimeOriginal()).contains(LocalDateTime.of(2005,2,22,13,51,32));
        assertThat(exifData.getSubsecTimeOriginal()).isPresent();
        assertThat(exifData.getSubsecTimeOriginal()).contains(Integer.valueOf(80));
        assertThat(exifData.getImageDescription()).isPresent();
        assertThat(exifData.getImageDescription()).contains("Christchurch; auf dem Cathedral Square");
        assertThat(exifData.getCameraMake()).isPresent();
        assertThat(exifData.getCameraMake()).contains("NIKON CORPORATION");
        assertThat(exifData.getCameraModel()).isPresent();
        assertThat(exifData.getCameraModel()).contains("NIKON D70");
        assertThat(exifData.getUserComment()).isNotPresent();
        
        file = new File(getClass().getResource("/tools/exiftool/folderA/iPhone5s.jpg").getPath());
        assertThat(files).containsKey(file);
        
        exifData = files.get(file);
        assertThat(exifData).isNotNull();
        assertThat(exifData.getDateTimeOriginal()).isPresent();
        assertThat(exifData.getDateTimeOriginal()).contains(LocalDateTime.of(2016,7,20,21,4,13));
        assertThat(exifData.getSubsecTimeOriginal()).isPresent();
        assertThat(exifData.getSubsecTimeOriginal()).contains(Integer.valueOf(660));
        assertThat(exifData.getImageDescription()).isNotPresent();
        assertThat(exifData.getCameraMake()).isPresent();
        assertThat(exifData.getCameraMake()).contains("Apple");
        assertThat(exifData.getCameraModel()).isPresent();
        assertThat(exifData.getCameraModel()).contains("iPhone 5s");
        assertThat(exifData.getUserComment()).isNotPresent();
        
    }
    
}
