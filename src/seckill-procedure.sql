-- 定义秒杀的存储过程
DELIMITER $$ -- 在console中把 ; 转换为 $$
-- in 入参， out 出参
-- row_count(); 返回上一次修改类操作的影响行数（insert, update, delete）
CREATE PROCEDURE `seckill`.`execute_seckill`
  (in v_seckill_id bigint, in v_phone bigint, in v_kill_time timestamp,
  out r_result int)
  BEGIN
    DECLARE insert_count int DEFAULT 0;
    START TRANSACTION;
      insert ignore into success_killed
       (seckill_id, user_phone, create_time)
       values (v_seckill_id, v_phone, v_kill_time);
      select row_count() into insert_count;
    IF(insert_count = 0) THEN
      ROLLBACK;
      SET r_result = -1;
    ELSEIF(insert_count = -2) THEN
      ROLLBACK;
      SET r_result = -2;
    ELSE
      update seckill
      set number = number - 1
      where seckill_id = v_seckill_id
        and end_time > v_kill_time
        and start_time < v_kill_time
        and number > 0;
      select row_count() into insert_count;
      IF(insert_count = 0) THEN
        ROLLBACK;
        set r_result = 0;
      ELSEIF(insert_count < 0) THEN
        ROLLBACK;
        set r_result = -2;
      ELSE
        COMMIT;
        set r_result = 1;
      END IF;
    END IF;
  END;
$$
-- 存储过程定义结束

DELIMITER ;
-- 定义变量
set @r_result = -3;
-- 执行procedure
call execute_seckill(1002, 15903669153, now(), @r_result);
-- 查看result
select r_result;