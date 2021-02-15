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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.sellics.keyword.constant.ApplicationConstant.*;

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

    /**
     * Finds search volume estimate by calling Amazon's autocomplete API multiple times. Max number of calls is
     * capped to 6. Any more calls would result in a score of zero based on the scoring logic.
     *
     * @param keyword Input keyword for estimate API
     * @return search volume estimate
     */
    @Override
    public int estimateSearchVolume(String keyword) {
        SuggestionMatchDTO suggestionMatch = searchMatch(keyword.toLowerCase(), 6);
        if (null == suggestionMatch)
            return 0;
        return calculateEstimate(suggestionMatch);
    }

    /**
     * Two factors contribute to calculation of estimate.
     * 1. Number of calls that were made to get an exact match
     * 2. Index of the exact match in the response
     * <p>
     * Score is identified to have an exponential relation with the number of calls made.
     * y = 100 - x^2;
     * where y is the score
     * x is the number of tries + 0.index in match. x is then multiplied by 2 to scale 5 different points against 100.
     *
     * @param suggestion Matched suggestion
     * @return search volume estimate
     */
    private static int calculateEstimate(SuggestionMatchDTO suggestion) {

        int numberOfTries = suggestion.getMatchedPage();
        double scoringFactor = (numberOfTries + ((double) suggestion.getMatchedIdx() / 10)) * 2;
        double score = 100 - Math.pow(scoringFactor, 2);
        return score > 0 ? (int) score : 0;

    }

    /**
     * Calls Amazon's autocomplete API multiple times and finds a match.
     *
     * @param keyword Input keyword for estimate API
     * @return The number of tries made to get an exact match from Amazon auto complete API and the position (index) of
     * the exact match. Returns null if no match found.
     */
    private SuggestionMatchDTO searchMatch(String keyword, int maxNumberOfCalls) {
        String searchTerm = String.valueOf(keyword.charAt(0));
        for (int i = 0; i < maxNumberOfCalls; i++) {
            LOG.info(SEARCH_TERM_MSG + searchTerm);
            AutoCompleteResponseDTO response = restTemplate.getForObject(autoCompleteUri.toUriString(),
                    AutoCompleteResponseDTO.class, searchTerm);
            if (null == response || null == response.getSuggestions() || response.getSuggestions().isEmpty())
                return null;

            SuggestionMatchDTO suggestionMatch = checkForMatch(keyword, searchTerm, response.getSuggestions());
            if (suggestionMatch.isCompleteMatch()) {
                LOG.info(MATCH_MSG + suggestionMatch);
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

    /**
     * @param keyword     Input keyword for estimate API
     * @param searchTerm  Current term under query
     * @param suggestions Amazon autocomplete suggestions from API
     * @return Can return two states.
     * 1. An exact match with attribute completeMatch = true and the position of match.
     * 2. A partial match with attribute completeMatch = false & matchSoFar with the updated search term with partial
     * match.
     */
    private SuggestionMatchDTO checkForMatch(String keyword, String searchTerm, List<SuggestionDTO> suggestions) {
        String[] keywordArrSplit = keyword.split(SPACE);
        String[] searchNotRequired = Arrays.copyOfRange(searchTerm.split(SPACE), 0,
                searchTerm.split(SPACE).length - 1);

        List<SuggestionMetaDTO> filteredSuggestions = SearchVolumeMapper.populateSuggestionMetadata(suggestions);
        SuggestionMatchDTO suggestion = new SuggestionMatchDTO();
        if (searchNotRequired.length > 0)
            suggestion.setMatchSoFar(String.join(SPACE, searchNotRequired) + SPACE);

        for (int kwIdx = searchNotRequired.length; kwIdx < keywordArrSplit.length; kwIdx++) {
            int finalKwIdx = kwIdx;
            List<SuggestionMetaDTO> suggestionMeta = filteredSuggestions.stream().
                    filter(filteredSuggestion -> filteredSuggestion.getSuggestions().length > finalKwIdx &&
                            keywordArrSplit[finalKwIdx].equals(filteredSuggestion.getSuggestions()[finalKwIdx])).
                    collect(Collectors.toList());

            SuggestionMatchDTO match = findMatch(suggestion, searchTerm, suggestionMeta, keyword,
                    kwIdx - searchNotRequired.length == 0);
            if (null != match)
                return match;

            filteredSuggestions = suggestionMeta;
            suggestion.setMatchSoFar(suggestion.getMatchSoFar() + keywordArrSplit[kwIdx] + SPACE);

        }
        return suggestion;
    }

    /**
     * @param suggestion         Response SuggestionMatchDTO object
     * @param searchTerm         Current term under query
     * @param suggestionMetaDTOS Suggestion meta data
     * @param keyword            Input keyword for estimate API
     * @param scanAllMatches     Should the whole list be scanned for an entire match.
     * @return Can return 3 possible states.
     * 1. There is no match. Updates the matching search term until this point and returns. It is not possible to
     * look for partial/exact match when there are no matching results.
     * 2. If there are suggestion matches, find an exact match and return. Is controlled by scan all matches boolean.
     * 3. Returns null when above 2 conditions are not met. i.e. the search process is not over yet and has a
     * partial match, which should get updated and searched for remaining partial matches in further iterations.
     */
    private SuggestionMatchDTO findMatch(SuggestionMatchDTO suggestion, String searchTerm,
                                         List<SuggestionMetaDTO> suggestionMetaDTOS, String keyword,
                                         boolean scanAllMatches) {
        if (suggestionMetaDTOS.size() == 0) {
            String matchSoFar = searchTerm.length() > suggestion.getMatchSoFar().length()
                    ? searchTerm : suggestion.getMatchSoFar();
            suggestion.setMatchSoFar(matchSoFar);
            return suggestion;
        }

        return suggestionMetaDTOS.stream().
                filter(suggestionMeta -> scanAllMatches && suggestionMeta.getKeyword().equals(keyword)).
                findFirst().
                map(suggestionMetaDTO ->
                        SearchVolumeMapper.populateMatchedSuggestion(true, suggestionMetaDTO.getIndex(), suggestion)).
                orElse(null);

    }

}
