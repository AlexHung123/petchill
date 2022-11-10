package com.alexhong.petchill.product.web;


import com.alexhong.petchill.product.entity.CategoryEntity;
import com.alexhong.petchill.product.service.CategoryService;
import com.alexhong.petchill.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    CategoryService  categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model){
        //TODO 1. find all the categories
        List<CategoryEntity> level1Categories = categoryService.getLevel1Categories();
        model.addAttribute("categories", level1Categories);


        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        //1. get the same lock if the name is same
        RLock lock = redisson.getLock("my-lock");

        //2. lock
        lock.lock(10, TimeUnit.SECONDS);
        try {
            Thread.sleep(30000);
            System.out.println("Locked successfully" + Thread.currentThread().getId());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println(Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }

    @GetMapping("/write")
    @ResponseBody
    public String writeValue(){

        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");

        String s = "";
        RLock rLock = readWriteLock.writeLock();
        try {
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue(){

        String writeValue1 = "";
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.readLock();

        try {
            rLock.lock();
            writeValue1 = redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }
        return writeValue1;
    }

    @GetMapping("/park")
    @ResponseBody
    public String park(){
        RSemaphore park = redisson.getSemaphore("park");
        try {
            park.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "ok";
    }


    @GetMapping("/go")
    @ResponseBody
    public String go(){
        RSemaphore park = redisson.getSemaphore("park");
        park.release();
        return "ok";
    }

    @GetMapping("/lockDoor")
    public String lockDock() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();
        return "Leave....";
    }

    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id") Long id){
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();
        return id + " class leave...";
    }


}
