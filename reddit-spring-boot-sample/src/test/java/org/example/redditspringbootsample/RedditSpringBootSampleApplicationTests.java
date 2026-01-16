package org.example.redditspringbootsample;

import org.example.redditspringbootautoconfigure.RedditClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedditSpringBootSampleApplicationTests {

    @Autowired
    private RedditClient redditClient;

    @Test
    void contextLoads() {
        assertThat(redditClient).isNotNull();
    }

    @Test
    void redditClientBeanIsAutoConfigured() {
        assertThat(redditClient).isNotNull();
    }
}
