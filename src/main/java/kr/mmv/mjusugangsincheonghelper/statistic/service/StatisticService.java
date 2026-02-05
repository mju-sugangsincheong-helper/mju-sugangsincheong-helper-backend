package kr.mmv.mjusugangsincheonghelper.statistic.service;

import kr.mmv.mjusugangsincheonghelper.statistic.dto.SummaryStatsResponseDto;

import java.util.Map;

public interface StatisticService {
    Map<String, Object> getSubscriptionStats();
    
    SummaryStatsResponseDto getSummaryStats();
}