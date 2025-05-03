package com.quodex.snipr.repositories;

import com.quodex.snipr.dto.ClickEventDTO;
import com.quodex.snipr.models.ClickEvent;
import com.quodex.snipr.models.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    List<ClickEventDTO> findByUrlMappingAndClickDateBetween(UrlMapping urlMapping, LocalDateTime start, LocalDateTime end);
    List<ClickEventDTO> findByUrlMappingInAndClickDateBetween(List<UrlMapping> urlMapping, LocalDateTime start, LocalDateTime end);
}
