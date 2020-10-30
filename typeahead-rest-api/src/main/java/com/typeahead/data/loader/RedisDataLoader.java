package com.typeahead.data.loader;

import com.typeahead.repository.RedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY_REDIS;

@Slf4j
@Component
@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_REDIS, matchIfMissing = true)
public class RedisDataLoader implements ITypeaheadDataLoader {
	private static final int DATA_FILES_COUNT = 10;
	private static final double DEFAULT_SCORE = 1.0;

	private RedisRepository redisRepo;

	@Autowired
	public RedisDataLoader(RedisRepository redisRepository) {
		this.redisRepo = redisRepository;
	}

	@Override
	public void loadData() {
		AtomicInteger numDocLoaded = new AtomicInteger();
		log.info("Starting Redis Data Loader...");
		for (int i = 1; i <= 1; i++) {
			String csvFileName = getCsvFileName(i);
			ClassPathResource resource = new ClassPathResource(csvFileName);
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
				br.lines().forEach(line -> {
					numDocLoaded.getAndIncrement();
					if(numDocLoaded.get() % 4_000 == 0){
						log.info("Loaded document count is " + numDocLoaded);
					}
					redisRepo.acceptPhrase(line, DEFAULT_SCORE);
				});
			} catch (Exception e) {
				log.error("Error while inserting data into Redis. Error --> " + e.getMessage(), e);
				log.info("Error after loading [" + numDocLoaded + "] docs!");
				return;
			}
		}
		log.info("Redis Data Loader completed loading [" + numDocLoaded + "] docs...!!! You can use the system now");
	}

	private String getCsvFileName(int index) {
		return "100K_names_" + index + ".csv";
	}
}
