package com.elbuensabor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

        @Value("${app.upload.dir:src/main/resources/static/img/}")
        private String uploadDir;

        @Autowired
        private ActiveUserInterceptor activeUserInterceptor;

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Configurar para servir imágenes desde el directorio de upload
                String uploadPath = Paths.get(uploadDir).toUri().toString();

                registry.addResourceHandler("/img/**")
                                .addResourceLocations(uploadPath)
                                .setCachePeriod(3600); // Cache por 1 hora

                // También servir desde resources/static por defecto
                registry.addResourceHandler("/static/**")
                                .addResourceLocations("classpath:/static/")
                                .setCachePeriod(3600);
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(activeUserInterceptor)
                                .addPathPatterns("/api/**") // Aplicar a todos los endpoints de API
                                .excludePathPatterns(
                                                "/api/auth/**", // Excluir login, register, etc. (Aunque el interceptor
                                                                // ya
                                                                // los ignoraría)
                                                "/payment/**", // Excluir pagos (si son de terceros)
                                                "/webhooks/**", // Excluir webhooks (si son de terceros)
                                                "/api/*/debug" // Excluir rutas de debug
                                );
        }
}