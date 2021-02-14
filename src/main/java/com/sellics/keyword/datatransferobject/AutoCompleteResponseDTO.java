package com.sellics.keyword.datatransferobject;

import java.util.List;

public class AutoCompleteResponseDTO {
    List<SuggestionDTO> suggestions;

    public List<SuggestionDTO> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<SuggestionDTO> suggestions) {
        this.suggestions = suggestions;
    }

    @Override
    public String toString() {
        return "AutoCompleteResponseDTO{" +
                "suggestions=" + suggestions +
                '}';
    }
}
