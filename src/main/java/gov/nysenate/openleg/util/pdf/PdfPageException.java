package gov.nysenate.openleg.util.pdf;

public class PdfPageException extends RuntimeException {

    private final int pageNumber;

    public PdfPageException(int pageNumber, Throwable ex) {
        super("Pdf error on page " + pageNumber, ex);
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
