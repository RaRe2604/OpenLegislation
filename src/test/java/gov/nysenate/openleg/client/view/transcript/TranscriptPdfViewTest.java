package gov.nysenate.openleg.client.view.transcript;

import gov.nysenate.openleg.BaseTests;
import gov.nysenate.openleg.annotation.SillyTest;
import gov.nysenate.openleg.dao.base.LimitOffset;
import gov.nysenate.openleg.dao.base.SortOrder;
import gov.nysenate.openleg.model.transcript.Transcript;
import gov.nysenate.openleg.service.transcript.data.TranscriptDataService;
import gov.nysenate.openleg.util.pdf.PdfLineException;
import gov.nysenate.openleg.util.pdf.PdfPageException;
import gov.nysenate.openleg.util.pdf.PdfRenderException;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

// Not an integration test because it takes 5 mins to render all transcripts
@Category(SillyTest.class)
public class TranscriptPdfViewTest extends BaseTests {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptPdfViewTest.class);

    @Autowired
    private TranscriptDataService transcriptDataService;

    @Test
    public void renderAllTranscriptsTest() {
        long exceptions = transcriptDataService.getTranscriptIds(SortOrder.ASC, LimitOffset.ALL)
                .stream()
                .map(transcriptDataService::getTranscript)
                .map(this::writeTranscriptToNull)
                .filter(Optional::isPresent)
                .count();
        assertEquals("There should be no exceptions rendering all transcript pdfs", 0, exceptions);
    }

    private Optional<Throwable> writeTranscriptToNull(Transcript tscript) {
        try {
            TranscriptPdfView.writeTranscriptPdf(tscript, new NullOutputStream());
            return Optional.empty();
        } catch (PdfRenderException ex) {
            Throwable lastEx = ex;
            StringBuilder message = new StringBuilder("Error rendering ")
                    .append(ex.getFileName());
            if (ex.getCause() instanceof PdfPageException) {
                PdfPageException pageEx = (PdfPageException) ex.getCause();
                lastEx = pageEx;
                message.append(" page ")
                        .append(pageEx.getPageNumber());
                if (pageEx.getCause() instanceof PdfLineException) {
                    PdfLineException lineEx = (PdfLineException) pageEx.getCause();
                    lastEx = lineEx;
                    message.append(" line ")
                            .append(lineEx.getLineNumber())
                            .append(": \"")
                            .append(lineEx.getLineText())
                            .append("\"");
                }
            }
            Optional.of(lastEx.getCause())
                    .ifPresent(e -> message.append(": ").append(e.getMessage()));
            logger.error(message.toString());
            return Optional.of(ex);
        } catch (IOException e) {
            throw new RuntimeException("Uh Ohhhh", e);
        }
    }
}