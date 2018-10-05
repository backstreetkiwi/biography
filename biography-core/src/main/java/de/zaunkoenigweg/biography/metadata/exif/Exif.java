package de.zaunkoenigweg.biography.metadata.exif;

public enum Exif {

	IMAGE_DESCRIPTION("imageDescription", "Image Description"),
	DATETIME_ORIGINAL("dateTimeOriginal", "Date/Time Original"),
  SUBSEC_TIME_ORIGINAL("subSecTimeOriginal", "Sub Sec Time Original"),  
  CAMERA_MAKE("make", "Make"),  
  CAMERA_MODEL("model", "Camera Model Name"),  
	USER_COMMENT("userComment", "User Comment");
	
	private String exiftoolParam;
	private String exiftoolOutputKey;

	private Exif(String exiftoolParam, String exiftoolOutputKey) {
		this.exiftoolParam = exiftoolParam;
		this.exiftoolOutputKey = exiftoolOutputKey;
	}

	public String getExiftoolParam() {
		return exiftoolParam;
	}
	
	public String getExiftoolOutputKey() {
		return exiftoolOutputKey;
	}
}
