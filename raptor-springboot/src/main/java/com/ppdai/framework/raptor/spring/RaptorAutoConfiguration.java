package com.ppdai.framework.raptor.spring;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppdai.framework.raptor.spring.converter.RaptorJacksonMessageConverter;
import com.ppdai.framework.raptor.spring.converter.RaptorMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @author yinzuolong
 */
@Configuration
public class RaptorAutoConfiguration implements ApplicationContextAware{

    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean(RaptorMessageConverter.class)
    public RaptorMessageConverter createRaptorMessageConverter() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().applicationContext(applicationContext).build().configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        return new RaptorJacksonMessageConverter(objectMapper);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
