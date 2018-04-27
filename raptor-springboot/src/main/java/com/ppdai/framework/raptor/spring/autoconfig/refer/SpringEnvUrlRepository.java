package com.ppdai.framework.raptor.spring.autoconfig.refer;

import com.ppdai.framework.raptor.refer.repository.AbstractUrlRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.env.Environment;

public class SpringEnvUrlRepository extends AbstractUrlRepository {

    private Environment environment;

    @Setter
    @Getter
    private String keyPrefix = "";

    public SpringEnvUrlRepository(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getUrlString(String urlKey) {
        return environment.getProperty(this.keyPrefix + urlKey);
    }
}
