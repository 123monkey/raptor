package com.ppdai.framework.raptor.spring;

import com.ppdai.framework.raptor.spring.converter.RaptorJacksonMessageConverter;
import com.ppdai.framework.raptor.spring.converter.RaptorMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yinzuolong
 */
@Configuration
public class RaptorAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean(RaptorMessageConverter.class)
    public RaptorMessageConverter createRaptorMessageConverter() {
        return new RaptorJacksonMessageConverter();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
