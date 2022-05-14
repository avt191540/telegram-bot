package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

//    @Autowired
    private TelegramBot telegramBot;
    private final NotificationRepository repository;
//    private NotificationService notificationService;
//
//    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationService notificationService) {
//        this.telegramBot = telegramBot;
//        this.notificationService = notificationService;
//    }


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
                logger.info("start has been received");
                sendMessage(message.chat().id(), "Hi, how are you?");
                LocalDateTime dateTimeNow = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
                sendMessage(message.chat().id(), dateTimeNow.toString());

//                SendMessage sendMessage = new SendMessage(message.chat().id(), "Hi, how are you?");
//                telegramBot.execute(sendMessage);
            } else {
                NotificationTask parsMessageResult = parseMessage(message.text());
                if (parsMessageResult != null && parsMessageResult.getNotificationDate().isAfter(LocalDateTime.now())) {
                    recordNotification(message.chat().id(), parsMessageResult);
                } else {
                    sendMessage(message.chat().id(), "Incorrect notification please try again");
                }

//                SendMessage sendMessage = new SendMessage(message.chat().id(), "I'm waiting for a start");
//                telegramBot.execute(sendMessage);
            }

        }
//        updates.forEach(update -> {
//            logger.info("Processing update: {}", update);
//            // Process your updates here
//        });
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
//                taskFromMessage = new NotificationTask();
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

}
