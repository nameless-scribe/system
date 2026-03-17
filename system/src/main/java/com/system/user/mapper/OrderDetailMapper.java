package com.system.user.mapper;

import com.system.user.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    void batchInsert(@Param("orderDetails") List<OrderDetail> orderDetails);

    List<OrderDetail> getByOrderId(@Param("orderId") Long orderId);
}

