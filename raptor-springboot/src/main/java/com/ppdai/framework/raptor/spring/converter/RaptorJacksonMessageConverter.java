package com.ppdai.framework.raptor.spring.converter;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * @author yinzuolong
 */
public class RaptorJacksonMessageConverter extends MappingJackson2HttpMessageConverter implements RaptorMessageConverter {

    public RaptorJacksonMessageConverter() {
        super(Jackson2ObjectMapperBuilder.json().build().configure(MapperFeature.AUTO_DETECT_GETTERS, false));
    }

    public RaptorJacksonMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
