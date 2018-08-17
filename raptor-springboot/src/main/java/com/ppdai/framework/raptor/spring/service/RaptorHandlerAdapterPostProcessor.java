package com.ppdai.framework.raptor.spring.service;

import com.ppdai.framework.raptor.spring.converter.RaptorMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 将RaptorHandlerMethodProcessor设置到RequestMappingHandlerAdapter中，优先处理参数和返回值
 *
 * @author yinzuolong
 */
@Slf4j
public class RaptorHandlerAdapterPostProcessor implements BeanPostProcessor {

    @Autowired
    private RaptorMessageConverter raptorMessageConverter;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (RequestMappingHandlerAdapter.class.isAssignableFrom(ClassUtils.getUserClass(bean))) {
            RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) bean;
            List<Object> advices = getRequestResponseBodyAdviceByReflect(adapter);
            RaptorHandlerMethodProcessor raptorHandlerMethodProcessor = new RaptorHandlerMethodProcessor(raptorMessageConverter,advices);

            ArrayList<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(adapter.getArgumentResolvers());
            argumentResolvers.add(0, raptorHandlerMethodProcessor);
            adapter.setArgumentResolvers(argumentResolvers);

            ArrayList<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>(adapter.getReturnValueHandlers());
            returnValueHandlers.add(0, raptorHandlerMethodProcessor);
            adapter.setReturnValueHandlers(returnValueHandlers);
        }
        return bean;
    }

    private List<Object> getRequestResponseBodyAdviceByReflect(RequestMappingHandlerAdapter adapter) {
        try {
            Field field = adapter.getClass().getDeclaredField("requestResponseBodyAdvice");
            //设置对象的访问权限，保证对private的属性的访问
            field.setAccessible(true);
            return (List<Object>) field.get(adapter);
        } catch (Exception e) {
            log.warn("getRequestResponseBodyAdviceByReflect failed :" + e.getMessage());
            return new ArrayList<>();
        }
    }

}
