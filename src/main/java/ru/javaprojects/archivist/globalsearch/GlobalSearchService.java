package ru.javaprojects.archivist.globalsearch;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalSearchService {
    private final String query = """
                (SELECT CONCAT(d.decimal_number, ' document card') AS header, 'documents' AS group_name, 
                d.id AS entity_id, CONCAT('decimal number = ', d.decimal_number) AS match_value 
                FROM documents d WHERE UPPER(d.decimal_number) LIKE UPPER(CONCAT('%', :keyword, '%')) 
                AND d.auto_generated IS FALSE LIMIT :searchResultsLimit) 
                UNION
                (SELECT CONCAT(d.decimal_number, ' document card') AS header, 'documents' AS group_name, 
                d.id AS entity_id, CONCAT('name = ', d.name) AS match_value 
                FROM documents d WHERE UPPER(d.name) LIKE UPPER(CONCAT('%', :keyword, '%')) 
                AND d.auto_generated IS FALSE LIMIT :searchResultsLimit) 
                UNION
                (SELECT CONCAT(d.decimal_number, ' document card') AS header, 'documents' AS group_name, 
                d.id AS entity_id, CONCAT('inventory number = ', d.inventory_number) AS match_value 
                FROM documents d WHERE UPPER(d.inventory_number) LIKE UPPER(CONCAT('%', :keyword, '%')) 
                AND d.auto_generated IS FALSE LIMIT :searchResultsLimit) 
                UNION
                (SELECT CONCAT(c.name, ' change notice card') AS header, 'change-notices' AS group_name, 
                c.id AS entity_id, CONCAT('name = ', c.name) AS match_value 
                FROM change_notices c WHERE UPPER(c.name) LIKE UPPER(CONCAT('%', :keyword, '%')) 
                AND c.auto_generated IS FALSE LIMIT :searchResultsLimit) 
                UNION
                (SELECT CONCAT(d.decimal_number, ' album card') AS header, 'albums' AS group_name, 
                a.id AS entity_id, CONCAT('main document = ', d.decimal_number) AS match_value 
                FROM albums a JOIN documents d ON a.document_id = d.id 
                WHERE UPPER(d.decimal_number) LIKE UPPER(CONCAT('%', :keyword, '%')) LIMIT :searchResultsLimit)
                UNION
                (SELECT CONCAT(c.name, ' company card') AS header, 'companies' AS group_name, 
                c.id AS entity_id, CONCAT('name = ', c.name) AS match_value 
                FROM companies c WHERE UPPER(c.name) LIKE UPPER(CONCAT('%', :keyword, '%')) LIMIT :searchResultsLimit)
                UNION
                (SELECT CONCAT(d.name, ' department card') AS header, 'departments' AS group_name, 
                d.id AS entity_id, CONCAT('name = ', d.name) AS match_value 
                FROM departments d WHERE UPPER(d.name) LIKE UPPER(CONCAT('%', :keyword, '%')) LIMIT :searchResultsLimit)
                LIMIT :searchResultsLimit
                """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Value("${search-results-limit}")
    private Integer searchResultsLimit;

    public List<SearchResult> getSearchResults(String keyword) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("keyword", keyword)
                .addValue("searchResultsLimit", searchResultsLimit);
        return jdbcTemplate.query(query, namedParameters, BeanPropertyRowMapper.newInstance(SearchResult.class));
    }
}
