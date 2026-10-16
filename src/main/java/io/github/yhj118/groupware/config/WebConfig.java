package io.github.yhj118.groupware.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

// 사진 저장 폴더 uploads가 외부에 있으므로 웹 주소와 연결하기 위한 설정(src 폴더 내에 없으므로 외부로 간주)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path uploadDirectory = Paths.get("uploads").toAbsolutePath().normalize();
        String uploadPath = uploadDirectory.toUri().toString();

        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
