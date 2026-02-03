package kr.mmv.mjusugangsincheonghelper.global.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.mmv.mjusugangsincheonghelper.global.entity.StudentDevice;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

/**
 * 학생 디바이스 Repository
 */
@Repository
public interface StudentDeviceRepository extends JpaRepository<StudentDevice, Long> {

    /**
     * 학생 ID로 활성 디바이스 목록 조회
     */
    @Query("SELECT d FROM StudentDevice d WHERE d.student.studentId = :studentId AND d.status = 'ACTIVE'")
    List<StudentDevice> findActiveByStudentId(@Param("studentId") String studentId);

    /**
     * 학생 ID로 모든 디바이스 목록 조회 (상태 무관)
     */
    List<StudentDevice> findByStudentStudentId(String studentId);

    /**
     * FCM 토큰으로 디바이스 조회
     */
    Optional<StudentDevice> findByFcmToken(String fcmToken);

    /**
     * FCM 토큰 존재 여부 확인
     */
    boolean existsByFcmToken(String fcmToken);

    /**
     * 학생 ID로 모든 활성 FCM 토큰 조회
     */
    @Query("SELECT d.fcmToken FROM StudentDevice d WHERE d.student.studentId = :studentId AND d.status = 'ACTIVE'")
    List<String> findActiveFcmTokensByStudentId(@Param("studentId") String studentId);

    /**
     * 여러 학생 ID로 모든 활성 FCM 토큰 조회
     */
    @Query("SELECT d.fcmToken FROM StudentDevice d WHERE d.student.studentId IN :studentIds AND d.status = 'ACTIVE'")
    List<String> findActiveFcmTokensByStudentIds(@Param("studentIds") List<String> studentIds);

    /**
     * 학생 ID로 디바이스 삭제 (Hard Delete - 사용자가 요청 시)
     */
    @Transactional
    void deleteByStudentStudentId(String studentId);

    /**
     * FCM 토큰으로 디바이스 삭제 (Hard Delete)
     */
    @Transactional
    void deleteByFcmToken(String fcmToken);

    /**
     * 특정 학생들의 모든 활성 기기 정보를 한 번에 가져오기 (Fetch Join)
     * - 알림 발송용이므로 status = 'ACTIVE' 필수
     */
    @Query("SELECT d FROM StudentDevice d JOIN FETCH d.student WHERE d.student.studentId IN :studentIds AND d.status = 'ACTIVE'")
    List<StudentDevice> findAllActiveByStudentIdIn(@Param("studentIds") List<String> studentIds);
}
