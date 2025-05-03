package com.quodex.snipr.repositories;

import com.quodex.snipr.dto.UrlMappingDTO;
import com.quodex.snipr.models.UrlMapping;
import com.quodex.snipr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    UrlMapping findByShortUrl(String shortUrl);
    List<UrlMapping> findByUser(User user);

    boolean existsByShortUrl(String shortUrl);

}
