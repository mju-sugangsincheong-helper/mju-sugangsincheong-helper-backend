package kr.mmv.mjusugangsincheonghelper.global.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.mmv.mjusugangsincheonghelper.global.entity.Section;

import java.util.List;

/**
 * Section Repository
 * PK: sectioncls (String)
 */
@Repository
public interface SectionRepository extends JpaRepository<Section, String> {

    /**
     * 활성화된 강의 목록 조회
     */
    List<Section> findByIsActiveTrue();

    /**
     * 학과 코드로 강의 목록 조회
     */
    List<Section> findByDeptcdAndIsActiveTrue(String deptcd);

    /**
     * 캠퍼스 + 활성화된 강의 목록 조회
     */
    List<Section> findByCampusdivAndIsActiveTrue(String campusdiv);

    /**
     * 여석 존재 강의 조회 (구독 알림)
     */
    @Query("SELECT s FROM Section s WHERE s.sectioncls IN :sectionIds AND s.isFull = false AND s.isActive = true")
    List<Section> findAvailableSections(@Param("sectionIds") List<String> sectionIds);

    /**
     * 검색 (과목명)
     */
    @Query("SELECT s FROM Section s WHERE s.isActive = true AND s.curinm LIKE %:keyword%")
    List<Section> searchByCurinm(@Param("keyword") String keyword);

    /**
     * 검색 (교수명)
     */
    @Query("SELECT s FROM Section s WHERE s.isActive = true AND s.profnm LIKE %:keyword%")
    List<Section> searchByProfnm(@Param("keyword") String keyword);

    /**
     * 연도별 강의 수 조회
     */
    long countByCuriyearAndIsActiveTrue(String curiyear);

    /**
     * 연도 + 캠퍼스별 강의 수 조회
     */
    long countByCuriyearAndCampusdivAndIsActiveTrue(String curiyear, String campusdiv);

    /**
     * 학수번호로 강의 조회
     */
    List<Section> findByCurinumAndIsActiveTrue(String curinum);

    /**
     * 여석 있는 강의 목록 (현재 신청인원 < 정원)
     */
    @Query("SELECT s FROM Section s WHERE s.isActive = true AND s.isFull = false ORDER BY s.curinm")
    List<Section> findAvailableSections();

    /**
     * 특정 대상학년 강의 조회 (0은 전체, 1~4는 해당 학년)
     */
    List<Section> findByComyearAndIsActiveTrue(Integer comyear);

    /**
     * 전체 학과 목록 조회 (중복 제거)
     * 활성화된 강의에서 학과코드, 학과명, 캠퍼스구분을 DISTINCT 조회
     */
    @Query("SELECT DISTINCT s.deptcd, s.deptnm, s.campusdiv FROM Section s WHERE s.isActive = true AND s.deptcd IS NOT NULL ORDER BY s.campusdiv, s.deptnm")
    List<Object[]> findDistinctDepartments();

    /**
     * 캠퍼스별 학과 목록 조회 (중복 제거)
     */
    @Query("SELECT DISTINCT s.deptcd, s.deptnm, s.campusdiv FROM Section s WHERE s.isActive = true AND s.campusdiv = :campusdiv AND s.deptcd IS NOT NULL ORDER BY s.deptnm")
    List<Object[]> findDistinctDepartmentsByCampus(@Param("campusdiv") String campusdiv);
}
