package gov.nysenate.openleg.util.pdf;

public class PdfRenderException extends RuntimeException {

    private final String fileName;
    private final String fileType;

    public PdfRenderException(String fileName, String fileType, Throwable cause) {
        super("Error while rendering " + fileName + " pdf: " + fileName, cause);
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }
}
