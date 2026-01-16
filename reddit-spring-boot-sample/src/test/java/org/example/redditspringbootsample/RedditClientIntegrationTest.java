package org.example.redditspringbootsample;

import org.example.redditspringbootautoconfigure.RedditClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedditClientIntegrationTest {

    @Autowired
    private RedditClient redditClient;

    @Test
    void shouldFetchPostsFromSubreddit() {
        // When
        List<RedditClient.RedditPost> posts = redditClient.getPosts("java", "hot", 5);

        // Then
        assertThat(posts).isNotNull();
        // Note: Reddit API might return empty list or posts depending on availability
        // We just verify the client doesn't throw exceptions
    }

    @Test
    void shouldSearchReddit() {
        // When
        List<RedditClient.RedditPost> posts = redditClient.search("java", 5);

        // Then
        assertThat(posts).isNotNull();
    }

    @Test
    void shouldGetSubredditInfo() {
        // When
        Map<String, Object> info = redditClient.getSubredditInfo("java");

        // Then
        assertThat(info).isNotNull();
    }
}
