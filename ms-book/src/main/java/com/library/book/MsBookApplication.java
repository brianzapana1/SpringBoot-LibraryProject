package com.library.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.library.book","http"})
public class MsBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsBookApplication.class, args);
	}
}
