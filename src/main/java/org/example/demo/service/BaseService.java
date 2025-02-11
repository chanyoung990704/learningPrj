package org.example.demo.service;

import java.util.List;

public interface BaseService<T> {

    Long save(T object);

    T findById(Long id);

    List<T> findAll();

    Long deleteById(Long id);

    Long update(Long id, T object);

}
