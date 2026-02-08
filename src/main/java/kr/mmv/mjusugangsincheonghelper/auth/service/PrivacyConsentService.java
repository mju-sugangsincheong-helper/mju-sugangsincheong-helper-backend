package kr.mmv.mjusugangsincheonghelper.auth.service;

import kr.mmv.mjusugangsincheonghelper.global.entity.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PrivacyConsentService {
    public void saveConsent(Student student, String version, String type) {
        log.info("Privacy consent saved for student {}: version={}, type={}", student.getStudentId(), version, type);
    }
}
