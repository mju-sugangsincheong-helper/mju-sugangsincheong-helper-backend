package kr.mmv.mjusugangsincheonghelper.global.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.mmv.mjusugangsincheonghelper.global.entity.PracticeSession;
import kr.mmv.mjusugangsincheonghelper.global.entity.Student;

import java.util.List;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {

    /**
     * 사용자의 연습 기록 조회
     */
    List<PracticeSession> findByStudentOrderByCreatedAtDesc(Student student);

    /**
     * 학생의 과목 수 별 최고 기록 조회
     * (각 count_num 별 가장 빠른 time_ms 기록 조회)
     */
    @Query("SELECT p FROM PracticeSession p WHERE p.student.studentId = :studentId AND (p.countNum, p.timeMs) IN " +
           "(SELECT p2.countNum, MIN(p2.timeMs) FROM PracticeSession p2 WHERE p2.student.studentId = :studentId GROUP BY p2.countNum)")
    List<PracticeSession> findBestRecordsByStudent(@Param("studentId") String studentId);


    /**
     * 전체 랭킹 조회 (장바구니 개수별 상위 기록)
     */
    @Query(value = "SELECT p.* FROM practice_sessions p " +
            "INNER JOIN (SELECT count_num, MIN(time_ms) as min_time FROM practice_sessions GROUP BY count_num, student_id) best " +
            "ON p.count_num = best.count_num AND p.time_ms = best.min_time " +
            "ORDER BY p.count_num, p.time_ms", nativeQuery = true)
    List<PracticeSession> findGlobalRanking();

    /**
     * 특정 과목 수(count_num)에 대한 랭킹 조회
     * (각 사용자별 최고 기록만 조회, time_ms 오름차순)
     */
    @Query("SELECT p FROM PracticeSession p WHERE p.countNum = :countNum AND p.timeMs = " +
           "(SELECT MIN(p2.timeMs) FROM PracticeSession p2 WHERE p2.student = p.student AND p2.countNum = :countNum) " +
           "ORDER BY p.timeMs")
    List<PracticeSession> findRankingByCountNum(@Param("countNum") Integer countNum);


    // ==========================================
    //  Personal Rank Calculation (Count Queries)
    // ==========================================

    /**
     * 전체 랭킹: 나보다 기록이 좋은 사람 수
     */
    @Query(value = "SELECT COUNT(*) FROM (" +
            "SELECT student_id FROM practice_sessions " +
            "WHERE count_num = :countNum " +
            "GROUP BY student_id " +
            "HAVING MIN(time_ms) < :myTime " +
            ") as better_ranks", nativeQuery = true)
    long countHigherRank(@Param("countNum") Integer countNum, @Param("myTime") Long myTime);

    /**
     * 학과 랭킹: 같은 학과 내에서 나보다 기록이 좋은 사람 수
     */
    @Query(value = "SELECT COUNT(*) FROM (" +
            "SELECT p.student_id FROM practice_sessions p " +
            "JOIN students s ON p.student_id = s.student_id " +
            "WHERE p.count_num = :countNum AND s.department = :dept " +
            "GROUP BY p.student_id " +
            "HAVING MIN(p.time_ms) < :myTime " +
            ") as better_ranks", nativeQuery = true)
    long countHigherRankByDept(@Param("countNum") Integer countNum, @Param("myTime") Long myTime, @Param("dept") String dept);

    /**
     * 학년 랭킹: 같은 학년 내에서 나보다 기록이 좋은 사람 수
     */
    @Query(value = "SELECT COUNT(*) FROM (" +
            "SELECT p.student_id FROM practice_sessions p " +
            "JOIN students s ON p.student_id = s.student_id " +
            "WHERE p.count_num = :countNum AND s.grade = :grade " +
            "GROUP BY p.student_id " +
            "HAVING MIN(p.time_ms) < :myTime " +
            ") as better_ranks", nativeQuery = true)
    long countHigherRankByGrade(@Param("countNum") Integer countNum, @Param("myTime") Long myTime, @Param("grade") String grade);
}
