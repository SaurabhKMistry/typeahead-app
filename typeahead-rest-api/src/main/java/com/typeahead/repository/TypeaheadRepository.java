package com.typeahead.repository;

import com.typeahead.trie.TrieEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeaheadRepository extends CrudRepository<TrieEntity, Long> {
}
