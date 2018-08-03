package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Description:
 * @Author linghui.wlh
 * @Date 2018-08-01 14:08
 */
public class RedisDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private JedisPool jedisPool;

    public RedisDao(String ip, int port){
        jedisPool = new JedisPool(ip, port);
    }

    /**
     * 需要序列化(反序列化)的对象的schema
     */
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    /**
     * redis操作
     * @param seckillId
     * @return
     */
    public Seckill getSeckill(long seckillId){
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:" + seckillId;
                //由于redis并没有实现内部序列化操作
                //序列化步骤: get -> byte[] -> 反序列化 -> Object(Seckill)
                //使用java自带的序列化工具效率太低(implements serializable),
                // 所以使用开源的(https://github.com/eishay/jvm-serializers/wiki) protostuff
                //protostuff: 序列化的标准: pojo
                byte[] bytes = jedis.get(key.getBytes());
                //缓存获取到
                if(bytes != null){
                    //使用schema创造一个空对象
                    Seckill seckill = schema.newMessage();
                    //调用这句话以后,seckill对象就已经被赋值了。这个过程比java自带反序列化占用空间少10-5倍,时间快100倍
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    return seckill;
                }
            }finally{
                jedis.close();
            }
        }catch(Exception e){
            logger.error("exception_:{}", seckillId, e);
        }
        return null;
    }

    /**
     * 把seckill对象序列化,插入redis中
     * @param seckill
     * @return
     */
    public String putSeckill(Seckill seckill){
        //set Object -> 序列化 -> byte[] -> redis
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:" + seckill.getSeckillId();
                //序列化,使用linkedBuffer的意思是,当你的对象非常大,会有一个缓存的过程。
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                    LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存,单位秒
                int timeout = 60 * 60;
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            }finally{
                jedis.close();
            }
        }catch(Exception e){
            logger.error("exception_:{}", seckill, e);
        }
        return null;
    }
}
