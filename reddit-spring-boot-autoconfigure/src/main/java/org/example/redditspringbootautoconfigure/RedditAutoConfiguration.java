package org.example.redditspringbootautoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;


@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@EnableConfigurationProperties(RedditProperties.class)
public class RedditAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RedditAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public RestClient redditRestClient(RedditProperties properties) {
        log.info("Configuring Reddit RestClient with user-agent: {}", properties.getUserAgent());
        
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("User-Agent", properties.getUserAgent())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedditClient redditClient(RestClient redditRestClient, RedditProperties properties) {
        log.info("Creating RedditClient bean with base URL: {}", properties.getBaseUrl());
        log.info("Default subreddit: r/{}", properties.getDefaultSubreddit());
        
        return new RedditClient(redditRestClient, properties);
    }
}