package org.seckill.test.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by linghui.wlh on 25/4/18.
 */
//配置spring和junit整合，使junit启动时加载springioc容器
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({
    "classpath:spring/spring-dao.xml",
    "classpath:spring/spring-service.xml"})
//有了上面这两行，在启动junit的时候就会启动spring容器，并且验证spring和mybatis配置是否ok
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void testGetSeckillList() throws Exception{
        List<Seckill> list = seckillService.getSeckillList();

        logger.info("list = {}", list);
    }

    @Test
    public void testGetById() throws Exception{
        Long id = 1000L;
        Seckill seckill = seckillService.getById(id);
        logger.info("object = {}", seckill);
    }

    @Test
    public void testExportSeckillUrl() throws Exception{
        long id = 1001L;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposer = {} ", exposer);
    }

    @Test
    public void testExecuteSeckill() throws Exception{
        long id = 1000;
        long phone = 13838383838L;
        String md5 = "f7c8d8db8c05f8b46e70ba5e0d205a3c";

        try{
            SeckillExecution exe = seckillService.executeSeckill(id, phone, md5);
            logger.info("result = {}", exe);
        } catch(RepeatKillException e) {
            logger.error(e.getMessage());
        } catch(SeckillCloseException e) {
            logger.error(e.getMessage());
        }
    }
}
