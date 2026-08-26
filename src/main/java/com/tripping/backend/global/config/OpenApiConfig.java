package com.tripping.backend.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tripPingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trip Ping API")
                        .description("여행 루트 기록 및 관광 트렌드 분석 플랫폼 API 문서")
                        .version("v0.1"));
    }
}

/*
 * 사용법:
 * 1. 이 클래스는 별도 설정 없이도 컨트롤러를 자동 스캔합니다.
 * 2. 각 컨트롤러 클래스 위에 @Tag(name = "화면명")을 붙이면
 *    Swagger UI에서 API 스펙 표와 동일한 그룹으로 묶여서 보입니다.
 *
 *    예)
 *    @Tag(name = "마이페이지")
 *    @RestController
 *    @RequestMapping("/users/me")
 *    public class MyPageController { ... }
 *
 * 3. 서버 실행 후 http://localhost:8080/swagger-ui.html 접속하면 문서가 보입니다.
 */
