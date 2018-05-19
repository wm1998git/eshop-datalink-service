package com.wm.eshop.datalink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SpringBootApplication
// 将本应用作为一个微服务注册到Eureka Server上去
@EnableEurekaClient
// feign声明式的服务调用，类似于rpc风格的服务调用，【这种风格非常好且常用】，因它默认集成了ribbon做负载均衡，且默认集成了eureka做服务发现
@EnableFeignClients
public class EshopDataLinkServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EshopDataLinkServiceApplication.class, args);
	}

	@Bean
	public JedisPool jedisPool() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(100);
		config.setMaxIdle(5);
		config.setMaxWaitMillis(1000 * 10); // 连接Redis Pool最多等待10秒
		config.setTestOnBorrow(true);

		// 生产环境Redis的twemproxy的写主集群,ip和端口号同eshop-detail01机器的/usr/local/twemproxy-master/twemproxy-0.4.0/conf/nutcracker.yml配置
		return new JedisPool(config, "192.168.1.103", 1111);
	}

}