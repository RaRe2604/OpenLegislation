package gov.nysenate.openleg.model.sourcefiles.sobi;

import gov.nysenate.openleg.model.sourcefiles.SourceFile;

/**
 * This exception is thrown when the contents of a sobi file cannot be read
 */
public class UnreadableSobiEx extends RuntimeException {

    private static final long serialVersionUID = 8708541650408827491L;

    private SobiFile sobiFile;

    public UnreadableSobiEx(SourceFile sobiFile, Throwable cause) {
        super("Could not read text from sobi file: " + sobiFile, cause);
    }

    public SobiFile getSobiFile() {
        return sobiFile;
    }
}
