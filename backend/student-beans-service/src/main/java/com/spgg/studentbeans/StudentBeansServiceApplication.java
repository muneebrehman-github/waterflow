package com.spgg.studentbeans;

import com.spgg.studentbeans.config.StudentBeansProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StudentBeansProperties.class)
public class StudentBeansServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentBeansServiceApplication.class, args);
    }
}
