package com.scoprion.mall.wx.mapper;

import com.github.pagehelper.Page;
import com.scoprion.mall.domain.Ticket;
import com.scoprion.mall.domain.TicketUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author by Administrator
 * @created on 2017/11/2/002.
 */

@Mapper
public interface WxTicketMapper {

    /**
     * 用户优惠券列表
     *
     * @param userId
     * @return
     */
    Page<Ticket> findByUserId(@Param("userId") Long userId);

    /**
     * 判断优惠卷使用时间(useDate)
     * @param userId
     * @param ticketId
     * @return
     */
    TicketUser findByTicketId(@Param("userId") Long userId, @Param("ticketId") Long ticketId);
    /**
     * 根据Id查询优惠券详情
     *
     * @param id
     * @return
     */
    Ticket findById(@Param("id") Long id);

}
