package com.poultry.platform.service;

import com.poultry.platform.domain.AppUser;
import com.poultry.platform.domain.NotificationLog;
import com.poultry.platform.repository.AppUserRepository;
import com.poultry.platform.repository.NotificationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final AppUserRepository appUserRepository;

    public NotificationService(NotificationLogRepository notificationLogRepository, AppUserRepository appUserRepository) {
        this.notificationLogRepository = notificationLogRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public void notifyUser(Long userId, String title, String body, String type, Long refId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("알림 대상 사용자가 없습니다."));
        NotificationLog log = new NotificationLog();
        log.setUser(user);
        log.setTitle(title);
        log.setBody(body);
        log.setType(type);
        log.setRefId(refId);
        notificationLogRepository.save(log);
    }

    @Transactional
    public void notifyOrganization(Long organizationId, String title, String body, String type, Long refId) {
        List<AppUser> users = appUserRepository.findByOrganizationId(organizationId);
        for (AppUser user : users) {
            notifyUser(user.getId(), title, body, type, refId);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationLog> myNotifications(Long userId) {
        return notificationLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        NotificationLog log = notificationLogRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림이 없습니다."));
        if (!log.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        log.setReadFlag(true);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationLogRepository.countByUserIdAndReadFlagFalse(userId);
    }
}
