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
    
    private static final int MAX_DEVICE_COUNT = 5;

    @Override
    @Transactional
    public void registerDevice(String studentId, DeviceRegisterRequestDto request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_USER_NOT_FOUND));

        // 플랫폼 검증 및 변환
        StudentDevice.DevicePlatform platform;
        try {
            platform = StudentDevice.DevicePlatform.valueOf(request.getPlatform().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BaseException(ErrorCode.DEVICE_PLATFORM_INVALID);
        }

        Optional<StudentDevice> existingDevice = studentDeviceRepository.findByFcmToken(request.getFcmToken());

        if (existingDevice.isPresent()) {
            StudentDevice device = existingDevice.get();
            // 소유자가 변경된 경우 (다른 계정으로 로그인) - 기기 소유권 이전
            if (!device.getStudent().getStudentId().equals(studentId)) {
                log.info("Device ownership changed: token={}, oldUser={}, newUser={}", 
                        request.getFcmToken(), device.getStudent().getStudentId(), studentId);
                
                // 기존 사용자의 기기 개수는 감소하므로 체크 불필요
                // 새 사용자의 기기 개수 체크 필요
                validateDeviceLimit(studentId);
                
                device.setStudent(student); 
            }
            // 정보 업데이트 (마지막 활성 시간 포함)
            device.updateDeviceInfo(platform, request.getModelName(), request.getUserAgent());
        } else {
            // 신규 등록 시 개수 제한 체크
            validateDeviceLimit(studentId);

            // 신규 등록
            StudentDevice newDevice = StudentDevice.builder()
                    .student(student)
                    .fcmToken(request.getFcmToken())
                    .platform(platform)
                    .modelName(request.getModelName())
                    .userAgent(request.getUserAgent())
                    .lastActiveAt(LocalDateTime.now())
                    .build();
            studentDeviceRepository.save(newDevice);
        }
    }

    private void validateDeviceLimit(String studentId) {
        List<StudentDevice> devices = studentDeviceRepository.findByStudentStudentId(studentId);
        if (devices.size() >= MAX_DEVICE_COUNT) {
            throw new BaseException(ErrorCode.DEVICE_LIMIT_EXCEEDED);
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
    public void deleteDeviceById(String studentId, Long deviceId) {
        StudentDevice device = studentDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new BaseException(ErrorCode.DEVICE_NOT_FOUND));

        // 본인의 디바이스인지 검증
        if (!device.getStudent().getStudentId().equals(studentId)) {
            throw new BaseException(ErrorCode.AUTH_SECURITY_FORBIDDEN_ACCESS);
        }

        studentDeviceRepository.delete(device);
    }

    @Override
    @Transactional
    public void deleteDeviceByToken(String studentId, String fcmToken) {
        StudentDevice device = studentDeviceRepository.findByFcmToken(fcmToken)
                .orElseThrow(() -> new BaseException(ErrorCode.DEVICE_NOT_FOUND));

        // 본인의 디바이스인지 검증
        if (!device.getStudent().getStudentId().equals(studentId)) {
            throw new BaseException(ErrorCode.AUTH_SECURITY_FORBIDDEN_ACCESS);
        }

        studentDeviceRepository.delete(device);
    }
}
