package com.example.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import za.ac.cput.marketplace.MarketplaceApplication;

@SpringBootTest(
        classes = MarketplaceApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:student_marketplace_test;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password="
        }
)
class MarketplaceApplicationTests {

    @Test
    void contextLoads() {
    }

}