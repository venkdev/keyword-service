package com.sellics.keyword;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static com.sellics.keyword.constant.ApplicationConstant.*;

@SpringBootApplication
public class KeywordServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeywordServiceApplication.class, args);
    }

    @Value("${amazon.autocomplete.baseUri}")
    private String baseUri;

    @Value("${amazon.autocomplete.searchAlias}")
    private String searchAlias;

    @Value("${amazon.autocomplete.mid}")
    private String mid;

    @Bean
    @Qualifier("autoCompleteUri")
    public UriComponents autoCompleteUri() {

        return UriComponentsBuilder.newInstance()
                .scheme(HTTPS)
                .host(baseUri)
                .queryParam(SEARCH_ALIAS, searchAlias)
                .queryParam(MID, mid)
                .queryParam(PREFIX, KEYWORD_IN_BRACES)
                .build();

    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
