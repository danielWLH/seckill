package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SuccessKilled;


public interface SuccessKilledDao {
	/**
	 * insert kill detail, filter duplicate, return inserted row number, 0 means fail
	 * @param seckillId
	 * @param userPhone
     * @return
     */
	int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

	/**
	 * insert successKill entity into SuccessKilled
	 * @param seckillId
	 * @param userPhone
     * @return
     */
	SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);
	
}