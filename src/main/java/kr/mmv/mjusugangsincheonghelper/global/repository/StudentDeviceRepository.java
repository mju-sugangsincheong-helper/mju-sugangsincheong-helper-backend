package kr.mmv.mjusugangsincheonghelper.global.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.mmv.mjusugangsincheonghelper.global.entity.StudentDevice;

import java.util.List;
import java.util.Optional;

/**
 * 학생 디바이스 Repository
 */
@Repository
public interface StudentDeviceRepository extends JpaRepository<StudentDevice, Long> {

    /**
     * 학생 ID로 디바이스 목록 조회
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
     * 학생 ID로 모든 FCM 토큰 조회
     */
    @Query("SELECT d.fcmToken FROM StudentDevice d WHERE d.student.studentId = :studentId")
    List<String> findFcmTokensByStudentId(@Param("studentId") String studentId);

    /**
     * 여러 학생 ID로 모든 FCM 토큰 조회
     */
    @Query("SELECT d.fcmToken FROM StudentDevice d WHERE d.student.studentId IN :studentIds")
    List<String> findFcmTokensByStudentIds(@Param("studentIds") List<String> studentIds);

    /**
     * 학생 ID로 디바이스 삭제
     */
    void deleteByStudentStudentId(String studentId);

    /**
     * FCM 토큰으로 디바이스 삭제
     */
    void deleteByFcmToken(String fcmToken);
}
