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

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

//    @Autowired
    private TelegramBot telegramBot;
//    private NotificationService notificationService;
//
//    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationService notificationService) {
//        this.telegramBot = telegramBot;
//        this.notificationService = notificationService;
//    }


    public TelegramBotUpdatesListener(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
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
                SendMessage sendMessage = new SendMessage(update.message().chat().id(), "Hi, how are you?");
                telegramBot.execute(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage(update.message().chat().id(), "I'm waiting for a start");
                telegramBot.execute(sendMessage);
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

}
