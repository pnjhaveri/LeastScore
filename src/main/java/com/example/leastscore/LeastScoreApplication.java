package com.example.leastscore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LeastScoreApplication {

	public static void main(String[] args) {
		var databaseUrl = System.getenv("DATABASE_URL");
		if (databaseUrl != null && !databaseUrl.isBlank() && !databaseUrl.startsWith("jdbc")) {
			var withoutScheme = databaseUrl.replaceFirst("^postgres(ql)?://", "");
			var atIndex = withoutScheme.indexOf('@');
			var userInfo = withoutScheme.substring(0, atIndex);
			var colonIdx = userInfo.indexOf(':');
			var username = colonIdx >= 0 ? userInfo.substring(0, colonIdx) : userInfo;
			var password = colonIdx >= 0 ? userInfo.substring(colonIdx + 1) : "";
			var hostPortDb = withoutScheme.substring(atIndex + 1);

			System.setProperty("spring.datasource.url", "jdbc:postgresql://" + hostPortDb);
			System.setProperty("spring.datasource.username", username);
			System.setProperty("spring.datasource.password", password);
		}

		SpringApplication.run(LeastScoreApplication.class, args);
	}

}
