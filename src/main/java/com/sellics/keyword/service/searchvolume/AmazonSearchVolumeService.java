package com.sellics.keyword.service.searchvolume;

import com.sellics.keyword.datatransferobject.AutoCompleteResponseDTO;
import com.sellics.keyword.datatransferobject.SuggestionDTO;
import com.sellics.keyword.datatransferobject.SuggestionMatchDTO;
import com.sellics.keyword.datatransferobject.SuggestionMetaDTO;
import com.sellics.keyword.service.mapper.SearchVolumeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sellics.keyword.constant.ApplicationConstant.SPACE;

@Service
public class AmazonSearchVolumeService implements SearchVolumeService {

    RestTemplate restTemplate;

    UriComponents autoCompleteUri;

    private static final Logger LOG = LoggerFactory.getLogger(AmazonSearchVolumeService.class);

    public AmazonSearchVolumeService(RestTemplate restTemplate,
                                     @Qualifier("autoCompleteUri") UriComponents autoCompleteUri) {
        this.restTemplate = restTemplate;
        this.autoCompleteUri = autoCompleteUri;
    }

    @Override
    public int estimateSearchVolume(String keyword) {
        SuggestionMatchDTO searchMatch = getSearchMatch(keyword.toLowerCase());
        if (null == searchMatch)
            return 0;
        return calculateEstimate(searchMatch);
    }

    private static int calculateEstimate(SuggestionMatchDTO attribute) {

        int numberOfTries = attribute.getMatchedPage();
        double scoringFactor = numberOfTries * 2 + ((double) attribute.getMatchedIdx() / 10) * 2;
        //y=100-x^2;
        double score = 100 - Math.pow(scoringFactor, 2);
        return score > 0 ? (int) score : 0;

    }

    private SuggestionMatchDTO getSearchMatch(String keyword) {
        String searchTerm = "" + keyword.charAt(0);
        for (int i = 0; i < 7; i++) {
            LOG.info("Search term - " + searchTerm);
            AutoCompleteResponseDTO response = restTemplate.getForObject(autoCompleteUri.toUriString(),
                    AutoCompleteResponseDTO.class, searchTerm);
            if (null == response || null == response.getSuggestions() || response.getSuggestions().isEmpty())
                return null;

            SuggestionMatchDTO suggestionMatch = checkForMatch(keyword, searchTerm, response.getSuggestions());
            if (suggestionMatch.isCompleteMatch()) {
                LOG.info("Matched suggestion - " + suggestionMatch);
                suggestionMatch.setMatchedPage(i);
                return suggestionMatch;
            }

            if (suggestionMatch.getMatchSoFar().length() < keyword.length())
                searchTerm = suggestionMatch.getMatchSoFar() + keyword.charAt(suggestionMatch.getMatchSoFar().length());
            else if (searchTerm.length() < keyword.length())
                searchTerm = searchTerm + keyword.charAt(searchTerm.length());
            else
                return null;
        }
        return null;

    }

    private SuggestionMatchDTO checkForMatch(String keyword, String searchTerm, List<SuggestionDTO> suggestions) {
        String[] keywordArrSplit = keyword.split(SPACE);
        List<SuggestionMetaDTO> filteredSuggestions = SearchVolumeMapper.populateSuggestionMetadata(suggestions);
        SuggestionMatchDTO suggestion = new SuggestionMatchDTO();

        for (int kwIdx = 0; kwIdx < keywordArrSplit.length; kwIdx++) {
            int finalKwIdx = kwIdx;
            List<SuggestionMetaDTO> suggestionMeta = filteredSuggestions.stream().
                    filter(filteredSuggestion -> filteredSuggestion.getSuggestions().length > finalKwIdx &&
                            keywordArrSplit[finalKwIdx].equals(filteredSuggestion.getSuggestions()[finalKwIdx])).
                    collect(Collectors.toList());

            SuggestionMatchDTO matchFound = matchFound(suggestion, searchTerm, suggestionMeta, keyword);
            if (null != matchFound)
                return matchFound;

            filteredSuggestions = suggestionMeta;
            suggestion.setMatchSoFar(suggestion.getMatchSoFar() + keywordArrSplit[kwIdx] + SPACE);

        }
        return suggestion;
    }

    private SuggestionMatchDTO matchFound(SuggestionMatchDTO suggestion, String searchTerm,
                                          List<SuggestionMetaDTO> suggestionMetaDTOS, String keyword) {
        if (suggestionMetaDTOS.size() == 0) {
            String matchSoFar = searchTerm.length() > suggestion.getMatchSoFar().length()
                    ? searchTerm : suggestion.getMatchSoFar();
            suggestion.setMatchSoFar(matchSoFar);
            return suggestion;
        }

        if (suggestionMetaDTOS.size() == 1 && suggestionMetaDTOS.get(0).getKeyword().equals(keyword))
            return SearchVolumeMapper.populateMatchedSuggestion(true,
                    suggestionMetaDTOS.get(0).getIndex(), suggestion);


        for (int i = 0; i < suggestionMetaDTOS.size(); i++)
            if (suggestionMetaDTOS.get(i).getKeyword().equals(keyword))
                return SearchVolumeMapper.populateMatchedSuggestion(true, i, suggestion);

        return null;
    }

}
