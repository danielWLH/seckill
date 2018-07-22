package org.seckill.test.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

//配置spring和junit整合，使junit启动时加载springioc容器
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
//有了上面这两行，在启动junit的时候就会启动spring容器，并且验证spring和mybatis配置是否ok
public class SuccessKilledTest {
	
	//注入dao实现
	@Resource
	private SuccessKilledDao successKilledDao;
	
	@Test
	public void testInsertSuccessKilled() throws Exception{
		int insertCount = successKilledDao.insertSuccessKilled(1000L, 15903669153L);
		System.out.println(insertCount);
	}

	@Test
	public void testQueryByIdWithSeckill() throws Exception{
		SuccessKilled sk = successKilledDao.queryByIdWithSeckill(1000L, 15903669153L);
		System.out.println(sk.getSecKill());
		System.out.println(sk);
	}
}
