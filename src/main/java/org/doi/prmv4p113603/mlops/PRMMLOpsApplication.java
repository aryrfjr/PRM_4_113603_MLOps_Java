package org.doi.prmv4p113603.mlops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * NOTE: here Spring Framework will build up an entire system called the
 *  Spring Application Context by:
 *  - Scanning the code (e.g., via @ComponentScan).
 *  - Instantiating beans (like @Service, @Repository, @Controller).
 *  - Injecting dependencies (via @Autowired, constructor injection).
 *  - Managing lifecycle (e.g., initialization, shutdown hooks).
 */
@SpringBootApplication
public class PRMMLOpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PRMMLOpsApplication.class, args);
    }
}
