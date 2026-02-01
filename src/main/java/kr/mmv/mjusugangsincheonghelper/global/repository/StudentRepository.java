package kr.mmv.mjusugangsincheonghelper.global.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.mmv.mjusugangsincheonghelper.global.entity.Student;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    
    Optional<Student> findByStudentId(String studentId);
    
    boolean existsByStudentId(String studentId);
    
    Optional<Student> findByRefreshToken(String refreshToken);
}
