package kr.mmv.mjusugangsincheonghelper.notification.consumer.service;

import com.google.firebase.messaging.*;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentDeviceRepository;
import kr.mmv.mjusugangsincheonghelper.notification.common.dto.FcmMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FCM 실제 발송 및 사후 처리를 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSenderService {

    private final StudentDeviceRepository studentDeviceRepository;
    private static final int BATCH_SIZE = 450;

    /**
     * 개인화된 메시지 리스트를 배치로 나누어 발송합니다.
     */
    @Transactional
    public void sendMessages(List<FcmMessageDto> messageDtos) {
        if (messageDtos == null || messageDtos.isEmpty()) {
            return;
        }

        // log.info("[FCM] Starting to send {} messages", messageDtos.size());

        for (int i = 0; i < messageDtos.size(); i += BATCH_SIZE) {
            List<FcmMessageDto> batch = messageDtos.subList(i, Math.min(i + BATCH_SIZE, messageDtos.size()));
            sendBatch(batch);
        }
    }

    private void sendBatch(List<FcmMessageDto> batch) {
        try {
            List<Message> messages = batch.stream()
                    .map(this::toMessage)
                    .collect(Collectors.toList());

            BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages);
            handleBatchResponse(response, batch);
        } catch (Exception e) {
            log.error("[FCM] Critical error sending batch", e);
        }
    }

    private void handleBatchResponse(BatchResponse response, List<FcmMessageDto> batch) {
        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            List<String> invalidTokens = new ArrayList<>();

            for (int i = 0; i < responses.size(); i++) {
                SendResponse res = responses.get(i);
                if (!res.isSuccessful()) {
                    String errorCode = res.getException().getMessagingErrorCode().name();
                    String token = batch.get(i).getToken();
                    
                    log.warn("[FCM] Send failed for token: {} - Error: {}", 
                            token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "TOPIC", 
                            errorCode);

                    if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
                        if (token != null) {
                            invalidTokens.add(token);
                        }
                    }
                }
            }

            if (!invalidTokens.isEmpty()) {
                cleanupInvalidTokens(invalidTokens);
            }
        }
        log.info("[FCM] Send Batch processed: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());
    }

    private void cleanupInvalidTokens(List<String> tokens) {
        for (String token : tokens) {
            studentDeviceRepository.findByFcmToken(token).ifPresent(device -> {
                device.deactivate("FCM_ERROR_UNREGISTERED");
                studentDeviceRepository.save(device);
                log.info("[FCM] Deactivated invalid token in DB: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
            });
        }
    }

    private Message toMessage(FcmMessageDto dto) {
        Message.Builder builder = Message.builder();

        if (dto.getToken() != null) {
            builder.setToken(dto.getToken());
        } else if (dto.getTopic() != null) {
            builder.setTopic(dto.getTopic());
        }

        if (dto.getNotification() != null) {
            builder.setNotification(Notification.builder()
                    .setTitle(dto.getNotification().getTitle())
                    .setBody(dto.getNotification().getBody())
                    .setImage(dto.getNotification().getImage())
                    .build());
        }

        if (dto.getData() != null) {
            builder.putAllData(dto.getData());
        }

        if (dto.getWebpush() != null) {
            builder.setWebpushConfig(toWebpushConfig(dto.getWebpush()));
        }

        return builder.build();
    }

    private WebpushConfig toWebpushConfig(FcmMessageDto.WebpushDto dto) {
        WebpushConfig.Builder builder = WebpushConfig.builder();
        
        if (dto.getHeaders() != null) {
            builder.putAllHeaders(dto.getHeaders());
        }

        if (dto.getData() != null) {
            builder.putAllData(dto.getData());
        }

        if (dto.getNotification() != null) {
            WebpushNotification.Builder notifBuilder = WebpushNotification.builder()
                    .setTitle(dto.getNotification().getTitle())
                    .setBody(dto.getNotification().getBody())
                    .setIcon(dto.getNotification().getIcon())
                    .setImage(dto.getNotification().getImage())
                    .setLanguage(dto.getNotification().getLanguage())
                    .setRenotify(dto.getNotification().getRenotify() != null && dto.getNotification().getRenotify())
                    .setRequireInteraction(dto.getNotification().getRequireInteraction() != null && dto.getNotification().getRequireInteraction());

            if (dto.getNotification().getTimestamp() != null) {
                notifBuilder.setTimestampMillis(dto.getNotification().getTimestamp());
            }

            if (dto.getNotification().getActions() != null) {
                for (FcmMessageDto.WebpushDto.ActionDto action : dto.getNotification().getActions()) {
                    notifBuilder.addAction(new WebpushNotification.Action(action.getAction(), action.getTitle(), action.getIcon()));
                }
            }
            builder.setNotification(notifBuilder.build());
        }

        if (dto.getFcmOptions() != null) {
            builder.setFcmOptions(WebpushFcmOptions.withLink(dto.getFcmOptions().getLink()));
        }

        return builder.build();
    }
}
