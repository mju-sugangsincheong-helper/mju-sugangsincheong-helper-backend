package kr.mmv.mjusugangsincheonghelper.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.exception.BaseException;
import kr.mmv.mjusugangsincheonghelper.global.entity.Section;
import kr.mmv.mjusugangsincheonghelper.global.entity.Student;
import kr.mmv.mjusugangsincheonghelper.global.entity.Subscription;
import kr.mmv.mjusugangsincheonghelper.global.repository.SectionRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.SubscriptionRepository;
import kr.mmv.mjusugangsincheonghelper.subscription.dto.SubscriptionRequestDto;
import kr.mmv.mjusugangsincheonghelper.subscription.dto.SubscriptionResponseDto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 구독 서비스
 * PK: sectioncls (String)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository userRepository;

    private static final int MAX_SUBSCRIPTIONS = 50;

    /**
     * 구독 추가
     */
    @Transactional
    public SubscriptionResponseDto subscribe(String studentId, SubscriptionRequestDto request) {
        Student user = userRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_USER_NOT_FOUND));

        Section section = sectionRepository.findById(request.getSectioncls())
                .orElseThrow(() -> new BaseException(ErrorCode.SECTION_NOT_FOUND));

        // 이미 구독 중인지 확인
        if (subscriptionRepository.existsByUserAndSection(user, section)) {
            throw new BaseException(ErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
        }

        // 구독 수 제한 확인
        long currentCount = subscriptionRepository.countByUser(user);
        if (currentCount >= MAX_SUBSCRIPTIONS) {
            throw new BaseException(ErrorCode.SUBSCRIPTION_LIMIT_EXCEEDED);
        }

        Subscription subscription = Subscription.builder()
                .user(user)
                .section(section)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("User {} subscribed to section {} ({})", studentId, section.getCurinm(), section.getSectioncls());

        return SubscriptionResponseDto.from(saved);
    }

    /**
     * 구독 취소
     */
    @Transactional
    public void unsubscribe(String studentId, String sectioncls) {
        Student user = userRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_USER_NOT_FOUND));

        Section section = sectionRepository.findById(sectioncls)
                .orElseThrow(() -> new BaseException(ErrorCode.SECTION_NOT_FOUND));

        Subscription subscription = subscriptionRepository.findByUserAndSection(user, section)
                .orElseThrow(() -> new BaseException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscriptionRepository.delete(subscription);
        log.info("User {} unsubscribed from section {} ({})", studentId, section.getCurinm(), section.getSectioncls());
    }

    /**
     * 내 구독 목록 조회
     */
    public List<SubscriptionResponseDto> getMySubscriptions(String studentId) {
        return subscriptionRepository.findByStudentId(studentId).stream()
                .map(SubscriptionResponseDto::from)
                .collect(Collectors.toList());
    }
}
