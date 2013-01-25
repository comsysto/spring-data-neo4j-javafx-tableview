package com.comsysto;

import com.comsysto.config.Neo4jConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({Neo4jConfig.class, ScreensConfiguration.class})
public class CustomerAppConfiguration {

}
