package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Product;
import com.ds.project.app_context.models.User;
import com.ds.project.app_context.repositories.OrderDetailRepository;
import com.ds.project.app_context.repositories.OrderRepository;
import com.ds.project.common.entities.dto.TopCustomerDTO;
import com.ds.project.common.entities.dto.TopProductDTO;
import com.ds.project.common.entities.dto.UserDto;
import com.ds.project.common.entities.dto.response.ProductResponse;
import com.ds.project.common.mapper.ProductMapper;
import com.ds.project.common.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;

    private final ProductMapper productMapper;
    private final UserMapper userMapper;   // ⭐ thay CustomerMapper

    // -----------------------
    // ⭐ Top Customers
    // -----------------------
    public List<TopCustomerDTO> getTopCustomers(LocalDateTime start, LocalDateTime end, int limit) {

        List<Object[]> result = orderRepository.findTopCustomersDelivered(start, end);

        return result.stream()
                .map(r -> {
                    User user = (User) r[0];
                    UserDto userDto = userMapper.mapToDto(user);

                    int totalOrders = ((Number) r[1]).intValue();
                    double totalSpent = ((Number) r[2]).doubleValue();

                    return new TopCustomerDTO(userDto, totalOrders, totalSpent);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
    public List<TopProductDTO> getTopProducts(LocalDateTime start, LocalDateTime end, int limit) {
        List<Object[]> result = orderDetailRepository.findTopProductsDelivered(start, end);

        return result.stream()
                .map(r -> {
                    Product product = (Product) r[0];
                    ProductResponse productResponse = productMapper.mapToDto(product);
                    int totalQuantity = ((Number) r[1]).intValue();
                    double totalRevenue = ((Number) r[2]).doubleValue();
                    return new TopProductDTO(productResponse, totalQuantity, totalRevenue);
                })
                .limit(limit)
                .collect(Collectors.toList());

    }

}



