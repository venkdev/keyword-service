package com.sellics.keyword.datatransferobject;

public class EstimateDTO {
    private String keyword;
    private int estimate;

    private EstimateDTO(String keyword, int estimate) {
        this.keyword = keyword;
        this.estimate = estimate;
    }

    public String getKeyword() {
        return keyword;
    }

    public int getEstimate() {
        return estimate;
    }

    public static EstimateDTOBuilder newBuilder(){
        return new EstimateDTOBuilder();
    }

    public static class EstimateDTOBuilder{
        private String keyword;
        private int estimate;

        public EstimateDTOBuilder setKeyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public EstimateDTOBuilder setEstimate(int estimate) {
            this.estimate = estimate;
            return this;
        }

        public EstimateDTO createEstimateDTO(){
            return new EstimateDTO(keyword, estimate);
        }
    }

}
