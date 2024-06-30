package cat.udl.easymodel.vcomponent.common;

import cat.udl.easymodel.logic.types.NotificationType;

public class PendingNotification {
    private String notificationMessage;
    private NotificationType pendingNotificationType;

    public PendingNotification(String notificationMessage, NotificationType pendingNotificationType){
        this.notificationMessage = notificationMessage;
        this.pendingNotificationType = pendingNotificationType;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public NotificationType getPendingNotificationType() {
        return pendingNotificationType;
    }
}
