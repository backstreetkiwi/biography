package de.nikolauswinter.biography.tools.exif;

public class ExifMappingException extends RuntimeException {

    private static final long serialVersionUID = -2329602724456845702L;

    public ExifMappingException() {
        super();
    }

    public ExifMappingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ExifMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExifMappingException(String message) {
        super(message);
    }

    public ExifMappingException(Throwable cause) {
        super(cause);
    }

}
