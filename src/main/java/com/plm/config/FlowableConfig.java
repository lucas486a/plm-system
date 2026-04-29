package com.plm.config;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowableConfig {

    /**
     * Configure Flowable engine with custom settings.
     * BPMN process definitions are auto-deployed from classpath:/processes/*.bpmn20.xml
     * via Flowable's Spring Boot auto-configuration.
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> engineConfigurer() {
        return configuration -> {
            configuration.setActivityFontName("Arial");
            configuration.setLabelFontName("Arial");
            configuration.setAnnotationFontName("Arial");
        };
    }
}
