package ru.nubowski.timeTracker;

import jdk.jfr.Enabled;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.nubowski.timeTracker.config.CleanupProperties;

@SpringBootApplication
@EnableScheduling 	// for schedule tasks
@EnableConfigurationProperties(CleanupProperties.class) // not sure if TRUE by default
public class TimeTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeTrackerApplication.class, args);
	}

}




