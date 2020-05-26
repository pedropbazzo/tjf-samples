package com.baeldung.springdoc.repository;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.baeldung.springdoc.model.Cd;

@Repository
public class CdRepository {

	private Map<Long, Cd> cds = new HashMap<>();

	public Optional<Cd> findById(long id) {
		return Optional.ofNullable(cds.get(id));
	}

	public void add(Cd cd) {
		cds.put(cd.getId(), cd);
	}

	public Collection<Cd> getCds() {
		return cds.values();
	}

	public Page<Cd> getCds(Pageable pageable) {
		int toSkip = pageable.getPageSize() * pageable.getPageNumber();
		List<Cd> result = cds.values().stream().skip(toSkip).limit(pageable.getPageSize()).collect(toList());

		return new PageImpl<>(result, pageable, cds.size());
	}
}
