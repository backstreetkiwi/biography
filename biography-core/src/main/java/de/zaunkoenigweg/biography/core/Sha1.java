package de.zaunkoenigweg.biography.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Value object for a Biography media file SHA1 hash
 */
public class Sha1 {

	private static Pattern PATTERN = Pattern.compile("^\\p{XDigit}{40}$");

	private String sha1;
	
	private Sha1(String sha1) {
		Matcher matcher = PATTERN.matcher(Objects.requireNonNull(sha1));
		if (!matcher.matches()) {
			throw new IllegalArgumentException(String.format("Invalid sha1 '%s'", sha1));
		}
		this.sha1 = sha1;
	}

	/**
	 * Checks if the given SHA1 is valid
	 * 
	 * @param sha1 hash as string
	 * @return Is the given SHA1 valid?
	 */
	public static boolean isValid(String sha1) {
		if(sha1==null) {
			return false;
		}
		return PATTERN.matcher(sha1).matches();
	}

	/**
	 * Creates a Biography media file SHA1.
	 * 
	 * @param sha1 hash as string
	 * @return Biography media file SHA1 value object
	 */
	public static Sha1 of(String sha1) {
		if (!isValid(sha1)) {
			throw new IllegalArgumentException(String.format("'%s' is not a valid Biography media file sha1 hash.", sha1));
		}
		return new Sha1(sha1);
	}

    /**
     * Calculates the SHA-1 of the content of the media file
     * 
     * @param file  media file
     * @return SHA1 hash
     */
    public static Sha1 calculate(File file) {

        if (file == null) {
            return null;
        }

        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        
        Optional<MediaFileType> fileType = MediaFileType.of(file);
        
        if (!fileType.isPresent()) {
            return null;
        }
        
        byte[] allBytes = null;

        switch (fileType.get()) {
        case JPEG:
            allBytes = bufferedImageAsBinary(file);
            break;

        case QUICKTIME:
        case MPEG:
        case MP4:
        case AVI:
            allBytes = completeFileAsBinary(file);
            break;

        default:
            return null;
        }

        return Sha1.of(DigestUtils.sha1Hex(allBytes));
    }
    
	public String value() {
		return sha1;
	}


    private static byte[] completeFileAsBinary(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            return null;
        }
    };

    private static byte[] bufferedImageAsBinary(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            DataBuffer dataBuffer = image.getRaster().getDataBuffer();
            if(dataBuffer.getNumBanks()!=1) {
                return null;
            }
            if(dataBuffer.getDataType()!=DataBuffer.TYPE_BYTE) {
                return null;
            }
            int size = dataBuffer.getSize();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(size);
            for (int i = 0; i < dataBuffer.getSize(); i++) {
                byteArrayOutputStream.write(dataBuffer.getElem(i));
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }

    };

	@Override
	public String toString() {
		return this.sha1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sha1 == null) ? 0 : sha1.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sha1 other = (Sha1) obj;
		if (sha1 == null) {
			if (other.sha1 != null)
				return false;
		} else if (!sha1.equals(other.sha1))
			return false;
		return true;
	}
}
