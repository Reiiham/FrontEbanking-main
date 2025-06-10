package ma.ensa.ebankingver1.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
<<<<<<< HEAD
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.http.HttpHost;
=======
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "ma.ensa.ebankingver1.repository.elasticsearch",
        elasticsearchTemplateRef = "elasticsearchTemplate"
)
=======
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.cert.X509Certificate;

@Configuration
@EnableRetry
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
public class ElasticsearchConfig {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);

    @Value("${elasticsearch.host:localhost}")
<<<<<<< HEAD
    private String esHost;

    @Value("${elasticsearch.port:9200}")
    private int esPort;

    @Bean
    public RestClient restClient() {
        logger.info("Creating RestClient for Elasticsearch at {}:{}", esHost, esPort);
        return RestClient.builder(new HttpHost(esHost, esPort)).build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        logger.info("Creating ElasticsearchTransport");
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport elasticsearchTransport) {
        logger.info("Creating ElasticsearchClient");
        return new ElasticsearchClient(elasticsearchTransport);
    }

    @Bean(name = "elasticsearchTemplate")
    public ElasticsearchOperations elasticsearchTemplate(ElasticsearchClient elasticsearchClient) {
        logger.info("Creating ElasticsearchTemplate bean");
        return new ElasticsearchTemplate(elasticsearchClient);
    }

    @PostConstruct
    public void pingTest() {
        try {
            boolean status = elasticsearchClient().ping().value();
            logger.info("✅ Elasticsearch connected: {}", status);
        } catch (Exception e) {
            logger.error("❌ Elasticsearch connection failed: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to Elasticsearch", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (restClient() != null) {
                restClient().close();
                logger.info("✅ Elasticsearch RestClient closed");
            }
        } catch (Exception e) {
            logger.error("❌ Failed to close Elasticsearch RestClient: {}", e.getMessage());
        }
    }

    private ElasticsearchClient elasticsearchClient() {
        return elasticsearchClient(elasticsearchTransport(restClient()));
    }
=======
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.username:elastic}")
    private String username;

    @Value("${elasticsearch.password:bgKbWdFLbBE6ezgt4hjg}")
    private String password;

    @Bean
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public ElasticsearchClient elasticsearchClient() throws Exception {
        logger.info("Attempting to connect to Elasticsearch at {}:{}", host, port);

        // Disable SSL verification (for development only)
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                .build();

        // Set up credentials
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        // Build RestClient with HTTPS and credentials
        RestClient restClient = RestClient.builder(new HttpHost(host, port, "http"))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setSSLContext(sslContext)
                        .setDefaultCredentialsProvider(credentialsProvider))
                .build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);

        // Create index with mapping
        createCryptoTransactionsIndex(client);
        logger.info("Successfully connected to Elasticsearch and initialized index");
        return client;
    }

    private void createCryptoTransactionsIndex(ElasticsearchClient client) throws IOException {
        boolean indexExists = client.indices().exists(e -> e.index("crypto-transactions")).value();
        if (!indexExists) {
            logger.info("Creating crypto-transactions index");
            client.indices().create(c -> c
                    .index("crypto-transactions")
                    .mappings(m -> m
                            .properties("userId", p -> p.keyword(k -> k))
                            .properties("currency", p -> p.keyword(k -> k))
                            .properties("type", p -> p.keyword(k -> k))
                            .properties("amount", p -> p.double_(d -> d))
                            .properties("price", p -> p.double_(d -> d))
                            .properties("timestamp", p -> p.date(d -> d))
                    )
            );
        } else {
            logger.info("crypto-transactions index already exists");
        }
    }
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
}