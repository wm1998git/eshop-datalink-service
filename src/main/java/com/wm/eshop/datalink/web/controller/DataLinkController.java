package com.wm.eshop.datalink.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import com.alibaba.fastjson.JSONObject;
import com.wm.eshop.datalink.service.EshopProductService;

@RestController
public class DataLinkController {

	@Autowired
	private EshopProductService eshopProductService;
	@Autowired
	private JedisPool jedisPool;

	@RequestMapping("/product")
	@ResponseBody
	public String getProduct(Long productId) {

		// 1、先读本地的ehcache，但是我们这里就不做了，因为之前都演示过了，大家自己做就可以了

		// 2、读redis主集群
		Jedis jedis = jedisPool.getResource();
		String dimProductJSON = jedis.get("dim_product_" + productId);// 由聚合服务：eshop-dataaggr-service服务写入到Redis的key

		// 3、如果从Redis主集群也没获取到商品数据，则调用依赖服务获取商品完整数据(商品基本信息+商品属性+商品规格)
		if (dimProductJSON == null || "".equals(dimProductJSON)) {
			String productDataJSON = eshopProductService.findProductById(productId);// 调用依赖服务获取商品数据

			if (productDataJSON != null && !"".equals(productDataJSON)) {
				JSONObject productDataJSONObject = JSONObject.parseObject(productDataJSON);

				String productPropertyDataJSON = eshopProductService.findProductPropertyByProductId(productId);// 调用依赖服务获取商品属性数据
				if (productPropertyDataJSON != null && !"".equals(productPropertyDataJSON)) {
					productDataJSONObject.put("product_property", JSONObject.parse(productPropertyDataJSON));
				}

				String productSpecificationDataJSON = eshopProductService.findProductSpecificationByProductId(productId);// 调用依赖服务获取商品规格数据
				if (productSpecificationDataJSON != null && !"".equals(productSpecificationDataJSON)) {
					productDataJSONObject.put("product_specification", JSONObject.parse(productSpecificationDataJSON));
				}

				// 4、将商品完整数据设置给redis的主集群
				jedis.set("dim_product_" + productId, productDataJSONObject.toJSONString());

				// 5、将商品完整数据返回给nginx应用层的本地缓存
				return productDataJSONObject.toJSONString();
			}
		}

		return "";
	}

}
