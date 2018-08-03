package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by linghui.wlh on 24/4/18.
 */
public interface SeckillDao {
    /**
     * 减少库存， if return value > 1, means updated rows num
     * @param seckillId
     * @param killTime
     * @return
     */
    int reduceNumber(@Param("seckillId") Long seckillId, @Param("killTime")Date killTime);


    /**
     * query by id
     * @param seckillId
     * @return
     */
    Seckill queryById(long seckillId);

    /**
     * query all
     * @param offset
     * @param limit
     * @return
     */
    List<Seckill> queryAll(@Param("offset")int offset, @Param("limit")int limit);

    /**
     * 使用procedure执行秒杀
     * @param params
     */
    void killByProcedure(Map<String, Object> params);

}
