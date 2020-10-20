package com.auto.complete.typeahead;

import com.auto.complete.typeahead.util.ElasticSearchDataLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Slf4j
@SpringBootApplication
@PropertySource("classpath:typeahead.properties")
public class TypeaheadApplication implements CommandLineRunner {
    private final ApplicationContext appContext;

    @Autowired
    private ElasticSearchDataLoader dataLoader;

    @Autowired
    public TypeaheadApplication(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:typeahead");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    public static void main(String[] args) {
        SpringApplication.run(TypeaheadApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            dataLoader.loadDataInElasticSearch();
        } catch(Exception e){
            log.error("Could not load data. Error while loading --> " + e, e);
        }
    }
}
