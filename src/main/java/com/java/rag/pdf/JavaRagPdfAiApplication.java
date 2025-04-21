package com.java.rag.pdf;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JavaRagPdfAiApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(JavaRagPdfAiApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		// Load .env file
		Dotenv dotenv = Dotenv.configure().load();
		// Now you can use the environment variables
		String openaiApiKey = dotenv.get("OPENAI_API_KEY");

		System.out.println("OpenAI API Key Loaded: " + openaiApiKey);

	}
}
