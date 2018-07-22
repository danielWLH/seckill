package org.seckill.exception;

/**
 * 这个项目里面所有异常的父类
 * **/
public class SeckillException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public SeckillException(String message) {
		super(message);
	}
	
	public SeckillException(String message, Throwable cause){
		super(message, cause);
	}
}
