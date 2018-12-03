package gov.nysenate.openleg.dao.bill.reference.openleg;

import gov.nysenate.openleg.BaseTests;
import gov.nysenate.openleg.model.bill.BaseBillId;
import gov.nysenate.openleg.model.spotcheck.SpotCheckReport;
import gov.nysenate.openleg.service.spotcheck.openleg.OpenlegBillReportService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * Created by Chenguang He on 2017/3/21.
 */
public class JsonOpenlegBillSoptCheckTests extends BaseTests {
    private static final Logger logger = LoggerFactory.getLogger(JsonOpenlegBillSoptCheckTests.class);

    @Autowired
    OpenlegBillReportService openlegBillReportService;

    @Test
    public void testGetBillView() throws Exception {
        SpotCheckReport<BaseBillId> spotCheckReport = openlegBillReportService.generateReport(LocalDateTime.parse("2017-01-01T00:00:00"),null);
        openlegBillReportService.saveReport(spotCheckReport);
    }
}
