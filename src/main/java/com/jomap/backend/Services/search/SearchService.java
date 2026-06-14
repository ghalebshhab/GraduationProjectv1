package com.jomap.backend.Services.search;

import java.util.List;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.search.SearchItem;

public interface SearchService {

    ApiResponse<com.jomap.backend.DTOs.PaginatedResponse<SearchItem>> getAllItems(String userEmail, int page, int size);
}
