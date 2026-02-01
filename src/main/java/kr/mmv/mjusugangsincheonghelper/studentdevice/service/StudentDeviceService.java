package kr.mmv.mjusugangsincheonghelper.studentdevice.service;

import java.util.List;

import kr.mmv.mjusugangsincheonghelper.studentdevice.dto.DeviceRegisterRequestDto;
import kr.mmv.mjusugangsincheonghelper.studentdevice.dto.DeviceResponseDto;

public interface StudentDeviceService {
    void registerDevice(String studentId, DeviceRegisterRequestDto request);
    List<DeviceResponseDto> getMyDevices(String studentId);
    void deleteDevice(String studentId, String fcmToken);
}
