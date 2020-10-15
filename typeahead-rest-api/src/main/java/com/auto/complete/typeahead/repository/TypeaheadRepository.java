package com.auto.complete.typeahead.repository;

import com.auto.complete.typeahead.domain.TrieEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeaheadRepository extends CrudRepository<TrieEntity, Long> {
}
