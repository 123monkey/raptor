package com.ppdai.framework.raptor.spring.service;

import com.ppdai.framework.raptor.rpc.RaptorServiceInterceptor;
import com.ppdai.framework.raptor.spring.converter.RaptorMessageConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Import({RaptorHandlerAdapterPostProcessor.class,
        RaptorHandlerMethodProcessor.class})
@Configuration
public class RaptorServiceAutoConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private RaptorMessageConverter raptorMessageConverter;

    @Autowired
    private ObjectProvider<List<RaptorServiceInterceptor>> handlerInterceptors;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //先增加RaptorContextInitHandlerInterceptor，其他拦截器之前先初始化RaptorContext
        registry.addInterceptor(new RaptorContextInitHandlerInterceptor());

        List<RaptorServiceInterceptor> handlerInterceptorList = handlerInterceptors.getIfAvailable();
        if (CollectionUtils.isEmpty(handlerInterceptorList)) {
            return;
        }
        handlerInterceptorList.sort(new AnnotationAwareOrderComparator());
        for (RaptorServiceInterceptor raptorServiceInterceptor : handlerInterceptorList) {
            registry.addInterceptor(new RaptorHandlerInterceptorAdapter(raptorServiceInterceptor));
        }
    }

    @Bean
    public RaptorHandlerExceptionResolver raptorHandlerExceptionResolver(){
        return new RaptorHandlerExceptionResolver(raptorMessageConverter);
    }

    @Configuration
    @ConditionalOnClass(AbstractEndpoint.class)
    static class ActuatorEndpointConfig implements BeanClassLoaderAware {

        private ClassLoader classLoader;

        @Bean
        @ConditionalOnBean(RequestMappingHandlerMapping.class)
        public RaptorProtoFilesEndpoint createRaptorProtoFilesEndpoint(RequestMappingHandlerMapping requestMappingHandlerMapping) {
            RaptorProtoFilesEndpoint raptorProtoFilesEndpoint = new RaptorProtoFilesEndpoint(requestMappingHandlerMapping);
            raptorProtoFilesEndpoint.setClassLoader(classLoader);
            return raptorProtoFilesEndpoint;
        }

        @Override
        public void setBeanClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }
    }

}
