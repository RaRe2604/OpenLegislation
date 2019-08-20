package gov.nysenate.openleg.controller.api.admin;

import com.google.common.collect.Range;
import gov.nysenate.openleg.client.response.base.BaseResponse;
import gov.nysenate.openleg.client.response.base.ListViewResponse;
import gov.nysenate.openleg.client.response.base.ViewObjectResponse;
import gov.nysenate.openleg.client.response.error.ErrorCode;
import gov.nysenate.openleg.client.response.error.ErrorResponse;
import gov.nysenate.openleg.client.response.error.ViewObjectErrorResponse;
import gov.nysenate.openleg.client.view.process.DataProcessRunDetailView;
import gov.nysenate.openleg.client.view.process.DataProcessRunInfoView;
import gov.nysenate.openleg.client.view.process.DataProcessRunView;
import gov.nysenate.openleg.controller.api.base.BaseCtrl;
import gov.nysenate.openleg.controller.api.base.InvalidRequestParamEx;
import gov.nysenate.openleg.dao.base.LimitOffset;
import gov.nysenate.openleg.dao.base.PaginatedList;
import gov.nysenate.openleg.config.Environment;
import gov.nysenate.openleg.model.process.DataProcessRun;
import gov.nysenate.openleg.model.process.DataProcessRunInfo;
import gov.nysenate.openleg.model.process.DataProcessUnit;
import gov.nysenate.openleg.processor.DataProcessor;
import gov.nysenate.openleg.service.process.DataProcessLogService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static gov.nysenate.openleg.controller.api.base.BaseCtrl.BASE_ADMIN_API_PATH;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(value = BASE_ADMIN_API_PATH + "/process", method = RequestMethod.GET)
public class DataProcessCtrl extends BaseCtrl
{
    private static final Logger logger = LoggerFactory.getLogger(DataProcessCtrl.class);
    private static final int COUNT = 100;
    public static long getRunsTimer = 0;
    private static long getInfoTimer = 0;
    private static long getUnitsTimer = 0;

    @Autowired private Environment env;
    @Autowired private DataProcessLogService processLogs;
    @Autowired private DataProcessor dataProcessor;

    /**
     * Data Process API
     * ----------------
     *
     * Triggers a data processing run
     * Usage: (POST) /api/3/admin/process/run
     *
     * Expected Output: DataProcessRunView if the run was successful, ErrorResponse otherwise
     */
    @RequiresPermissions("admin:dataProcess")
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public BaseResponse triggerDataProcess() {
        try {
            DataProcessRun run = dataProcessor.run("api", true);
            if (run != null) {
                return new ViewObjectResponse<>(new DataProcessRunView(run), "run started");
            }
            return new ErrorResponse(ErrorCode.DATA_PROCESS_RUN_FAILED);
        } catch (Exception ex) {
            logger.error("DataProcess exception: \n{}", ex);
            return new ViewObjectErrorResponse(ErrorCode.DATA_PROCESS_RUN_FAILED, ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * Data Process Runs API
     * ---------------------
     *
     * Get the process runs that occurred within a given data/time range.
     * Usage;
     * (GET) /api/3/admin/process/runs
     * (GET) /api/3/admin/process/runs/{from}
     * (GET) /api/3/admin/process/runs/{from}/{to}
     *
     * Where 'from' and 'to' are date times.
     *
     * Optional Params: full (boolean) - If true, returns process runs with no activity as well
     *                  detail (boolean) - If true, returns the first hundred or so units for each run.
     *                  limit, offset (int) - Paginate through the runs.
     *
     * Expected Output: DataProcessRunDetailView if 'detail' = true, DataProcessRunView otherwise.
     */

    /**
     * Gets the process runs from the past week.
     * @see #getRunsDuring(String, String, WebRequest)
     */
    @RequiresPermissions("admin:dataProcess")
    @RequestMapping("/runs")
    public BaseResponse getRecentRuns(WebRequest request) throws InvalidRequestParamEx {
        return getRunsDuring(LocalDateTime.now().minusDays(7), LocalDateTime.now(), request);
    }

    /**
     * Gets the process runs from a given date time.
     * @see #getRunsDuring(String, String, WebRequest)
     */
    @RequiresPermissions("admin:dataProcess")
    @RequestMapping("/runs/{from}")
    public BaseResponse getRunsFrom(@PathVariable String from, WebRequest request) throws InvalidRequestParamEx {
        LocalDateTime fromDateTime = parseISODateTime(from, "from");
        return getRunsDuring(fromDateTime, LocalDateTime.now(), request);
    }

    @RequiresPermissions("admin:dataProcess")
    @RequestMapping("/runs/{from}/{to}")
    public BaseResponse getRunsDuring(@PathVariable String from, @PathVariable String to, WebRequest request)
                                      throws InvalidRequestParamEx {
        LocalDateTime fromDateTime = parseISODateTime(from, "from");
        LocalDateTime toDateTime = parseISODateTime(to, "to");
        return getRunsDuring(fromDateTime, toDateTime, request);
    }

    private BaseResponse getRunsDuring(LocalDateTime fromDateTime, LocalDateTime toDateTime, WebRequest request) {
        long timer = 0;
        timer -= System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++)
            getRunsDuringa(fromDateTime, toDateTime, request);
        timer += System.currentTimeMillis();
        logger.info("Average of {} runs is: {}", COUNT, timer/COUNT);
        logger.info("Average for getRunsTimer is {}%", (100.0*getRunsTimer)/timer);
        //logger.info("Average of getInfoTimer is {}%", (100.0*getInfoTimer)/timer);
        //logger.info("Average of getUnitsTimer is {}%", (100.0*getUnitsTimer)/timer);
        getRunsTimer = 0; getInfoTimer = 0; getUnitsTimer = 0;
        return getRunsDuringa(fromDateTime, toDateTime, request);
    }

    private BaseResponse getRunsDuringa(LocalDateTime fromDateTime, LocalDateTime toDateTime, WebRequest request) {
        LimitOffset limOff = getLimitOffset(request, 100);
        boolean full = getBooleanParam(request, "full", false);
        boolean detail = getBooleanParam(request, "detail", false);

        //getRunsTimer -= System.currentTimeMillis();
        PaginatedList<DataProcessRun> runs = processLogs.getRuns(Range.closedOpen(fromDateTime, toDateTime), limOff, !full);
        //getRunsTimer += System.currentTimeMillis();
        return ListViewResponse.of(runs.getResults().stream()
            .map(run -> getInfoView(run, detail)).collect(toList()),
            runs.getTotal(), runs.getLimOff());
    }

    /**
     * Gets the proper DataProcessRunInfo view.
     * @param r to get data from.
     * @param detail whether to include full details of the run.
     * @return the proper view.
     */
    private DataProcessRunInfoView getInfoView(DataProcessRun r, boolean detail) {
        if (!detail) {
            //getInfoTimer -= System.currentTimeMillis();
            DataProcessRunInfo p = processLogs.getRunInfoFromRun(r);
            //getInfoTimer += System.currentTimeMillis();
            return new DataProcessRunInfoView(p);
        }
        //getUnitsTimer -= System.currentTimeMillis();
        PaginatedList<DataProcessUnit> units = processLogs.getUnits(r.getProcessId(), LimitOffset.FIFTY);
        //getUnitsTimer += System.currentTimeMillis();
        List<DataProcessUnit> unitResults = units.getResults();
        DataProcessRunInfo runInfo = new DataProcessRunInfo(r);
        if (!unitResults.isEmpty()) {
            runInfo.setFirstProcessed(Optional.of(unitResults.get(0)));
            runInfo.setLastProcessed(Optional.of(unitResults.get(unitResults.size() - 1)));
        }
        return new DataProcessRunDetailView(runInfo, units);
    }

    /**
     * Single Data Process Run API
     * ---------------------------
     *
     * Get a single data process run via the process id (int).
     * Usage: (GET) /api/3/admin/process/runs/{id}
     *
     * Optional Params: limit, offset (int) - Paginate through the units associated with this run.
     *
     * Expected Output: DataProcessRunDetailView
     */
    @RequiresPermissions("admin:dataProcess")
    @RequestMapping("/runs/id/{id:[0-9]+}")
    public BaseResponse getRuns(@PathVariable int id, WebRequest webRequest) {
        LimitOffset limOff = getLimitOffset(webRequest, 100);
        Optional<DataProcessRunInfo> runInfo = processLogs.getRunInfo(id);
        if (runInfo.isPresent()) {
            return new ViewObjectResponse<>(
                new DataProcessRunDetailView(runInfo.get(), processLogs.getUnits(runInfo.get().getRun().getProcessId(), limOff)));
        }
        else {
            return new ErrorResponse(ErrorCode.PROCESS_RUN_NOT_FOUND);
        }
    }
}
