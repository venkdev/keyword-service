package com.sellics.keyword.controller;

import com.sellics.keyword.service.searchvolume.SearchVolumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SearchVolumeControllerTest {

    @Mock
    SearchVolumeController searchVolumeController;

    @Mock
    SearchVolumeService searchVolumeService;

    MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(searchVolumeController)
                .setMessageConverters(new StringHttpMessageConverter(), new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void getEstimate() throws Exception {
        MockHttpServletRequestBuilder getEstimateReq = MockMvcRequestBuilders.get("/estimate?keyword=key")
                .contentType(MediaType.APPLICATION_JSON);
        Mockito.doReturn(90).when(searchVolumeService).estimateSearchVolume(Mockito.anyString());
        mockMvc.perform(getEstimateReq).andExpect(status().isOk());
    }
}