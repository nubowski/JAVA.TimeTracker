package ru.nubowski.timeTracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for clean-up operations. The properties are populated from the application
 * properties file using the prefix "cleanup".
 */
@Configuration
@ConfigurationProperties(prefix = "cleanup")
public class CleanupProperties {
    private int retentionPeriod;
    private String cronExpression;

    /**
     * Returns the configured retention period.
     *
     * @return the retention period
     */
    public int getRetentionPeriod() {
        return retentionPeriod;
    }

    /**
     * Sets the retention period.
     *
     * @param retentionPeriod the retention period
     */
    public void setRetentionPeriod(int retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
    }

    /**
     * Returns the configured cron expression for scheduled clean-up tasks.
     *
     * @return the cron expression
     */
    public String getCronExpression() {
        return cronExpression;
    }

    /**
     * Sets the cron expression for scheduled clean-up tasks.
     *
     * @param cronExpression the cron expression
     */
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }


}
