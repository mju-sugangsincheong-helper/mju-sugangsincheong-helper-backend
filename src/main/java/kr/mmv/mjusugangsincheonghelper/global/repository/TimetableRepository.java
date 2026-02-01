package kr.mmv.mjusugangsincheonghelper.global.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.mmv.mjusugangsincheonghelper.global.entity.Section;
import kr.mmv.mjusugangsincheonghelper.global.entity.Student;
import kr.mmv.mjusugangsincheonghelper.global.entity.Timetable;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    /**
     * 사용자의 시간표 조회
     */
    List<Timetable> findByUser(Student user);

    /**
     * 사용자 ID로 시간표 조회
     */
    @Query("SELECT t FROM Timetable t JOIN FETCH t.section WHERE t.user.studentId = :studentId")
    List<Timetable> findByStudentIdWithSection(@Param("studentId") String studentId);

    /**
     * 사용자-강의 등록 여부 확인
     */
    boolean existsByUserAndSection(Student user, Section section);

    /**
     * 특정 시간표 항목 조회
     */
    Optional<Timetable> findByUserAndSection(Student user, Section section);

    /**
     * 사용자의 시간표 항목 수
     */
    long countByUser(Student user);
}
