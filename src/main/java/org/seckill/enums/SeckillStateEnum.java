package org.seckill.enums;
/**
 * 使用枚举来表示数据字典
 * **/
public enum SeckillStateEnum {
	
	SUCCESS (1, "miao sha cheng gong"),
	END(0, "miao sha jie shu"),
	REPEAT_KILL(-1, "chong fu miao sha"),
	INNER_ERROR(-2, "xi tong yi chang"),
	DATA_REWRITE(-3, "shu ju cuan gai");
	
	private int state;
	
	private String stateInfo;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getStateInfo() {
		return stateInfo;
	}
	
	SeckillStateEnum(int state, String stateInfo){
		this.state = state;
		this.stateInfo = stateInfo;
	}

	public void setStateInfo(String stateInfo) {
		this.stateInfo = stateInfo;
	}
	
	public static SeckillStateEnum stateOf(int index){
		for(SeckillStateEnum state: values()){
			if(state.getState() == index){
				return state;
			}
		}
		return null;
	}
}
