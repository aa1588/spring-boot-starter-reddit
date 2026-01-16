package org.example.redditspringbootautoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = RedditProperties.REDDIT_PREFIX)
public class RedditProperties {

    public static final String REDDIT_PREFIX = "reddit";

    /**
     * Reddit API base URL
     */
    private String baseUrl = "https://www.reddit.com";

    /**
     * User agent for Reddit API requests (required by Reddit)
     */
    private String userAgent = "SpringBoot:RedditStarter:v1.0.0 (by /u/springboot)";

    /**
     * Connection timeout in milliseconds
     */
    private int connectTimeout = 5000;

    /**
     * Read timeout in milliseconds
     */
    private int readTimeout = 5000;

    /**
     * Default subreddit to use if none specified
     */
    private String defaultSubreddit = "java";

    /**
     * Default limit for posts fetched
     */
    private int defaultLimit = 25;

    // Getters and Setters

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getDefaultSubreddit() {
        return defaultSubreddit;
    }

    public void setDefaultSubreddit(String defaultSubreddit) {
        this.defaultSubreddit = defaultSubreddit;
    }

    public int getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }
}