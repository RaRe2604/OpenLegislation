package gov.nysenate.openleg.client.view.hearing;

import com.google.common.collect.ImmutableList;
import gov.nysenate.openleg.BaseTests;
import gov.nysenate.openleg.annotation.IntegrationTest;
import gov.nysenate.openleg.dao.base.LimitOffset;
import gov.nysenate.openleg.dao.base.SortOrder;
import gov.nysenate.openleg.model.hearing.PublicHearing;
import gov.nysenate.openleg.model.hearing.PublicHearingFile;
import gov.nysenate.openleg.processor.hearing.PublicHearingParser;
import gov.nysenate.openleg.service.hearing.data.PublicHearingDataService;
import gov.nysenate.openleg.util.FileIOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Category(IntegrationTest.class)
public class PublicHearingPdfViewTest extends BaseTests {

    @Autowired private PublicHearingDataService pHearDataService;
    @Autowired private PublicHearingParser pHearParser;

    /**
     * Smoke test attempting to write all existing/test public hearings as pdfs.
     */
    @Test
    public void renderAllPubHearingsTest() throws IOException {
        ImmutableList<PublicHearing> pHears = ImmutableList.<PublicHearing>builder()
                .addAll(getTestPHears())
                .addAll(getDataPHears())
                .build();
        for (PublicHearing pHear : pHears) {
            PublicHearingPdfView.writePublicHearingPdf(pHear, new NullOutputStream());
        }
    }

    private List<PublicHearing> getDataPHears() {
        return pHearDataService.getPublicHearingIds(SortOrder.ASC, LimitOffset.ALL)
                .stream()
                .map(pHearDataService::getPublicHearing)
                .collect(Collectors.toList());
    }

    private List<PublicHearing> getTestPHears() {
        File hearingDir = FileIOUtils.getResourceFile("hearing");
        RegexFileFilter hearingFilter =
                new RegexFileFilter(Pattern.compile("^\\d{2}-\\d{2}-\\d{2}.*\\.txt", Pattern.CASE_INSENSITIVE));
        return FileUtils.listFiles(hearingDir, hearingFilter, null)
                .stream()
                .map(this::toPHear)
                .collect(Collectors.toList());
    }

    private PublicHearing toPHear(File file) {
        try {
            return pHearParser.parseHearingFile(new PublicHearingFile(file));
        } catch (IOException e) {
            throw new RuntimeException("Uh Ohhhhh", e);
        }
    }

}