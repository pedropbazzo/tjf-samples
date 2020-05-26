package com.baeldung.springdoc.controller;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.baeldung.springdoc.exception.CdNotFoundException;
import com.baeldung.springdoc.model.Cd;
import com.baeldung.springdoc.repository.CdRepository;

@RestController
@RequestMapping("/api/framework/v1/cd")
public class CdController {

    @Autowired
    private CdRepository repository;

    @GetMapping("/{id}")
    public Cd findById(@PathVariable long id) {
        return repository.findById(id)
            .orElseThrow(() -> new CdNotFoundException());
    }

    @GetMapping("/")
    public Collection<Cd> findCds() {
        return repository.getCds();
    }

    @GetMapping("/filter")
    public Page<Cd> filterCds(Pageable pageable) {
        return repository.getCds(pageable);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Cd updateCd(@PathVariable("id") final String id, @RequestBody final Cd cd) {
        return cd;
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Cd patchCd(@PathVariable("id") final String id, @RequestBody final Cd cd) {
        return cd;
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Cd postCd(@NotNull @Valid @RequestBody final Cd cd) {
        return cd;
    }

    @RequestMapping(method = RequestMethod.HEAD, value = "/")
    @ResponseStatus(HttpStatus.OK)
    public Cd headCd() {
        return new Cd();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public long deleteCd(@PathVariable final long id) {
        return id;
    }
}
