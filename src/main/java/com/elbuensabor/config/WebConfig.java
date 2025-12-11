package com.elbuensabor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

        private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

        @Value("${app.upload.dir:src/main/resources/static/img/}")
        private String uploadDir;

        @Value("${app.public.img-path:/img/}")
        private String publicImgPath;

        @Autowired
        private ActiveUserInterceptor activeUserInterceptor;

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Configurar para servir imágenes desde el directorio de upload
                String uploadLocation = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

                logger.info("📁 Configurando servicio de imágenes:");
                logger.info("   - uploadDir: {}", uploadDir);
                logger.info("   - uploadLocation URI: {}", uploadLocation);
                logger.info("   - publicImgPath: {}", publicImgPath);

                registry.addResourceHandler(publicImgPath + "**")
                                .addResourceLocations(uploadLocation)
                                .setCachePeriod(3600); // Cache por 1 hora

                logger.info("✅ ResourceHandler {}** configurado correctamente", publicImgPath);

                // También servir desde resources/static por defecto
                registry.addResourceHandler("/static/**")
                                .addResourceLocations("classpath:/static/")
                                .setCachePeriod(3600);

                logger.info("✅ ResourceHandler /static/** configurado correctamente");
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(activeUserInterceptor)
                                .addPathPatterns("/api/**") // Aplicar a todos los endpoints de API
                                .excludePathPatterns(
                                                "/api/auth/**", // Excluir login, register, etc.
                                                "/payment/**", // Excluir pagos (si son de terceros)
                                                "/webhooks/**", // Excluir webhooks (si son de terceros)
                                                "/api/*/debug" // Excluir rutas de debug
                                );
        }
}