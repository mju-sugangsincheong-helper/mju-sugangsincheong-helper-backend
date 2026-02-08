package kr.mmv.mjusugangsincheonghelper.global.repository;

import kr.mmv.mjusugangsincheonghelper.global.entity.StudentPrivacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentPrivacyRepository extends JpaRepository<StudentPrivacy, String> {
}
