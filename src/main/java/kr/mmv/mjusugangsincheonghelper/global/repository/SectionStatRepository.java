package kr.mmv.mjusugangsincheonghelper.global.repository;

import kr.mmv.mjusugangsincheonghelper.global.entity.SectionStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionStatRepository extends JpaRepository<SectionStat, String> {

    @Modifying(clearAutomatically = true)
    @Query(value = """
        INSERT INTO section_stats (
            sectioncls, 
            takelim, listennow, available_seats,
            curr_subscribers, 
            curr_grade1_subscribers, curr_grade2_subscribers, curr_grade3_subscribers, curr_grade4_subscribers,
            max_subscribers, 
            max_grade1_subscribers, max_grade2_subscribers, max_grade3_subscribers, max_grade4_subscribers,
            last_updated, max_updated
        )
        SELECT * FROM (
            SELECT 
                s.sectioncls as sectioncls,
                COALESCE(s.takelim, 0) as takelim, 
                COALESCE(s.listennow, 0) as listennow, 
                GREATEST(0, COALESCE(s.takelim, 0) - COALESCE(s.listennow, 0)) as available_seats,
                COUNT(sub.student_id) as curr_subscribers,
                COALESCE(SUM(CASE WHEN st.grade = '1' THEN 1 ELSE 0 END), 0) as curr_grade1_subscribers,
                COALESCE(SUM(CASE WHEN st.grade = '2' THEN 1 ELSE 0 END), 0) as curr_grade2_subscribers,
                COALESCE(SUM(CASE WHEN st.grade = '3' THEN 1 ELSE 0 END), 0) as curr_grade3_subscribers,
                COALESCE(SUM(CASE WHEN st.grade >= '4' THEN 1 ELSE 0 END), 0) as curr_grade4_subscribers,
                COUNT(sub.student_id) as max_subscribers,
                COALESCE(SUM(CASE WHEN st.grade = '1' THEN 1 ELSE 0 END), 0) as max_grade1_subscribers,
                COALESCE(SUM(CASE WHEN st.grade = '2' THEN 1 ELSE 0 END), 0) as max_grade2_subscribers,
                COALESCE(SUM(CASE WHEN st.grade = '3' THEN 1 ELSE 0 END), 0) as max_grade3_subscribers,
                COALESCE(SUM(CASE WHEN st.grade >= '4' THEN 1 ELSE 0 END), 0) as max_grade4_subscribers,
                NOW() as last_updated, 
                NOW() as max_updated
            FROM sections s
            LEFT JOIN subscriptions sub ON s.sectioncls = sub.section_id
            LEFT JOIN students st ON sub.student_id = st.student_id
            GROUP BY s.sectioncls
        ) AS new_stats
        ON DUPLICATE KEY UPDATE
            takelim = new_stats.takelim,
            listennow = new_stats.listennow,
            available_seats = new_stats.available_seats,
            
            curr_subscribers = new_stats.curr_subscribers,
            curr_grade1_subscribers = new_stats.curr_grade1_subscribers,
            curr_grade2_subscribers = new_stats.curr_grade2_subscribers,
            curr_grade3_subscribers = new_stats.curr_grade3_subscribers,
            curr_grade4_subscribers = new_stats.curr_grade4_subscribers,
            
            max_subscribers = GREATEST(section_stats.max_subscribers, new_stats.max_subscribers),
            max_grade1_subscribers = GREATEST(section_stats.max_grade1_subscribers, new_stats.max_grade1_subscribers),
            max_grade2_subscribers = GREATEST(section_stats.max_grade2_subscribers, new_stats.max_grade2_subscribers),
            max_grade3_subscribers = GREATEST(section_stats.max_grade3_subscribers, new_stats.max_grade3_subscribers),
            max_grade4_subscribers = GREATEST(section_stats.max_grade4_subscribers, new_stats.max_grade4_subscribers),
            
            last_updated = new_stats.last_updated,
            max_updated = CASE 
                WHEN new_stats.max_subscribers > section_stats.max_subscribers THEN new_stats.max_updated 
                ELSE COALESCE(section_stats.max_updated, new_stats.max_updated)
            END
    """, nativeQuery = true)
    void refreshStatistics();
}