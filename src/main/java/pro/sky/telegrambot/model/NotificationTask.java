package pro.sky.telegrambot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private LocalDateTime notificationDate;
    private String notificationText;
    private Boolean executed;

    public NotificationTask() {
    }

    public NotificationTask(String notificationText, LocalDateTime notificationDate) {
        this.notificationDate = notificationDate;
        this.notificationText = notificationText;
        this.executed = false;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public LocalDateTime getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(LocalDateTime notificationDate) {
        this.notificationDate = notificationDate;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public Boolean getExecuted() {
        return executed;
    }

    public void setExecuted(Boolean executed) {
        this.executed = executed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTask that = (NotificationTask) o;
        return id.equals(that.id) && chatId.equals(that.chatId) && notificationDate.equals(that.notificationDate) && notificationText.equals(that.notificationText) && executed.equals(that.executed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, notificationDate, notificationText, executed);
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", notificationDate=" + notificationDate +
                ", notificationText='" + notificationText + '\'' +
                ", executed=" + executed +
                '}';
    }
}
