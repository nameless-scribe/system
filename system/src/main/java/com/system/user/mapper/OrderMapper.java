package com.system.user.mapper;

import com.system.user.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Order order);

    List<Order> selectByUserId(@Param("userId") Long userId,
                               @Param("start") int start,
                               @Param("size") int size);

    int selectTotalCountByUserId(@Param("userId") Long userId);

    Order selectById(@Param("orderId") Long orderId);

    List<Order> selectAll(@Param("status") Integer status,
                          @Param("start") int start,
                          @Param("size") int size);

    int selectTotalCount(@Param("status") Integer status);

    int updateStatus(@Param("orderId") Long orderId,
                     @Param("status") Integer status);

    List<Order> selectSoldByOwnerId(@Param("ownerId") Long ownerId,
                                    @Param("start") int start,
                                    @Param("size") int size);

    int selectSoldTotalCountByOwnerId(@Param("ownerId") Long ownerId);

    int countByOrderAndOwner(@Param("orderId") Long orderId,
                             @Param("ownerId") Long ownerId);

    int updateStatusAndShipTime(@Param("orderId") Long orderId,
                                @Param("status") Integer status,
                                @Param("shipTime") java.util.Date shipTime);

    int updateStatusAndCompleteTime(@Param("orderId") Long orderId,
                                    @Param("status") Integer status,
                                    @Param("completeTime") java.util.Date completeTime);

    int updateReturnInfo(@Param("orderId") Long orderId,
                         @Param("status") Integer status,
                         @Param("returnReason") String returnReason,
                         @Param("returnAddress") String returnAddress);

    java.util.List<Long> selectIdsToAutoComplete();
}

