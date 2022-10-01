package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class CreateOrderKafkaMessagePublisher implements OrderCreatedPaymentRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;

    private final OrderKafkaMessageHelper orderKafkaMessageHelper;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;

    public CreateOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper, OrderServiceConfigData orderServiceConfigData, OrderKafkaMessageHelper orderKafkaMessageHelper, KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.orderKafkaMessageHelper = orderKafkaMessageHelper;
        this.kafkaProducer = kafkaProducer;
    }


    @Override
    public void publish(OrderCreatedEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        log.info("Received OrderCreated event for order id: {} ",orderId);
        try {
            PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper
                    .orderCreatedEventToPaymentRequestAvroModel(domainEvent);
            kafkaProducer.send(orderServiceConfigData.getPaymentRequestTopicName(), orderId, paymentRequestAvroModel,
                    orderKafkaMessageHelper.getKafkaCallback(orderServiceConfigData.getPaymentResponseTopicName(),
                            paymentRequestAvroModel,orderId,"PaymentRequestAvroModel"));

          log.info("PaymentRequestAvroModel sent to kafka for order id: {} ",paymentRequestAvroModel.getOrderId());
        }catch (Exception e){
            log.info("error while sending paymentRequestAvroModel message " +
                    "to kafka with order id: {},error: {}  ", orderId, e.getMessage());
        }

    }

    private ListenableFutureCallback<SendResult<String, PaymentRequestAvroModel>>
    getKafkaCallback(String paymentResponseTopicName, PaymentRequestAvroModel paymentRequestAvroModel) {

    return new ListenableFutureCallback<SendResult<String, PaymentRequestAvroModel>>() {
        @Override
        public void onFailure(Throwable ex) {
            log.error("error while sending paymentRequestAvroModel "+
                    "message {} to topic {} ",paymentRequestAvroModel.toString(),paymentResponseTopicName );
        }

        @Override
        public void onSuccess(SendResult<String, PaymentRequestAvroModel> result) {
            RecordMetadata recordMetadata = result.getRecordMetadata();
            log.info("received successful response from kafka for order id: {} "+
                    "Topic : {} Partition: {} Offset :{} TimeStamp: {}",paymentRequestAvroModel.getOrderId()
            ,recordMetadata.topic(),
                    recordMetadata.partition(),
                    recordMetadata.offset(),
                    recordMetadata.timestamp());
        }
    };



    }
}
