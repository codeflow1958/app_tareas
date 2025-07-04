package com.umg.gestiontareas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gestionTareasAPI() {
        return new OpenAPI()
                .info(new Info().title("API de Gestión de Tareas")
                        .description("API para crear, gestionar y organizar tareas")
                        .version("v1.0.0"));
    }
}
