package org.example.springoauth2resourceserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
@EnableJpaAuditing
public class SpringOauth2ResourceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringOauth2ResourceServerApplication.class, args);
    }

}
