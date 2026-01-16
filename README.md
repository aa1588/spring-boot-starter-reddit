# Building a Custom Spring Boot Starter: A Reddit API Client

Ever wondered how Spring Boot magically configures everything for you? How does adding a single dependency suddenly give you a fully configured database connection or REST client? The answer lies in **Spring Boot Starters** and **Auto-Configuration**.

In this guide, we'll build a real-world Spring Boot Starter that wraps the Reddit API, and along the way, you'll learn exactly how Spring Boot's auto-configuration magic works under the hood.

## What We're Building

We're creating a custom Spring Boot Starter that:
- Automatically configures a Reddit API client
- Allows developers to fetch posts, search Reddit, and get subreddit info
- Requires zero manual configuration (just add the dependency!)
- Can be customized through `application.properties`

## Project Structure

This is a multi-module Maven project with three modules:

```
spring-boot-starter-reddit/
├── reddit-spring-boot-autoconfigure/    # The brain - auto-configuration logic
├── reddit-spring-boot-starter/          # The convenience - pulls everything together
└── reddit-spring-boot-sample/           # Demo app showing how to use it
```

### Why Three Modules?

This follows Spring Boot's convention:

1. **autoconfigure module** - Contains the actual auto-configuration logic and the client implementation
2. **starter module** - A dependency aggregator that pulls in the autoconfigure module and any required dependencies
3. **sample module** - A demo application to test our starter

## Module 1: The Auto-Configuration Module

This is where the magic happens. Let's break it down piece by piece.

### The Reddit Client (`RedditClient.java`)

This is the actual service that talks to Reddit's API:

```java
public class RedditClient {
    private final RestClient restClient;
    private final RedditProperties properties;

    public List<RedditPost> getPosts(String subreddit) {
        // Fetches posts from Reddit's public JSON API
    }
    
    public List<RedditPost> search(String query) {
        // Searches Reddit
    }
}
```

Nothing special here - it's just a regular Java class that uses Spring's `RestClient` to call Reddit's API.

### Configuration Properties (`RedditProperties.java`)

This class defines what users can configure:

```java
@ConfigurationProperties(prefix = "reddit")
public class RedditProperties {
    private String baseUrl = "https://www.reddit.com";
    private String userAgent = "SpringBoot:RedditStarter:v1.0.0";
    private String defaultSubreddit = "java";
    private int defaultLimit = 25;
    // ... getters and setters
}
```

The `@ConfigurationProperties` annotation tells Spring Boot to bind properties starting with `reddit.*` from `application.properties` to this class.

Users can now customize the client like this:

```properties
reddit.user-agent=MyApp:v1.0.0 (by /u/myusername)
reddit.default-subreddit=programming
reddit.default-limit=50
```

### The Auto-Configuration Class (`RedditAutoConfiguration.java`)

This is the heart of our starter. Let's examine it carefully:

```java
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@EnableConfigurationProperties(RedditProperties.class)
public class RedditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestClient redditRestClient(RedditProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("User-Agent", properties.getUserAgent())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedditClient redditClient(RestClient redditRestClient, 
                                     RedditProperties properties) {
        return new RedditClient(redditRestClient, properties);
    }
}
```

Let's break down each annotation:

#### `@AutoConfiguration`
This marks the class as an auto-configuration class. Spring Boot will automatically discover and process this class during application startup.

#### `@ConditionalOnClass(RestClient.class)`
This is a **conditional annotation**. It means: "Only apply this auto-configuration if `RestClient` is on the classpath."

This is smart because:
- If someone doesn't have Spring Web on their classpath, this configuration won't even try to run
- It prevents errors and keeps your starter flexible

#### `@EnableConfigurationProperties(RedditProperties.class)`
This tells Spring Boot to enable and process our `RedditProperties` class, making it available for dependency injection.

#### `@ConditionalOnMissingBean`
This annotation on the `@Bean` methods means: "Only create this bean if the user hasn't already defined one."

This is crucial for customization! If a user wants to provide their own `RestClient` configuration, they can:

```java
@Configuration
public class MyCustomConfig {
    @Bean
    public RestClient redditRestClient() {
        // My custom configuration
        return RestClient.builder()
            .baseUrl("https://oauth.reddit.com")
            .defaultHeader("Authorization", "Bearer " + token)
            .build();
    }
}
```

Spring Boot will detect this bean and skip creating the default one. This is the "opinionated defaults with easy customization" philosophy in action!

### Registering the Auto-Configuration

For Spring Boot 3.x and 4.x, we need to register our auto-configuration in:

**`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`**
```
org.example.redditspringbootautoconfigure.RedditAutoConfiguration
```

For backward compatibility with Spring Boot 2.x, you can also include:

**`META-INF/spring.factories`**
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.example.redditspringbootautoconfigure.RedditAutoConfiguration
```

This tells Spring Boot: "Hey, when you're scanning for auto-configurations, include this class!"

### Important: Jackson 3.x Compatibility

If you're using Spring Boot 4.0, note that it uses Jackson 3.x which changed its package structure:

**Old (Jackson 2.x):**
```java
import com.fasterxml.jackson.databind.JsonNode;
```

**New (Jackson 3.x):**
```java
import tools.jackson.databind.JsonNode;
```

Update your `pom.xml` accordingly:
```xml
<dependency>
    <groupId>tools.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

## Module 2: The Starter Module

The starter module is beautifully simple. It has **no code** - just a `pom.xml` that pulls in dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.example</groupId>
        <artifactId>reddit-spring-boot-autoconfigure</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
</dependencies>
```

This is the module users will add to their projects. It's a convenience wrapper that says: "Give me everything I need to use the Reddit client."

## Module 3: The Sample Application

This demonstrates how easy it is to use our starter:

### 1. Add the Dependency

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>reddit-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Use the Auto-Configured Bean

```java
@RestController
@RequestMapping("/api/reddit")
public class RedditController {

    private final RedditClient redditClient;

    public RedditController(RedditClient redditClient) {
        this.redditClient = redditClient;  // Auto-injected!
    }

    @GetMapping("/posts/{subreddit}")
    public List<RedditPost> getPosts(@PathVariable String subreddit) {
        return redditClient.getPosts(subreddit);
    }

    @GetMapping("/search")
    public List<RedditPost> search(@RequestParam String q) {
        return redditClient.search(q);
    }
}
```

That's it! No configuration needed. The `RedditClient` is automatically available for injection.

### 3. Optional: Customize via Properties

```properties
spring.application.name=reddit-spring-boot-sample

# Customize the Reddit client
reddit.user-agent=MyAwesomeApp:v1.0.0 (by /u/myusername)
reddit.default-subreddit=programming
reddit.default-limit=50

server.port=8080
```

## Understanding Conditional Annotations

Spring Boot's conditional annotations are what make auto-configuration so powerful. Here are the most common ones:

### `@ConditionalOnClass`
```java
@ConditionalOnClass(RestClient.class)
```
"Only activate if this class is on the classpath."

Use case: Don't try to configure a database if JDBC isn't available.

### `@ConditionalOnMissingBean`
```java
@ConditionalOnMissingBean
public RestClient redditRestClient() { ... }
```
"Only create this bean if the user hasn't defined one."

Use case: Provide defaults but allow users to override.

### `@ConditionalOnProperty`
```java
@ConditionalOnProperty(name = "reddit.enabled", havingValue = "true", matchIfMissing = true)
```
"Only activate if this property has a specific value."

Use case: Allow users to completely disable your auto-configuration with `reddit.enabled=false`.

### `@ConditionalOnMissingClass`
```java
@ConditionalOnMissingClass("org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter")
```
"Only activate if this class is NOT on the classpath."

Use case: Provide different configurations based on what's available.

## Building and Running

### Build the Project

```bash
mvn clean install
```

This builds all three modules and installs them to your local Maven repository.

### Run the Sample Application

```bash
cd reddit-spring-boot-sample
java -jar target/reddit-spring-boot-sample-0.0.1-SNAPSHOT.jar
```

Or use Maven:

```bash
mvn spring-boot:run
```

### Test the API

```bash
# Get hot posts from r/java
curl http://localhost:8080/api/reddit/posts/java

# Get new posts from r/programming
curl http://localhost:8080/api/reddit/posts/programming/new?limit=10

# Search Reddit
curl http://localhost:8080/api/reddit/search?q=spring+boot

# Get subreddit info
curl http://localhost:8080/api/reddit/subreddit/java
```

## How Spring Boot Discovers Your Starter

When your application starts, Spring Boot:

1. **Scans the classpath** for `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` files
2. **Reads all auto-configuration classes** listed in these files
3. **Evaluates conditional annotations** on each class
4. **Applies configurations** that meet their conditions
5. **Creates beans** that aren't already defined by the user

This happens automatically - you don't need to use `@ComponentScan` or `@Import` to include your auto-configuration!

## Key Takeaways

1. **Separation of Concerns**: Keep auto-configuration logic separate from the starter dependency aggregator
2. **Conditional Configuration**: Use `@Conditional*` annotations to make your starter smart and flexible
3. **Opinionated Defaults**: Provide sensible defaults that work out of the box
4. **Easy Customization**: Use `@ConditionalOnMissingBean` to allow users to override your beans
5. **Configuration Properties**: Use `@ConfigurationProperties` to make your starter configurable
6. **No Credentials Needed**: This starter uses Reddit's public JSON API - no authentication required!

## Common Pitfalls

### 1. Wrong Package in Auto-Configuration Registration
Make sure the fully qualified class name in your `AutoConfiguration.imports` file matches your actual package structure.

### 2. Missing `@ConditionalOnMissingBean`
Without this, users can't override your beans, making your starter inflexible.

### 3. Jackson Version Mismatch
Spring Boot 4.0 uses Jackson 3.x (`tools.jackson.*`), not Jackson 2.x (`com.fasterxml.jackson.*`).

### 4. Forgetting to Enable Configuration Properties
Don't forget `@EnableConfigurationProperties(YourProperties.class)` on your auto-configuration class.

## Next Steps

Now that you understand how Spring Boot Starters work, you can:

- Create starters for your company's internal libraries
- Wrap third-party APIs with Spring Boot auto-configuration
- Build reusable components that "just work" when added to a project
- Contribute to open-source Spring Boot starters

## Resources

- [Spring Boot Auto-Configuration Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.auto-configuration)
- [Creating Your Own Auto-Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)
- [Reddit JSON API Documentation](https://github.com/reddit-archive/reddit/wiki/JSON)

## License

This project is for educational purposes. Feel free to use it as a template for your own Spring Boot Starters!

---

**Happy coding!** 

If you found this helpful, consider starring the repo and sharing it with others learning Spring Boot.
