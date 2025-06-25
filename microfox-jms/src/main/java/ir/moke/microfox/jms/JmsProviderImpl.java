package ir.moke.microfox.jms;

import ir.moke.microfox.api.jms.JmsProvider;
import ir.moke.microfox.exception.MicrofoxException;
import jakarta.jms.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static ir.moke.microfox.api.jms.DestinationType.QUEUE;
import static ir.moke.microfox.api.jms.DestinationType.TOPIC;

public class JmsProviderImpl implements JmsProvider {
    private static final Logger logger = LoggerFactory.getLogger(JmsProviderImpl.class);
    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Integer IDLE_BETWEEN_RETRY = Integer.valueOf(JmsConfig.MICROFOX_JMS_IDLE_BETWEEN_RETRY);

    public void produceQueue(String identity, boolean transacted, int acknowledgeMode, Consumer<Session> consumer) {
        ConnectionFactory connectionFactory = JmsFactory.getConnectionFactory(identity);
        try (Connection connection = connectionFactory.createConnection()) {
            Session session = connection.createSession(transacted, acknowledgeMode);
            consumer.accept(session);
        } catch (Exception e) {
            throw new MicrofoxException(e);
        }
    }

    public void produceTopic(String identity, boolean transacted, int acknowledgeMode, Consumer<Session> consumer) {
        ConnectionFactory connectionFactory = JmsFactory.getConnectionFactory(identity);
        try (Connection connection = connectionFactory.createConnection()) {
            Session session = connection.createSession(transacted, acknowledgeMode);
            consumer.accept(session);
        } catch (Exception e) {
            throw new MicrofoxException(e);
        }
    }

    public void consumeQueue(String identity, String queueName, int acknowledgeMode, MessageListener listener) {
        reconnectScheduler.scheduleWithFixedDelay(() -> {
            try {
                ConnectionFactory connectionFactory = JmsFactory.getConnectionFactory(identity);
                JMSContext context = connectionFactory.createContext(acknowledgeMode);
                Destination destination = context.createQueue(queueName);
                JMSConsumer consumer = context.createConsumer(destination);
                consumer.setMessageListener(listener);
                context.setExceptionListener(new JmsExceptionHandler(identity, QUEUE, queueName, acknowledgeMode, listener));
                JmsFactory.registerContext(identity, context);
            } catch (Exception e) {
                logger.debug(e.getMessage());
                JmsFactory.closeContext(identity);
                consumeQueue(identity, queueName, acknowledgeMode, listener);
            }
        }, 0, IDLE_BETWEEN_RETRY, TimeUnit.MILLISECONDS);
    }

    public void consumeTopic(String identity, String topicName, int acknowledgeMode, MessageListener listener) {
        reconnectScheduler.scheduleWithFixedDelay(() -> {
            try {
                ConnectionFactory connectionFactory = JmsFactory.getConnectionFactory(identity);
                JMSContext context = connectionFactory.createContext(acknowledgeMode);
                Destination destination = context.createTopic(topicName);
                JMSConsumer consumer = context.createConsumer(destination);
                consumer.setMessageListener(listener);
                JmsFactory.registerContext(identity, context);
                context.setExceptionListener(new JmsExceptionHandler(identity, TOPIC, topicName, acknowledgeMode, listener));
            } catch (Exception e) {
                logger.debug(e.getMessage());
                JmsFactory.closeContext(identity);
                consumeTopic(identity, topicName, acknowledgeMode, listener);
            }
        }, 0, IDLE_BETWEEN_RETRY, TimeUnit.SECONDS);
    }
}
