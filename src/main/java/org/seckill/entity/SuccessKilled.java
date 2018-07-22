package org.seckill.entity;

import java.util.Date;

public class SuccessKilled {
	private long seckillId;
	
	private long userPhone;
	
	private short state;
	
	private Date createTime;
	
	//one to many relationship
	private Seckill secKill;
	
	@Override
	public String toString() {
		return "SuccessKilled [seckillId=" + seckillId + ", userPhone="
				+ userPhone + ", state=" + state + ", createTime=" + createTime
				+ "]";
	}

	public long getSeckillId() {
		return seckillId;
	}

	public void setSeckillId(long seckillId) {
		this.seckillId = seckillId;
	}

	public long getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(long userPhone) {
		this.userPhone = userPhone;
	}

	public short getState() {
		return state;
	}

	public void setState(short state) {
		this.state = state;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Seckill getSecKill() {
		return secKill;
	}

	public void setSecKill(Seckill secKill) {
		this.secKill = secKill;
	}
	
}
