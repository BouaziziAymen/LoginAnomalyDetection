package tn.devoteam.demo.detector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import tn.devoteam.demo.detector.config.LrConfig;

@EnableConfigurationProperties(LrConfig.class)
@SpringBootApplication
public class LoginAnomalyDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginAnomalyDetectorApplication.class, args);
	}

}
