package com.chatnova.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.chatnova.api.controller.ChatControllers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatNovaApplicationTests {
    @Autowired
    private ChatControllers controllers;

    @Test
    void applicationStartsWithHealthyApi() {
        assertThat(controllers.health().status()).isEqualTo("ok");
    }
}
