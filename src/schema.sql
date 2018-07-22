--database init script
CREATE DATABASE seckill;

use seckill;

CREATE TABLE seckill(
	`seckill_id` bigint NOT NULL AUTO_INCREMENT,
	`name`  	 varchar(120) NOT NULL,
	`number`  	 int NOT NULL,
	`start_time` DATETIME NOT NULL,
	`end_time`   DATETIME NOT NULL,
	`create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	PRIMARY KEY (seckill_id),
	key idx_start_time(start_time),
	key idx_end_time(end_time),
	key idx_create_time(create_time)			
)ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='seckill stock';

insert into 
	seckill (name, number, start_time, end_time)
values
	('1000元秒杀iphone6', 100, '2016-05-31 00:00:00', '2016-06-01 00:00:00'),
	('1000元秒杀ipad2', 200, '2016-05-31 00:00:00', '2016-06-01 00:00:00'),
	('1000元秒杀note4', 150, '2016-05-31 00:00:00', '2016-06-01 00:00:00'),
	('1000元秒杀macpro', 100, '2016-05-31 00:00:00', '2016-06-01 00:00:00');
	

--秒杀成功明细表
create table success_killed(
	`seckill_id` bigint NOT NULL,
	`user_phone` bigint NOT NULL,
	`state`      tinyint NOT NULL DEFAULT -1,
	`create_time` timestamp NOT NULL,
	PRIMARY KEY(seckill_id, user_phone),
	key idx_create_time(create_time)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;	