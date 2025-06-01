package com.clinicsalon.professional.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Professional Service API")
                        .description("API para gerenciamento de profissionais/funcionários da clínica/salão")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Clínica Salão")
                                .email("contato@clinicasalao.com")
                                .url("https://www.clinicasalao.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
