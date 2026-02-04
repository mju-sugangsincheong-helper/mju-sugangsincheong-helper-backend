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
    
    private static final int MAX_DEVICE_COUNT = 100;

    @Override
    @Transactional
    public void registerDevice(String studentId, DeviceRegisterRequestDto request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_USER_NOT_FOUND));

        String newToken = request.getFcmToken();
        String oldToken = request.getOldToken();

        // 1. Token Rotation (oldToken이 있는 경우)
        if (oldToken != null && !oldToken.isEmpty()) {
            Optional<StudentDevice> oldDeviceOpt = studentDeviceRepository.findByFcmToken(oldToken);

            if (oldDeviceOpt.isPresent()) {
                StudentDevice oldDevice = oldDeviceOpt.get();
                // 본인 기기인지 확인
                if (oldDevice.getStudent().getStudentId().equals(studentId)) {
                    log.info("Rotate FCM Token: user={}, old={}, new={}", studentId, oldToken, newToken);

                    // 새 토큰이 이미 DB에 존재하는지 확인 (충돌 방지)
                    Optional<StudentDevice> collisionCheck = studentDeviceRepository.findByFcmToken(newToken);
                    if (collisionCheck.isPresent()) {
                        // 새 토큰이 이미 존재함 -> 기존 기기(oldDevice)는 더 이상 유효하지 않으므로 삭제
                        // (이미 새 토큰으로 등록된 기기가 있으므로 업데이트 대신 삭제 처리)
                        log.warn("Target token already exists during rotation. Deleting old device.");
                        studentDeviceRepository.delete(oldDevice);
                        // 이후 로직에서 existingDevice(newToken) 처리로 넘어감
                    } else {
                        // 정상 Rotation: 기존 레코드 업데이트 (이 메서드가 기기를 활성화 상태로 변경)
                        oldDevice.updateFcmToken(newToken);
                        oldDevice.updateDeviceInfo(
                                request.getOsFamily(),
                                request.getOsVersion(),
                                request.getBrowserName(),
                                request.getBrowserVersion(),
                                request.getUserAgent()
                        );
                        return; // 종료
                    }
                } else {
                    log.warn("Token rotation failed: ownership mismatch. user={}, owner={}", studentId, oldDevice.getStudent().getStudentId());
                    // 소유권 불일치 시, 신규 등록 로직으로 진행
                }
            }
        }

        Optional<StudentDevice> existingDevice = studentDeviceRepository.findByFcmToken(newToken);

        if (existingDevice.isPresent()) {
            StudentDevice device = existingDevice.get();
            // 소유자가 변경된 경우 (다른 계정으로 로그인) - 기기 소유권 이전
            if (!device.getStudent().getStudentId().equals(studentId)) {
                log.info("Device ownership changed: token={}, oldUser={}, newUser={}", 
                        newToken, device.getStudent().getStudentId(), studentId);
                
                // 기존 사용자의 기기 개수는 감소하므로 체크 불필요
                // 새 사용자의 기기 개수 체크 필요 (활성 기기만 체크)
                validateDeviceLimit(studentId);
                
                device.setStudent(student); 
            }
            // 정보 업데이트 (기기를 활성화 상태로 변경)
            device.updateDeviceInfo(
                    request.getOsFamily(),
                    request.getOsVersion(),
                    request.getBrowserName(),
                    request.getBrowserVersion(),
                    request.getUserAgent()
            );
        } else {
            // 신규 등록 시 개수 제한 체크 (활성 기기만 체크)
            validateDeviceLimit(studentId);

            // 신규 등록
            StudentDevice newDevice = StudentDevice.builder()
                    .student(student)
                    .fcmToken(newToken)
                    .osFamily(request.getOsFamily())
                    .osVersion(request.getOsVersion())
                    .browserName(request.getBrowserName())
                    .browserVersion(request.getBrowserVersion())
                    .userAgent(request.getUserAgent())
                    .lastActiveAt(LocalDateTime.now())
                    .build();
            studentDeviceRepository.save(newDevice);
        }
    }

    private void validateDeviceLimit(String studentId) {
        List<StudentDevice> activeDevices = studentDeviceRepository.findActiveByStudentId(studentId);
        if (activeDevices.size() >= MAX_DEVICE_COUNT) {
            throw new BaseException(ErrorCode.DEVICE_LIMIT_EXCEEDED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponseDto> getMyDevices(String studentId) {
        // 사용자에게는 비활성화된 기기도 모두 보여줌 (Soft Delete 시각화)
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
