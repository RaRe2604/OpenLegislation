package gov.nysenate.openleg.client.view.bill;

import com.google.common.collect.Iterables;
import gov.nysenate.openleg.client.view.base.ListView;
import gov.nysenate.openleg.client.view.base.MapView;
import gov.nysenate.openleg.client.view.base.ViewObject;
import gov.nysenate.openleg.model.bill.BillInfo;
import gov.nysenate.openleg.model.bill.BillStatusType;

import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Just the essentials for displaying a Bill in a search result for example.
 */
public class BillInfoView extends SimpleBillInfoView implements ViewObject
{
    protected String summary;
    protected boolean signed;
    protected boolean adopted;
    protected boolean vetoed;
    protected BillStatusView status;
    protected ListView<BillStatusView> milestones;
    protected ListView<BillActionView> actions;
    protected MapView<String, PublishStatusView> publishStatusMap;
    protected ProgramInfoView programInfo;

    public BillInfoView(BillInfo billInfo) {
        super(billInfo);
        if (billInfo != null) {
            summary = billInfo.getSummary();
            if (!billInfo.getMilestones().isEmpty()) {
                BillStatusType lastStatus = Iterables.getLast(billInfo.getMilestones()).getStatusType();
                if (!billType.isResolution()) {
                    if (lastStatus.equals(BillStatusType.SIGNED_BY_GOV)) {
                        signed = true;
                    }
                    else if (lastStatus.equals(BillStatusType.VETOED)) {
                        vetoed = true;
                    }
                }
                else if (lastStatus.equals(BillStatusType.ADOPTED)) {
                    adopted = true;
                }
            }
            billType = new BillTypeView(billInfo.getBillId().getBillType());
            programInfo = billInfo.getProgramInfo() != null ? new ProgramInfoView(billInfo.getProgramInfo()) : null;
            status = new BillStatusView(billInfo.getStatus());
            milestones = ListView.of(billInfo.getMilestones().stream().map(BillStatusView::new).collect(toList()));
            actions = ListView.of(billInfo.getActions().stream().map(BillActionView::new).collect(toList()));
            publishStatusMap = billInfo.getAmendPublishStatusMap().entrySet().stream()
                    .map((pubStatEntry) -> new PublishStatusView(pubStatEntry.getKey().displayName(), pubStatEntry.getValue()))
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(PublishStatusView::getVersion, Function.identity()),
                            MapView::of));
        }
    }

    protected BillInfoView(){
        super();
    }

    public String getSummary() {
        return summary;
    }

    public boolean isSigned() {
        return signed;
    }

    public boolean isAdopted() {
        return adopted;
    }

    public boolean isVetoed() {
        return vetoed;
    }

    public BillStatusView getStatus() {
        return status;
    }

    public ListView<BillStatusView> getMilestones() {
        return milestones;
    }

    public ListView<BillActionView> getActions() {
        return actions;
    }

    public ProgramInfoView getProgramInfo() {
        return programInfo;
    }

    public MapView<String, PublishStatusView> getPublishStatusMap() {
        return publishStatusMap;
    }

    @Override
    public String getViewType() {
        return "bill-info";
    }
}