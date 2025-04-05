package com.mobileapp.repository;


import com.mobileapp.model.Support;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportRepository extends JpaRepository<Support, Integer> {
    List<Support> findAllByOrderByTokenIdDesc();
}