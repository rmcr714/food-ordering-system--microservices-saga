package com.food.ordering.system.order.service.dataaccess.restaurant.adapter;

import com.food.ordering.system.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.order.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    private final RestaurantJpaRepository restaurantJpaRepository;

    public RestaurantRepositoryImpl(RestaurantDataAccessMapper restaurantDataAccessMapper, RestaurantJpaRepository restaurantJpaRepository) {
        this.restaurantDataAccessMapper = restaurantDataAccessMapper;
        this.restaurantJpaRepository = restaurantJpaRepository;
    }

    @Override
    public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
        List<UUID> restaurantProducts =
                restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
        Optional<List<RestaurantEntity>> restaurantEntities = restaurantJpaRepository
                .findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(),
                        restaurantProducts);
        return restaurantEntities.map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
    }
}
