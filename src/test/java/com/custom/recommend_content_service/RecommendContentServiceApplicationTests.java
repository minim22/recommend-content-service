package com.custom.recommend_content_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.kafka.consumer.group-id=test-group"
})
class RecommendContentServiceApplicationTests {

    @Test
    void contextLoads() {
        // Context 로드 테스트
    }

}
