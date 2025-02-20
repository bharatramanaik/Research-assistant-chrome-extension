package com.research.assistant.service;

import com.research.assistant.model.ResearchRequest;
import org.springframework.stereotype.Service;


public interface ResearchService {
    String processContent(ResearchRequest request) throws Exception;
}
