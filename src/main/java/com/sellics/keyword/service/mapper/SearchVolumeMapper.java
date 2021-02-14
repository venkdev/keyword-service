package com.sellics.keyword.service.mapper;

import com.sellics.keyword.datatransferobject.SuggestionDTO;
import com.sellics.keyword.datatransferobject.SuggestionMatchDTO;
import com.sellics.keyword.datatransferobject.SuggestionMetaDTO;

import java.util.ArrayList;
import java.util.List;

public class SearchVolumeMapper {
    public static SuggestionMatchDTO populateMatchedSuggestion(boolean completeMatch, int matchIdx, SuggestionMatchDTO suggestionMatchDTO) {
        suggestionMatchDTO.setMatchedIdx(matchIdx);
        suggestionMatchDTO.setCompleteMatch(completeMatch);
        return suggestionMatchDTO;
    }

    public static List<SuggestionMetaDTO> populateSuggestionMetadata(List<SuggestionDTO> suggestions) {
        List<SuggestionMetaDTO> suggestionMetaDTOList = new ArrayList<>();
        for (int i = 0; i < suggestions.size(); i++) {
            SuggestionMetaDTO suggestionMetaDTO = new SuggestionMetaDTO();
            suggestionMetaDTO.setSuggestions(suggestions.get(i).getValue().split(" "));
            suggestionMetaDTO.setKeyword(suggestions.get(i).getValue());
            suggestionMetaDTO.setIndex(i);
            suggestionMetaDTOList.add(suggestionMetaDTO);
        }
        return suggestionMetaDTOList;
    }
}
