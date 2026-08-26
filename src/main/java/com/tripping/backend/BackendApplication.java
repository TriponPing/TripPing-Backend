package com.tripping.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	// 서버가 켜질 때 DB 연결을 테스트하는 코드
	@Bean
	public CommandLineRunner testDatabaseConnection(DataSource dataSource) {
		return args -> {
			try (Connection connection = dataSource.getConnection()) {
				System.out.println("==================================================");
				System.out.println("🎉 Supabase OK : " + connection.getMetaData().getURL());
				System.out.println("==================================================");
			} catch (Exception e) {
				System.err.println("❌ Supabase 연결 실패: " + e.getMessage());
			}
		};
	}
}