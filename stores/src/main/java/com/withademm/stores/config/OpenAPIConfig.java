package com.withademm.stores.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI storesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stores E-Commerce API")
                        .description("REST API documentation for Products and Categories with CRUD, pagination, and validation.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Adem Mekonnen")
                                .email("adem.se2010@gmail.com")));
    }
}