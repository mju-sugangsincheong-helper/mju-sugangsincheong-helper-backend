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
    List<PracticeSession> findByUserOrderByCreatedAtDesc(Student user);

    /**
     * 사용자의 장바구니 개수별 최고 기록 조회
     */
    @Query("SELECT p FROM PracticeSession p WHERE p.user.studentId = :studentId " +
            "AND (p.countNum, p.timeMs) IN " +
            "(SELECT p2.countNum, MIN(p2.timeMs) FROM PracticeSession p2 " +
            "WHERE p2.user.studentId = :studentId GROUP BY p2.countNum)")
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
     * 특정 장바구니 개수의 랭킹 조회
     */
    @Query("SELECT p FROM PracticeSession p WHERE p.countNum = :countNum " +
            "AND p.timeMs = (SELECT MIN(p2.timeMs) FROM PracticeSession p2 " +
            "WHERE p2.user = p.user AND p2.countNum = :countNum) " +
            "ORDER BY p.timeMs")
    List<PracticeSession> findRankingByCountNum(@Param("countNum") Integer countNum);
}
