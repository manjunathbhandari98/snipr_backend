package com.quodex.snipr.service;

import com.quodex.snipr.dto.ClickEventDTO;
import com.quodex.snipr.dto.UrlMappingDTO;
import com.quodex.snipr.models.ClickEvent;
import com.quodex.snipr.models.UrlMapping;
import com.quodex.snipr.models.User;
import com.quodex.snipr.repositories.ClickEventRepository;
import com.quodex.snipr.repositories.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UrlMappingService {

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private ClickEventRepository clickEventRepository;

    /**
     * Creates a new shortened URL entry for a given original URL and user.
     * Ensures the generated short URL is unique.
     *
     * @param originalUrl The original long URL to be shortened
     * @param user        The authenticated user who created the short URL
     * @return A DTO containing URL mapping details
     */
    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateUniqueShortUrl(); // Ensure no duplicate
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setClickCount(0); // Ensure initial click count is 0
        UrlMapping savedUrlMapping = urlMappingRepository.save(urlMapping);
        return convertToDto(savedUrlMapping);
    }

    /**
     * Converts a UrlMapping entity to its corresponding DTO for response.
     *
     * @param urlMapping The entity fetched from DB
     * @return The DTO to return to the frontend
     */
    private UrlMappingDTO convertToDto(UrlMapping urlMapping) {
        UrlMappingDTO urlMappingDTO = new UrlMappingDTO();
        urlMappingDTO.setId(urlMapping.getId());
        urlMappingDTO.setOriginalUrl(urlMapping.getOriginalUrl());
        urlMappingDTO.setShortUrl(urlMapping.getShortUrl());
        urlMappingDTO.setClickCount(urlMapping.getClickCount());
        urlMappingDTO.setUsername(urlMapping.getUser().getUsername());
        return urlMappingDTO;
    }

    /**
     * Generates a unique short URL by checking the database for collisions.
     * If a collision is found, retries until a unique code is generated.
     *
     * @return A unique short URL string
     */
    private String generateUniqueShortUrl() {
        String shortUrl;
        do {
            shortUrl = generateShortUrl();
        } while (urlMappingRepository.existsByShortUrl(shortUrl));
        return shortUrl;
    }

    /**
     * Generates a random 8-character string using alphanumeric characters.
     * Used to create short URLs.
     *
     * @return Random 8-char alphanumeric string
     */
    private String generateShortUrl() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        Random random = new Random();
        StringBuilder shortUrl = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }
        return shortUrl.toString();
    }

    // Retrieves a list of shortened URLs created by a specific user and returns them as DTOs
    public List<UrlMappingDTO> getUrlByUser(User user) {
        // Fetch all UrlMapping entities associated with the given user from the database,
        // convert each entity to a UrlMappingDTO using 'convertToDto()', and collect them into a List
        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDto) // convert each UrlMapping entity to UrlMappingDTO
                .collect(Collectors.toList()); // collect results into a list
    }


    // Retrieves click event statistics for a given short URL between two dates, grouped by date
    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
        // Look up the UrlMapping entity by the short URL
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);

        // Proceed only if the short URL was found
        if (urlMapping != null) {

            // Fetch all ClickEvent entities associated with this URL and within the given time range
            return clickEventRepository
                    .findByUrlMappingAndClickDateBetween(urlMapping, start, end).stream()

                    // Group the click events by their date (ignoring the time portion)
                    .collect(Collectors.groupingBy(
                            click -> click.getClickDate().atStartOfDay().toLocalDate(), // group by date only
                            Collectors.counting() // count number of clicks per date
                    ))

                    // Convert the grouped Map<LocalDate, Long> into a List<ClickEventDTO>
                    .entrySet().stream().map(entry -> {
                        ClickEventDTO clickEventDTO = new ClickEventDTO();
                        clickEventDTO.setClickDate(entry.getKey()); // set the date
                        clickEventDTO.setCount(entry.getValue());   // set the click count
                        return clickEventDTO;
                    })

                    // Collect the result into a list
                    .collect(Collectors.toList());
        }

        // Return null if no matching short URL was found
        return null;
    }

    /**
     * Retrieves the total number of clicks grouped by date for all URLs created by a specific user
     * within a given date range.
     *
     * @param user The user whose URL click events should be counted.
     * @param start The start date of the date range (inclusive).
     * @param end The end date of the date range (inclusive).
     * @return A map where the key is the date and the value is the number of clicks on that day.
     */
    public Map<LocalDate, Long> getTotalClicksByuserAndDate(User user, LocalDate start, LocalDate end) {
        // Fetch all URL mappings created by the given user
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);

        // Fetch all click events for the user's URLs between the start and end dates
        List<ClickEventDTO> clickEvents = clickEventRepository
                .findByUrlMappingInAndClickDateBetween(
                        urlMappings,
                        start.atStartOfDay(),                // Convert LocalDate to LocalDateTime at start of day
                        end.plusDays(1).atStartOfDay()       // Include full 'end' day by using next day's start time
                );

        // Group the click events by date and count how many clicks occurred on each date
        return clickEvents.stream()
                .collect(Collectors.groupingBy(
                        click -> click.getClickDate().atStartOfDay().toLocalDate(), // Group by just the date part
                        Collectors.counting() // Count how many clicks per date
                ));
    }

    /**
     * Retrieves the original URL corresponding to a shortened URL.
     * Also increments the click count and logs a new click event.
     *
     * @param shortUrl The shortened URL identifier.
     * @return The UrlMapping object if found; otherwise null.
     */
    public UrlMapping getOriginalUrl(String shortUrl) {
        // Find the URL mapping by short URL code
        UrlMapping url = urlMappingRepository.findByShortUrl(shortUrl);

        // If the mapping exists, increment click count and log a click event
        if (url != null) {
            url.setClickCount(url.getClickCount() + 1); // Increment click count
            urlMappingRepository.save(url);             // Save the updated count

            // Create a new ClickEvent entry
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setClickDate(LocalDateTime.now()); // Set current timestamp
            clickEvent.setUrlMapping(url);                // Associate the click event with the URL
            clickEventRepository.save(clickEvent);        // Save the click event to DB
        }

        // Return the original URL mapping
        return url;
    }

}