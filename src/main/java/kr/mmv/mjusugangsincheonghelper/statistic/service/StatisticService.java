package kr.mmv.mjusugangsincheonghelper.statistic.service;

import kr.mmv.mjusugangsincheonghelper.statistic.dto.SummaryStatsResponseDto;
import kr.mmv.mjusugangsincheonghelper.statistic.dto.CourseStatisticResponseDto;

import java.util.Map;

public interface StatisticService {
    Map<String, Object> getSubscriptionStats();
    
    SummaryStatsResponseDto getSummaryStats();

    CourseStatisticResponseDto getCourseStatistics(String sectioncls);
}