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
	private ITypeaheadDataLoader dataLoader;

	@Autowired
	public TypeaheadApplication(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
										ITypeaheadDataLoader dataLoader) {
		this.dataLoader = dataLoader;
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
		ReloadableResourceBundleMessageSource msgSrc = new ReloadableResourceBundleMessageSource();
		msgSrc.setBasename("classpath:typeahead");
		msgSrc.setDefaultEncoding("UTF-8");
		return msgSrc;
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			dataLoader.loadData();
		} catch (Exception e) {
			log.error("Error while loading data. Error --> " + e, e);
		}
	}
}
