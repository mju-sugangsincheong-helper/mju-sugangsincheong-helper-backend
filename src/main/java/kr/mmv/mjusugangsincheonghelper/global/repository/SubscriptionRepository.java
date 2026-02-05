package kr.mmv.mjusugangsincheonghelper.global.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.mmv.mjusugangsincheonghelper.global.entity.Section;
import kr.mmv.mjusugangsincheonghelper.global.entity.Student;
import kr.mmv.mjusugangsincheonghelper.global.entity.Subscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 사용자의 모든 구독 조회
     */
    List<Subscription> findByUser(Student user);

    /**
     * 사용자의 활성화된 구독 조회
     */
    List<Subscription> findByUserAndNotificationEnabledTrue(Student user);

    /**
     * 사용자 ID로 구독 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.studentId = :studentId")
    List<Subscription> findByStudentId(@Param("studentId") String studentId);

    /**
     * 특정 강의의 구독자 조회 (알림 발송용)
     */
    @Query("SELECT s FROM Subscription s JOIN FETCH s.user WHERE s.section.id = :sectionId AND s.notificationEnabled = true")
    List<Subscription> findSubscribersForSection(@Param("sectionId") Long sectionId);

    /**
     * 여러 강의의 구독자 조회 (배치 알림용)
     */
    @Query("SELECT s FROM Subscription s JOIN FETCH s.user WHERE s.section.id IN :sectionIds AND s.notificationEnabled = true")
    List<Subscription> findSubscribersForSections(@Param("sectionIds") List<Long> sectionIds);

    /**
     * 사용자-강의 구독 여부 확인
     */
    boolean existsByUserAndSection(Student user, Section section);

    /**
     * 특정 구독 조회
     */
    Optional<Subscription> findByUserAndSection(Student user, Section section);

    /**
     * 사용자의 구독 수
     */
    long countByUser(Student user);

    /**
     * 특정 강의의 구독자 수 (통계용)
     */
    long countBySection(Section section);

    /**
     * 강의 sectioncls로 구독자 수 조회
     */
    @Query("SELECT s.section.sectioncls, COUNT(s) FROM Subscription s GROUP BY s.section.sectioncls")
    List<Object[]> countSubscriptionsBySection();

    /**
     * 여러 강의의 구독자 조회 (sectioncls 기반, 배치 알림용)
     */
    @Query("SELECT s FROM Subscription s JOIN FETCH s.user WHERE s.section.sectioncls IN :sectionIds AND s.notificationEnabled = true")
    List<Subscription> findSubscribersForSectionIds(@Param("sectionIds") List<String> sectionIds);

    /**
     * 사용자의 모든 구독 삭제
     */
    void deleteByUser(Student user);

    /**
     * 구독을 1개 이상 가진 고유 학생 수 조회 (통계용)
     */
    @Query("SELECT COUNT(DISTINCT s.user) FROM Subscription s")
    long countDistinctSubscribers();

    /**
     * 학과별 구독자 수 TOP 10 조회
     * Student.department 기준, 구독자 수 내림차순
     */
    @Query("SELECT s.user.department, COUNT(DISTINCT s.user) FROM Subscription s WHERE s.user.department IS NOT NULL GROUP BY s.user.department ORDER BY COUNT(DISTINCT s.user) DESC")
    List<Object[]> countByDepartmentTop10();
}
