package com.typeahead;

import com.typeahead.data.loader.ITypeaheadDataLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Slf4j
@SpringBootApplication
@PropertySource("classpath:typeahead.properties")
public class TypeaheadApplication implements CommandLineRunner {
	private ITypeaheadDataLoader typeaheadDataLoader;

	@Autowired
	public TypeaheadApplication(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
										ITypeaheadDataLoader typeaheadDataLoader) {
		this.typeaheadDataLoader = typeaheadDataLoader;
	}

	public static void main(String[] args) {
		SpringApplication.run(TypeaheadApplication.class, args);
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
		bean.setValidationMessageSource(messageSource());
		return bean;
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:typeahead");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			typeaheadDataLoader.loadData();
		} catch (Exception e) {
			log.error("Error while loading data. Error --> " + e, e);
		}
	}
}
