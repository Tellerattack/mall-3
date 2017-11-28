package com.scoprion.mall.wx.service.ticket;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.scoprion.constant.Constant;
import com.scoprion.enums.CommonEnum;
import com.scoprion.mall.domain.Ticket;
import com.scoprion.mall.domain.TicketExt;
import com.scoprion.mall.domain.TicketSnapshot;
import com.scoprion.mall.domain.TicketUser;
import com.scoprion.mall.wx.mapper.WxTicketMapper;
import com.scoprion.mall.wx.mapper.WxTicketSnapshotMapper;
import com.scoprion.mall.wx.pay.util.WxUtil;
import com.scoprion.result.BaseResult;
import com.scoprion.result.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 * @author by hmy
 * @created on 2017/11/2/002.
 */

@Service
public class WxTicketServiceImpl implements WxTicketService {

    @Autowired
    private WxTicketMapper wxTicketMapper;

    @Autowired
    private WxTicketSnapshotMapper wxTicketSnapshotMapper;


    /**
     * 用户优惠券列表
     *
     * @param pageNo
     * @param pageSize
     * @param wxCode
     * @return
     */
    @Override
    public PageResult findByUserId(Integer pageNo, Integer pageSize, String wxCode) {
        PageHelper.startPage(pageNo, pageSize);
        String userId = WxUtil.getOpenId(wxCode);
        Page<TicketExt> page = wxTicketMapper.findByUserId(userId);
        List<TicketExt> list = page.getResult();
        Date currentTime = new Date();
        list.forEach(item -> {
            Date startDate = item.getStartDate();
            Date endDate = item.getEndDate();
            if (startDate.after(currentTime)) {
                item.setExpire(CommonEnum.UN_START.getCode());
            }
            if (endDate.before(currentTime)) {
                item.setExpire(CommonEnum.EXPIRE.getCode());
            }
            if (startDate.compareTo(currentTime) < 0 && startDate.compareTo(
                    currentTime) == 0 && currentTime.compareTo(endDate) < 0 && currentTime.compareTo(endDate) == 0) {
                item.setExpire(CommonEnum.ON_THE_WAY.getCode());
            }
        });
        return new PageResult(page);
    }

    /**
     * 领取优惠券
     *
     * @param wxCode
     * @param ticketId
     * @return
     */
    @Override
    public BaseResult getTicket(Long ticketId, String wxCode) {
        String userId = WxUtil.getOpenId(wxCode);
        int count = wxTicketMapper.findByTicketIdAndUserId(ticketId, userId);
        if (count > 0) {
            return BaseResult.error("add_error", "已经领取过了");
        }
        //查询优惠券详情
        Ticket ticket = wxTicketMapper.findById(ticketId);
        if (ticket.getEndDate().before(new Date()) || CommonEnum.NORMAL.getCode().equals(ticket.getStatus())) {
            return BaseResult.error("add_error", "优惠券已过期");
        }
        //判断优惠券是否限量
        if (CommonEnum.NORMAL.getCode().equals(ticket.getNumLimit())) {
            if (ticket.getNum() == 0) {
                return BaseResult.error("add_error", "领取失败,优惠券已经领完了");
            }
            TicketSnapshot snapshot = new TicketSnapshot();
            BeanUtils.copyProperties(ticket, snapshot);
            snapshot.setTicketId(ticket.getId());
            int result = wxTicketSnapshotMapper.add(snapshot);
            if (result > 0) {
                TicketUser ticketUser = new TicketUser();
                ticketUser.setNum(1);
                ticketUser.setUserId(userId);
                ticketUser.setSnapshotId(snapshot.getId());
                ticketUser.setStatus(CommonEnum.UN_NORMAL.getCode());
                int ticketNum = wxTicketMapper.updateTicketNum(ticketId);
                int addResult = wxTicketMapper.addTicketUser(ticketUser);
                if (addResult > 0 && ticketNum > 0) {
                    return BaseResult.success("领取成功");
                }
            }
        }
        TicketSnapshot snapshot = new TicketSnapshot();
        BeanUtils.copyProperties(ticket, snapshot);
        snapshot.setTicketId(ticket.getId());
        int result = wxTicketSnapshotMapper.add(snapshot);
        if (result > 0) {
            TicketUser ticketUser = new TicketUser();
            ticketUser.setNum(1);
            ticketUser.setSnapshotId(snapshot.getId());
            ticketUser.setUserId(userId);
            ticketUser.setStatus(CommonEnum.UN_NORMAL.getCode());
            int addResult = wxTicketMapper.addTicketUser(ticketUser);
            if (addResult > 0) {
                return BaseResult.success("领取成功");
            }
        }
        return BaseResult.error("add_error", "领取失败");
    }


    /**
     * 查询优惠券所有列表
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findAll(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        Page<Ticket> page = wxTicketMapper.findAll();
        return new PageResult(page);
    }


}
