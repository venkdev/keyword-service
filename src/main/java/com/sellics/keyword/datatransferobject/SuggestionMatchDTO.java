package com.sellics.keyword.datatransferobject;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;

public class SuggestionMatchDTO {
    int matchedIdx;
    boolean completeMatch;
    int matchedPage;
    String matchSoFar;

    public SuggestionMatchDTO() {
        matchedIdx = -1;
        matchedPage = -1;
        completeMatch = false;
        matchSoFar = EMPTY_STRING;
    }


    public int getMatchedIdx() {
        return matchedIdx;
    }

    public void setMatchedIdx(int matchedIdx) {
        this.matchedIdx = matchedIdx;
    }

    public boolean isCompleteMatch() {
        return completeMatch;
    }

    public void setCompleteMatch(boolean completeMatch) {
        this.completeMatch = completeMatch;
    }

    public String getMatchSoFar() {
        return matchSoFar;
    }

    public void setMatchSoFar(String matchSoFar) {
        this.matchSoFar = matchSoFar;
    }

    public int getMatchedPage() {
        return matchedPage;
    }

    public void setMatchedPage(int matchedPage) {
        this.matchedPage = matchedPage;
    }

    @Override
    public String toString() {
        return "SuggestionMatchDTO{" +
                "matchedIdx=" + matchedIdx +
                ", completeMatch=" + completeMatch +
                ", matchedPage=" + matchedPage +
                ", matchSoFar='" + matchSoFar + '\'' +
                '}';
    }
}
