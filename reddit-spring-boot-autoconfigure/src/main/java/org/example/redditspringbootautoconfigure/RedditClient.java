package org.example.redditspringbootautoconfigure;

import tools.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedditClient {

    private static final Logger log = LoggerFactory.getLogger(RedditClient.class);

    private final RestClient restClient;
    private final RedditProperties properties;

    public RedditClient(RestClient restClient, RedditProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /**
     * Get posts from a subreddit
     */
    public List<RedditPost> getPosts(String subreddit) {
        return getPosts(subreddit, "hot", properties.getDefaultLimit());
    }

    /**
     * Get posts from a subreddit with sorting
     * @param subreddit - subreddit name (without r/)
     * @param sort - hot, new, top, rising
     * @param limit - number of posts to fetch (max 100)
     */
    public List<RedditPost> getPosts(String subreddit, String sort, int limit) {
        String path = "/r/" + subreddit + "/" + sort + ".json";

        log.debug("Fetching posts from: {}{}", properties.getBaseUrl(), path);

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("limit", limit)
                            .build())
                    .header("User-Agent", properties.getUserAgent())
                    .retrieve()
                    .body(JsonNode.class);

            return parsePosts(response);

        } catch (Exception e) {
            log.error("Error fetching posts from r/{}", subreddit, e);
            return new ArrayList<>();
        }
    }

    /**
     * Search Reddit
     */
    public List<RedditPost> search(String query) {
        return search(query, properties.getDefaultLimit());
    }

    /**
     * Search Reddit with limit
     */
    public List<RedditPost> search(String query, int limit) {
        log.debug("Searching Reddit for: {}", query);

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("q", query)
                            .queryParam("limit", limit)
                            .build())
                    .header("User-Agent", properties.getUserAgent())
                    .retrieve()
                    .body(JsonNode.class);

            return parsePosts(response);

        } catch (Exception e) {
            log.error("Error searching Reddit for: {}", query, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get subreddit information
     */
    public Map<String, Object> getSubredditInfo(String subreddit) {
        String path = "/r/" + subreddit + "/about.json";

        log.debug("Fetching subreddit info: {}", subreddit);

        try {
            JsonNode response = restClient.get()
                    .uri(path)
                    .header("User-Agent", properties.getUserAgent())
                    .retrieve()
                    .body(JsonNode.class);

            return parseSubredditInfo(response);

        } catch (Exception e) {
            log.error("Error fetching subreddit info for r/{}", subreddit, e);
            return new HashMap<>();
        }
    }

    // Helper methods

    private List<RedditPost> parsePosts(JsonNode root) {
        List<RedditPost> posts = new ArrayList<>();

        if (root == null || !root.has("data")) {
            return posts;
        }

        JsonNode children = root.path("data").path("children");

        for (JsonNode child : children) {
            JsonNode data = child.path("data");

            RedditPost post = new RedditPost();
            post.setTitle(data.path("title").asText());
            post.setAuthor(data.path("author").asText());
            post.setSubreddit(data.path("subreddit").asText());
            post.setScore(data.path("score").asInt());
            post.setNumComments(data.path("num_comments").asInt());
            post.setUrl(data.path("url").asText());
            post.setPermalink("https://www.reddit.com" + data.path("permalink").asText());
            post.setCreatedUtc(data.path("created_utc").asLong());
            post.setSelfText(data.path("selftext").asText());
            post.setThumbnail(data.path("thumbnail").asText());

            posts.add(post);
        }

        return posts;
    }

    private Map<String, Object> parseSubredditInfo(JsonNode root) {
        Map<String, Object> info = new HashMap<>();

        if (root == null || !root.has("data")) {
            return info;
        }

        JsonNode data = root.path("data");

        info.put("name", data.path("display_name").asText());
        info.put("title", data.path("title").asText());
        info.put("description", data.path("public_description").asText());
        info.put("subscribers", data.path("subscribers").asInt());
        info.put("activeUsers", data.path("active_user_count").asInt());
        info.put("created", data.path("created_utc").asLong());
        info.put("url", "https://www.reddit.com" + data.path("url").asText());

        return info;
    }

    // Inner class for Reddit Post
    public static class RedditPost {
        private String title;
        private String author;
        private String subreddit;
        private int score;
        private int numComments;
        private String url;
        private String permalink;
        private long createdUtc;
        private String selfText;
        private String thumbnail;

        // Getters and Setters

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getSubreddit() {
            return subreddit;
        }

        public void setSubreddit(String subreddit) {
            this.subreddit = subreddit;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public int getNumComments() {
            return numComments;
        }

        public void setNumComments(int numComments) {
            this.numComments = numComments;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPermalink() {
            return permalink;
        }

        public void setPermalink(String permalink) {
            this.permalink = permalink;
        }

        public long getCreatedUtc() {
            return createdUtc;
        }

        public void setCreatedUtc(long createdUtc) {
            this.createdUtc = createdUtc;
        }

        public String getSelfText() {
            return selfText;
        }

        public void setSelfText(String selfText) {
            this.selfText = selfText;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }
    }
}