package org.example.redditspringbootsample;

import org.example.redditspringbootautoconfigure.RedditClient;
import org.example.redditspringbootautoconfigure.RedditClient.RedditPost;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reddit")
public class RedditController {

    private final RedditClient redditClient;

    public RedditController(RedditClient redditClient) {
        this.redditClient = redditClient;
    }

    @GetMapping("/posts/{subreddit}")
    public List<RedditPost> getPosts(@PathVariable String subreddit) {
        return redditClient.getPosts(subreddit);
    }

    @GetMapping("/posts/{subreddit}/{sort}")
    public List<RedditPost> getPostsSorted(
            @PathVariable String subreddit,
            @PathVariable String sort,
            @RequestParam(defaultValue = "25") int limit) {
        return redditClient.getPosts(subreddit, sort, limit);
    }

    @GetMapping("/search")
    public List<RedditPost> search(@RequestParam String q) {
        return redditClient.search(q);
    }

    @GetMapping("/subreddit/{name}")
    public Map<String, Object> getSubredditInfo(@PathVariable String name) {
        return redditClient.getSubredditInfo(name);
    }

    @GetMapping("/test")
    public String test() {
        try {
            List<RedditPost> posts = redditClient.getPosts("java", "hot", 5);
            return "Success! Got " + posts.size() + " posts";
        } catch (Exception e) {
            return "Error: " + e.getMessage() + " - " + e.getClass().getName();
        }
    }
}