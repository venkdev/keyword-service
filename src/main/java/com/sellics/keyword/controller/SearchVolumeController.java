package com.sellics.keyword.controller;

import com.sellics.keyword.datatransferobject.EstimateDTO;
import com.sellics.keyword.service.searchvolume.SearchVolumeService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SearchVolumeController {

    SearchVolumeService searchVolumeService;

    public SearchVolumeController(SearchVolumeService searchVolumeService) {
        this.searchVolumeService = searchVolumeService;
    }

    @GetMapping("/estimate")
    public EstimateDTO test(@RequestParam String keyword){
        if(StringUtils.isEmpty(keyword))
            return EstimateDTO.newBuilder().setKeyword(keyword).setEstimate(0).createEstimateDTO();

        return EstimateDTO.newBuilder().setKeyword(keyword)
                .setEstimate(searchVolumeService.estimateSearchVolume(keyword)).createEstimateDTO();


    }
}
