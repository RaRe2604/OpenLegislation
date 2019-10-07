package gov.nysenate.openleg.util.pdf;

public class PdfLineException extends RuntimeException {

    private final int lineNumber;
    private final String lineText;

    public PdfLineException(int lineNumber, String lineText, Throwable cause) {
        super("Pdf error on line " + lineNumber + ": \"" + lineText + "\"", cause);
        this.lineNumber = lineNumber;
        this.lineText = lineText;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLineText() {
        return lineText;
    }
}
