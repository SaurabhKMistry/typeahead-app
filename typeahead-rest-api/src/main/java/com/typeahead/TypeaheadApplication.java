package com.typeahead;

import com.typeahead.data.loader.AbstractTypeaheadDataLoaderBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;

import static com.typeahead.common.TypeaheadConstants.TYPEAHEAD_POWERED_BY_REDIS;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;
import static com.typeahead.redis.RedisPropertyKeys.REDIS_HOST;
import static com.typeahead.redis.RedisPropertyKeys.REDIS_PORT;
import static java.lang.Integer.parseInt;

@Slf4j
@SpringBootApplication
@PropertySource("classpath:typeahead.properties")
public class TypeaheadApplication implements CommandLineRunner {
	private final ApplicationContext appCxt;
	private Environment environment;

	@Autowired
	public TypeaheadApplication(ApplicationContext appCtx, Environment environment) {
		this.environment = environment;
		this.appCxt = appCtx;
	}

	public static void main(String[] args) {
		SpringApplication.run(TypeaheadApplication.class, args);
	}

	@Bean
	@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_REDIS)
	public JedisConnectionFactory connectionFactory() {
		String host = environment.getProperty(REDIS_HOST, "localhost");
		String port = environment.getProperty(REDIS_PORT, "6379");

		RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
		standaloneConfig.setHostName(host);
		standaloneConfig.setPort(parseInt(port));

		return new JedisConnectionFactory(standaloneConfig);
	}

	@Bean
	@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_REDIS)
	public StringRedisTemplate redisTemplate(JedisConnectionFactory connectionFactory) {
		StringRedisTemplate redisTemplate = new StringRedisTemplate();
		redisTemplate.setConnectionFactory(connectionFactory);
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
			// because of cyclic dependency issue in spring, getting data loader from application context
			// instead of using @Autowired
			AbstractTypeaheadDataLoaderBase dataLoader = getDataLoaderFromApplicationContext();
			dataLoader.loadData();
		} catch (Exception e) {
			log.error("Error while loading data. Error --> " + e, e);
		}
	}

	private AbstractTypeaheadDataLoaderBase getDataLoaderFromApplicationContext() {
		Map<String, AbstractTypeaheadDataLoaderBase> map = appCxt.getBeansOfType(AbstractTypeaheadDataLoaderBase.class);
		return (AbstractTypeaheadDataLoaderBase) map.values().toArray()[0];
	}
}
