package com.alexhong.petchill.product;

import com.alexhong.petchill.product.entity.BrandEntity;
import com.alexhong.petchill.product.service.BrandService;
import com.alexhong.petchill.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.*;
import java.util.*;

@Slf4j
@SpringBootTest
class PetchillProductApplicationTests {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    BrandService brandService;
    
    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void redisson(){
        System.out.println(redissonClient);
    }

    @Test
    public void testStringRedisTemplate(){
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        stringStringValueOperations.set("hello", "world_" + UUID.randomUUID().toString());
        String hello = stringStringValueOperations.get("hello");
        System.out.println(hello);
    }

    @Test
    void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("apple");
//        brandService.save(brandEntity);
//        System.out.println("save successfully");

        List<BrandEntity> brand_id = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        brand_id.forEach((item)->{
            System.out.println(item);
        });
    }


    @Test
    void textUpload(){
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.beimei());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        cfg.useHttpsDomains = false;
//...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
        String accessKey = "IPvCzxr12b49oxzJtucZ54-jfqthxm6jPzA8mjkm";
        String secretKey = "U4cuuqoR-DSZBF21_ZkTUV-PThmoXVnzzm36ZvTo";
        String bucket = "petchill";
//默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;
        try {
            //byte[] uploadBytes = "hello qiniu cloud".getBytes("utf-8");
            InputStream inputStream = new FileInputStream("/Users/alexhung/Desktop/test.png");
            ByteArrayInputStream byteInputStream=new ByteArrayInputStream(IOUtils.toByteArray(inputStream));
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(byteInputStream,key,upToken,null, null);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
            }
        } catch (UnsupportedEncodingException ex) {
            //ignore
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public class TreeNode{
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode(){}

        TreeNode(int val){
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right){
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    @Test
    void test1(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("Full path: {}", Arrays.asList(catelogPath));
    }


    @Test
    void test2(){
        TreeNode root = new TreeNode(1);
        ArrayList<Integer> integers = new ArrayList<>();
        Stack<TreeNode> stacks = new Stack<>();
        stacks.push(root);

        while (!stacks.empty()){
            TreeNode pop = stacks.pop();
            integers.add(pop.val);
            if(pop.right != null){
                stacks.push(pop.right);
            }

            if(pop.left != null){
                stacks.push(pop.left);
            }
        }

    }

}
