package org.seckill.service;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

import java.util.List;

/**
 * Created by linghui.wlh on 25/4/18.
 * 业务接口，要站在使用者的角度来设计接口
 * 从三个方面来考虑：1，方法定义的粒度；2，参数；3，返回类型
 */
public interface SeckillService {
    /**
     * query all seckill records
     * */
    List<Seckill> getSeckillList();

    /**
     * query single seckill record
     * **/
    Seckill getById(Long seckillId);

    /**
     * output seckill interface url
     * 秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     * **/
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行seckill动作
     * **/
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException;

}
