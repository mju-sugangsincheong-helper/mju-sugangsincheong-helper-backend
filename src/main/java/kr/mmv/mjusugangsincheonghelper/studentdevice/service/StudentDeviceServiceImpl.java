package kr.mmv.mjusugangsincheonghelper.studentdevice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.exception.BaseException;
import kr.mmv.mjusugangsincheonghelper.global.entity.Student;
import kr.mmv.mjusugangsincheonghelper.global.entity.StudentDevice;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentDeviceRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentRepository;
import kr.mmv.mjusugangsincheonghelper.studentdevice.dto.DeviceRegisterRequestDto;
import kr.mmv.mjusugangsincheonghelper.studentdevice.dto.DeviceResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentDeviceServiceImpl implements StudentDeviceService {

    private final StudentDeviceRepository studentDeviceRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public void registerDevice(String studentId, DeviceRegisterRequestDto request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_USER_NOT_FOUND));

        Optional<StudentDevice> existingDevice = studentDeviceRepository.findByFcmToken(request.getFcmToken());

        if (existingDevice.isPresent()) {
            StudentDevice device = existingDevice.get();
            // 소유자가 변경된 경우 (다른 계정으로 로그인) - 기기 소유권 이전
            if (!device.getStudent().getStudentId().equals(studentId)) {
                log.info("Device ownership changed: token={}, oldUser={}, newUser={}", 
                        request.getFcmToken(), device.getStudent().getStudentId(), studentId);
                device.setStudent(student); 
            }
            // 정보 업데이트 (마지막 활성 시간 포함)
            device.updateDeviceInfo(request.getPlatform(), request.getUserAgent());
        } else {
            // 신규 등록
            StudentDevice newDevice = StudentDevice.builder()
                    .student(student)
                    .fcmToken(request.getFcmToken())
                    .platform(request.getPlatform())
                    .userAgent(request.getUserAgent())
                    .lastActiveAt(LocalDateTime.now())
                    .build();
            studentDeviceRepository.save(newDevice);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponseDto> getMyDevices(String studentId) {
        return studentDeviceRepository.findByStudentStudentId(studentId).stream()
                .map(DeviceResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDevice(String studentId, String fcmToken) {
        StudentDevice device = studentDeviceRepository.findByFcmToken(fcmToken)
                .orElseThrow(() -> new BaseException(ErrorCode.DEVICE_NOT_FOUND));

        // 본인의 디바이스인지 검증
        if (!device.getStudent().getStudentId().equals(studentId)) {
            throw new BaseException(ErrorCode.AUTH_SECURITY_FORBIDDEN_ACCESS);
        }

        studentDeviceRepository.delete(device);
    }
}
