package com.typeahead;

import com.typeahead.data.loader.ITypeaheadDataLoader;
import com.typeahead.redis.Autocompletion;
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
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;

@Slf4j
@SpringBootApplication
@PropertySource("classpath:typeahead.properties")
public class TypeaheadApplication implements CommandLineRunner {
	private final ApplicationContext appCxt;

	@Autowired
	public TypeaheadApplication(ApplicationContext appCtx) {
		this.appCxt = appCtx;
	}

	public static void main(String[] args) {
		SpringApplication.run(TypeaheadApplication.class, args);
	}

	@Bean
	RedisTemplate<String, Autocompletion> redisTemplate() {
		RedisTemplate<String, Autocompletion> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(new JedisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		return redisTemplate;
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
			Map<String, ITypeaheadDataLoader> map = appCxt.getBeansOfType(ITypeaheadDataLoader.class);
			ITypeaheadDataLoader dataLoader = (ITypeaheadDataLoader) map.values().toArray()[0];
			dataLoader.loadData();
		} catch (Exception e) {
			log.error("Error while loading data. Error --> " + e, e);
		}
	}
}
