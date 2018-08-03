package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Created by linghui.wlh on 25/4/18.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //注入service依赖
    @Autowired // = @Resource = @Inject
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    //用于混淆md5
    private final String salt = "!@%*)(*&^werBVCω≈çâ≤";

    @Override
    public List<Seckill> getSeckillList(){
        long start = System.currentTimeMillis();
        logger.info("sec kill list start");
        List<Seckill> list = seckillDao.queryAll(0, 4);
        long end = System.currentTimeMillis();
        logger.info("sec kill list end: ", (end - start));
        return list;
    }


    @Override
    public Seckill getById(Long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点: 缓存优化
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            seckill = seckillDao.queryById(seckillId);
            if(seckill == null){
                //缓存和db都没查到,返回false
                return new Exposer(false, seckillId);
            }else{
                //缓存没查到,db查到,insert into 缓存
                redisDao.putSeckill(seckill);
            }
        }

        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();

        if(nowTime.getTime() < startTime.getTime() ||
                nowTime.getTime() > endTime.getTime()){
            return new Exposer(false, seckillId, nowTime.getTime(),
                    startTime.getTime(), endTime.getTime());
        }

        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1，开发团队达成一致的约定，明确标注事务方法的编程风格
     * 2，保证事务方法的执行时间尽可能短，不要穿插其他的网络操作，比如redis，cache，http等。保证这个方法功能集中，都是针对DB的。
     * 3，不是所有的方法都需要事务，比如只有一条修改操作，或者只读操作，都不需要事务控制。
     * **/
    public SeckillExecution executeSeckill(long seckillId, long userPhone,
                                           String md5) throws SeckillException, RepeatKillException,
            SeckillCloseException {

        Date now = new Date();

        try{
            if(md5 == null || !md5.equals(getMD5(seckillId))){
                throw new SeckillException("seckill data rewrite");
            }
            //优化过后的逻辑:首先记录购买行为,其次更新库存操作。如果更新库存出问题,抛异常,rollback;否则commit
            //1. 纪录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if(insertCount <= 0){
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            } else {
                //2. 减库存, 热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, now);
                if(updateCount <= 0){
                    //没有更新纪录，秒杀结束了, rollback
                    throw new SeckillCloseException("seckill is closed");
                }else{
                    //commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        } catch(SeckillCloseException e1) {
            throw e1;
        } catch(RepeatKillException e2) {
            throw e2;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            //所有编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error: " + e.getMessage());
        }
    }

    /**
     * 执行秒杀动作, 使用存储过程
     * @return
     */
    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5){
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId, SeckillStateEnum.DATA_REWRITE);
        }

        Date killTime = new Date();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("seckillId", seckillId);
        params.put("phone", userPhone);
        params.put("killTime", killTime);
        params.put("result", null);
        //执行存储过程,result会被赋值
        try {
            seckillDao.killByProcedure(params);
            Integer result = MapUtils.getInteger(params, "result", -2);
            if(result == 1){
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, sk);
            }else{
                return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
            }
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
        }
    }

    //generate md5 function
    private String getMD5(long seckillId){
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
