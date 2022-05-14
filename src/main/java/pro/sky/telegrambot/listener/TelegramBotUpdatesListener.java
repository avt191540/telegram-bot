package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private TelegramBot telegramBot;
    private final NotificationRepository repository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            logger.info("Processing update: {}", update);
            Message message = update.message();
            if (message.text().startsWith("/start")) {
                logger.info("Start command has been received");
                LocalDateTime dateTimeNow = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
                sendMessage(message.chat().id(), "Hi, how are you? Current date and time: " + dateTimeNow.toString());
            } else {
                NotificationTask parsMessageResult = parseMessage(message.text());
                if (parsMessageResult != null && parsMessageResult.getNotificationDate().isAfter(LocalDateTime.now())) {
                    recordNotification(message.chat().id(), parsMessageResult);
                } else {
                    sendMessage(message.chat().id(), "Incorrect notification please try again");
                }
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private NotificationTask parseMessage(String botMessage) {
        Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(botMessage);
        NotificationTask taskFromMessage = null;
        try {
            if (matcher.find()) {
                LocalDateTime messageDateTime = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                String messageText = matcher.group(3);
                taskFromMessage = new NotificationTask(messageText, messageDateTime);
            }
        } catch (Exception e) {
            logger.error("Incorrect to parse botMessage: " + botMessage, e);
        }
        return taskFromMessage;
    }

    private void sendMessage(Long chatId, String sendText) {
        SendMessage sendMessage = new SendMessage(chatId, sendText);
        telegramBot.execute(sendMessage);
    }

    private NotificationTask recordNotification(Long chatId, NotificationTask task) {
        task.setChatId(chatId);
        NotificationTask recordedTask = repository.save(task);
        logger.info("Task from Notification has been recorded successfully");
        sendMessage(chatId, "Received Notification has been successfully recorded");
        return recordedTask;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendingTasksToUsers() {
        logger.info("Checking tasks to sending");
        LocalDateTime dateTimeNow = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Collection<NotificationTask> sentTasks = repository.getNotificationTasksByNotificationDateEquals(dateTimeNow);
        if (sentTasks.size() != 0) {
            logger.info("Total tasks found to send: " + sentTasks.size());
            for (NotificationTask sentTask : sentTasks) {
                sendMessage(sentTask.getChatId(), sentTask.getNotificationText());
                sentTask.setExecuted(true);
            }
            repository.saveAll(sentTasks);
        }
    }
}
