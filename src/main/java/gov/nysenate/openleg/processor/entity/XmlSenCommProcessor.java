package gov.nysenate.openleg.processor.entity;

import gov.nysenate.openleg.model.base.SessionYear;
import gov.nysenate.openleg.model.entity.*;
import gov.nysenate.openleg.model.process.DataProcessUnit;
import gov.nysenate.openleg.model.sourcefiles.sobi.SobiFragment;
import gov.nysenate.openleg.model.sourcefiles.sobi.SobiFragmentType;
import gov.nysenate.openleg.processor.base.AbstractDataProcessor;
import gov.nysenate.openleg.processor.sobi.SobiProcessor;
import gov.nysenate.openleg.model.entity.MemberNotFoundEx;
import gov.nysenate.openleg.service.entity.member.data.MemberService;
import gov.nysenate.openleg.util.XmlHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import javax.xml.xpath.XPathExpressionException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class XmlSenCommProcessor extends AbstractDataProcessor implements SobiProcessor
{
    private static final Logger logger = LogManager.getLogger(XmlSenCommProcessor.class);

    private static final DateTimeFormatter meetTimeSDF = DateTimeFormatter.ofPattern("hh:mm a");

    @Autowired
    protected MemberService memberService;

    @Autowired
    protected XmlHelper xml;

    @PostConstruct
    public void init() {
        initBase();
    }

    /** {@inheritDoc  */
    @Override
    public SobiFragmentType getSupportedType() {
        return SobiFragmentType.COMMITTEE;
    }

    /** {@inheritDoc  */
    @Override
    public void process(SobiFragment sobiFragment) {
        logger.info("Called committee processor");
        DataProcessUnit unit = createProcessUnit(sobiFragment);
        String xmlString = sobiFragment.getText();
        try {
            Document doc = xml.parse(xmlString);
            Node committeeRoot = xml.getNode("sencommmem", doc);
            SessionYear sessionYear = new SessionYear(Integer.parseInt(xml.getString("@sessyr", committeeRoot)));
            int year = Integer.parseInt(xml.getString("@year", committeeRoot));
            Chamber chamber = Chamber.SENATE;
            logger.info("Processing " + chamber + "committees for s" + sessionYear + " y" + year + "\t" +
                        sobiFragment.getPublishedDateTime());

            committeeRoot = xml.getNode("committees", committeeRoot);
            NodeList committeeNodes = committeeRoot.getChildNodes();
            for(int i = 0; i < committeeNodes.getLength() ; i++){
                Node committeeNode = committeeNodes.item(i);
                if (committeeNode.getNodeName().equals("committee")) {
                    try {
                        Committee committee = new Committee();
                        committee.setSession(sessionYear);
                        committee.setPublishedDateTime(sobiFragment.getPublishedDateTime());
                        committee.setChamber(chamber);
                        processCommittee(committeeNode, committee);
                        committeeDataService.saveCommittee(committee, sobiFragment);
                    }
                    catch (Exception e){
                        unit.addException("XML Sen Comm parsing error", e);
                        logger.error(e);
                    }
                }
            }
        }
        catch (Exception e) {
            unit.addException("XML Sen Comm parsing error", e);
            logger.error(e);
        } finally {
            postDataUnitEvent(unit);
            checkIngestCache();
        }
    }

    @Override
    public void postProcess() {}

    @Override
    public void checkIngestCache() {
        if (!env.isSobiBatchEnabled()) {
            flushAllUpdates();
        }
    }

        /** --- Internal Methods --- */

    private Committee processCommittee(Node committeeNode, Committee committee) throws XPathExpressionException {
        committee.setName(xml.getString("name/text()", committeeNode));
        committee.setLocation(xml.getString("location/text()", committeeNode));

        String meetDay = xml.getString("meetday/text()", committeeNode);
        committee.setMeetDay(StringUtils.isNotEmpty(meetDay) ? DayOfWeek.valueOf(meetDay.toUpperCase()) : null);

        String meetTimeStr = xml.getString("meettime/text()", committeeNode);
        committee.setMeetTime(StringUtils.isNotEmpty(meetTimeStr) ? LocalTime.parse(meetTimeStr, meetTimeSDF) : null);

        committee.setMeetAltWeek(xml.getString("meetaltweek/text()", committeeNode).trim().equalsIgnoreCase("Yes"));
        committee.setMeetAltWeekText(xml.getString("meetaltweektext/text()", committeeNode));

        Node committeeMembership = xml.getNode("membership", committeeNode);
        committee.setMembers(processCommitteeMembers(committeeMembership, committee));
        return committee;
    }

    private List<CommitteeMember> processCommitteeMembers(Node committeeMembership, Committee committee)
                                                          throws XPathExpressionException {
        List<CommitteeMember> committeeMembers = new ArrayList<CommitteeMember>();
        NodeList committeeMembersNodes = committeeMembership.getChildNodes();

        for(int i = 0; i < committeeMembersNodes.getLength(); i++){

            Node memberNode = committeeMembersNodes.item(i);
            if (memberNode.getNodeName().equals("member")) {
                String shortName = xml.getString("name/text()", memberNode);
                SessionMember sessionMember;
                try {
                    sessionMember = memberService.getMemberByShortNameEnsured(shortName, committee.getSession(),
                                                                       committee.getChamber());
                }
                catch (MemberNotFoundEx memberNotFoundEx) {
                    logger.error("Could not identify committee member " + shortName + " " + committee.getSession() +
                                 " " + committee.getChamber());
                    continue;
                }

                CommitteeMember committeeMember = new CommitteeMember();
                committeeMember.setSequenceNo(Integer.parseInt(xml.getString("@seqno", memberNode)));
                committeeMember.setMember(sessionMember);
                committeeMember.setMajority(
                    xml.getString("memberlist/text()", memberNode).trim().equalsIgnoreCase("Majority"));

                String title = xml.getString("title/text()", memberNode).trim();
                if (title.equalsIgnoreCase("Chairperson")) {
                    committeeMember.setTitle(CommitteeMemberTitle.CHAIR_PERSON);
                }
                else if (title.equalsIgnoreCase("Vice-Chair")) {
                    committeeMember.setTitle(CommitteeMemberTitle.VICE_CHAIR);
                }
                else {
                    committeeMember.setTitle(CommitteeMemberTitle.MEMBER);
                }
                committeeMembers.add(committeeMember);
            }
        }
        return committeeMembers;
    }
}
