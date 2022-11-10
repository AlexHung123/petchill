package com.alexhong.petchill.search.service;

import com.alexhong.petchill.search.vo.SearchParam;
import com.alexhong.petchill.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}
