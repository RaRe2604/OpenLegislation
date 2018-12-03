package gov.nysenate.openleg.service.sobi;

import gov.nysenate.openleg.BaseTests;
import gov.nysenate.openleg.processor.sobi.SobiProcessService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SobiProcessServiceTest extends BaseTests
{
    private static final Logger logger = LoggerFactory.getLogger(SobiProcessServiceTest.class);

    @Autowired
    private SobiProcessService sobiProcessService;

    @Test
    public void ingestTest() {
            sobiProcessService.ingest();
    }

    @Test
    public void fullTest() {
            sobiProcessService.collate();
            sobiProcessService.ingest();
    }


}
