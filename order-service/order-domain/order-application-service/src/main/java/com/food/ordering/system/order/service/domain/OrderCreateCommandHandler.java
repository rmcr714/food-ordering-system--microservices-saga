package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.exception.DomainException;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class OrderCreateCommandHandler {

    private final OrderDomainService orderDomainService;

    private final OrderRepository orderRepository;

    private final CustomerRepository customerRepository;

    private final RestaurantRepository restaurantRepository;

    private final OrderDataMapper orderDataMapper;

    public OrderCreateCommandHandler(OrderDomainService orderDomainService,
                                     OrderRepository orderRepository,
                                     CustomerRepository customerRepository,
                                     RestaurantRepository restaurantRepository,
                                     OrderDataMapper orderDataMapper) {
        this.orderDomainService = orderDomainService;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderDataMapper = orderDataMapper;
    }


    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand){
        
        checkCustomer(createOrderCommand.getCustomerId());
     Restaurant restaurant = checkRestaurant(createOrderCommand);
     Order order =    orderDataMapper.createOrderCommandToOrder(createOrderCommand);
     OrderCreatedEvent orderCreatedEvent =  orderDomainService.validateAndInitiateOrder(order,restaurant);

     //fire an evenet to trigger payment processing in payment service

     Order orderResult= saveOrder(order);
     log.info("order has been created with id {} ",orderResult.getId().getValue());
     return orderDataMapper.orderToCreateOrderResponse(orderResult,"Order created successfully");



    }

    private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
        Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
     Optional<Restaurant>   optionalRestaurant =  restaurantRepository.findRestaurantInformation(restaurant);
     if(optionalRestaurant.isEmpty()){
         log.warn("could not find restaurant wiht id {} ",createOrderCommand.getRestaurantId());
         throw new OrderDomainException("could not find restaurant wiht id {} "+createOrderCommand.getRestaurantId());
     }

        return optionalRestaurant.get();
    }

    private void checkCustomer(UUID customerId) {
        Optional<Customer> customer = customerRepository.findCustomer(customerId);
        if(customer.isEmpty()){
            log.info("Couldn't find the customer with id "+customerId);
            throw new DomainException("Couldn't find the customer with id "+ customerId);
        }
    }


    private Order saveOrder(Order order){
       Order orderResult =  orderRepository.save(order);
       if(orderResult==null){
           log.error("oops,could not save order");
           throw new DomainException("Could not save order");
       }
       log.info("Order with id {} saved",orderResult.getId().getValue());
       return orderResult;
    }


}
