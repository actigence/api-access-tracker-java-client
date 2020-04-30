package com.actigence.aal.aws;

import com.actigence.aal.dto.ApiAccessLog;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;

@Slf4j
public class SQSClient
{
    private static final String QUEUE_NAME_ENV = "AAL_QUEUE_NAME";
    private static final String QUEUE_NAME_SYS_PROP = "all.queue_name";
    private static final String CLIENT_ID_ENV = "AAL_CLIENT_ID";
    private static final String CLIENT_ID_SYS_PROP = "all.client_id";
    private static final String DEFAULT_QUEUE_NAME = "aal_api_invocation_queue";

    private final Gson gson;
    private final String queueUrl;
    private final AmazonSQS sqs;

    public SQSClient()
    {
        sqs = initSQSClient();
        queueUrl = initQueue();
        gson = new Gson();
    }

    /**
     * Publish message to Api Access Logger SQS queue
     *
     * @param apiAccessLog
     */
    public void publish(ApiAccessLog apiAccessLog)
    {
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(gson.toJson(apiAccessLog));
        sqs.sendMessage(send_msg_request);
        log.debug("Message sent successfully: {}", apiAccessLog.getLogId());
    }

    private AmazonSQS initSQSClient()
    {
        AmazonSQS sqs;
        sqs = AmazonSQSClientBuilder.defaultClient();
        return sqs;
    }

    private String initQueue()
    {
        String queueUrl;
        final String queueName = getQueueName();
        log.debug("Connecting to SQS queue name: {}", queueName);


        try
        {
            CreateQueueResult create_result = sqs.createQueue(queueName);
        }
        catch (AmazonSQSException e)
        {
            if (!e.getErrorCode().equals("QueueAlreadyExists"))
            {
                log.error("Error creating queue with name: {}", queueName);
                throw e;
            } else
            {
                log.debug("Queue already exists with name: {}", queueName);
            }
        }

        queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        log.debug("Connected to SQS queue url: {}", queueUrl);
        return queueUrl;
    }

    private String getQueueName()
    {
        return ofNullable(getenv(QUEUE_NAME_ENV))
                .orElseGet(() -> ofNullable(getProperty(QUEUE_NAME_SYS_PROP))
                        .orElse(DEFAULT_QUEUE_NAME));
    }

    private String getClientId()
    {
        return ofNullable(getenv(CLIENT_ID_ENV))
                .orElseGet(() -> ofNullable(getProperty(CLIENT_ID_SYS_PROP))
                        .orElse(null));
    }
}
