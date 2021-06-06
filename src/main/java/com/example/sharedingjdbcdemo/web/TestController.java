package com.example.sharedingjdbcdemo.web;

import com.example.sharedingjdbcdemo.dao.TestDAO;
import com.example.sharedingjdbcdemo.dto.UserDTO;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@RestController()
@Log4j2
public class TestController {

    @Autowired
    private TestDAO testDAO;

    @GetMapping("test_add")
    public void test_add() {
        log.error("test_add");
        testDAO.addUser(new UserDTO(1, "T1", "teacher"));
        testDAO.addUser(new UserDTO(2, "S2", "student"));
    }

    @GetMapping("test_add2")
    public void test_add2() {
        log.error("test_add2");
        testDAO.addUser(new UserDTO(3, "E3", "expert"));
        testDAO.addUser(new UserDTO(4, "S4", "student"));
    }

    @SneakyThrows
    @GetMapping("test_list")
    public void test_list() {
        log.error("test_list");
        log.error("主库 {}", CompletableFuture.supplyAsync(() -> {
            try (HintManager manager = HintManager.getInstance()) {
                return testDAO.listUser();
            }
        }).get());
//        long time = System.currentTimeMillis();
//        IntStream.range(0, 1000).parallel().forEach((i) -> testDAO.listUser());
//        log.error("test_list执行用时：{} 毫秒", System.currentTimeMillis() - time);
    }
}
