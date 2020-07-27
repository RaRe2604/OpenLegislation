package gov.nysenate.openleg.service.notification.subscription;

import gov.nysenate.openleg.model.notification.NotificationSubscription;
import gov.nysenate.openleg.model.notification.NotificationType;
import gov.nysenate.openleg.model.notification.SubscriptionNotFoundEx;

import java.time.LocalDateTime;
import java.util.Set;

public interface NotificationSubscriptionDataService {

    /**
     * Get all subscriptions for the given user name
     *
     * @param userName String
     * @return List<NotificationSubscription>
     */
    Set<NotificationSubscription> getSubscriptions(String userName);

    /**
     * Get all subscriptions that cover notifications of the given type
     * @param type NotificationType
     * @return List<NotificationSubscription>
     */
    Set<NotificationSubscription> getSubscriptions(NotificationType type);

    /**
     * Get every single subscription.
     * @return {@link Set<NotificationSubscription>}
     */
    Set<NotificationSubscription> getAllSubscriptions();

    /**
     * Update a subscription
     * @param subscription NotificationSubscription
     * @return {@link NotificationSubscription} the updated subscription
     */
    NotificationSubscription updateSubscription(NotificationSubscription subscription);

    /**
     * Remove a subscription
     * @param subscriptionId
     */
    void removeSubscription(int subscriptionId);

    /**
     * Sets the active status for the given notification id.
     *  @param subscriptionId int
     * @param active boolean
     */
    void setActive(int subscriptionId, boolean active) throws SubscriptionNotFoundEx;
}
