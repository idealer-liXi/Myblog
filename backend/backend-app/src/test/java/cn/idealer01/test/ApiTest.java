package cn.idealer01.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test() {
//        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
//        ops.set("name", "张三");
//        System.out.println(ops.get("age"));
    }


}
