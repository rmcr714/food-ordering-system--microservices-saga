package com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;

public interface RestaurantApprovalResponseMessageListener {

    public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse);
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse);
}
