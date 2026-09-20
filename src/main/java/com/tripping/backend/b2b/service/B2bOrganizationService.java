package com.tripping.backend.b2b.service;

import com.tripping.backend.b2b.dto.*;
import com.tripping.backend.b2b.repository.OrgApiKeyRepository;
import com.tripping.backend.b2b.repository.OrgNotificationSettingRepository;
import com.tripping.backend.b2b.repository.OrganizationRepository;
import com.tripping.backend.entity.OrgApiKey;
import com.tripping.backend.entity.OrgNotificationSetting;
import com.tripping.backend.entity.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

// 조직 설정 화면(기관 정보 / 알림 설정 / API 키)을 담당한다.
@RequiredArgsConstructor
@Service
public class B2bOrganizationService {

    private static final String KEY_PREFIX = "tp_";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrganizationRepository organizationRepository;
    private final OrgNotificationSettingRepository notificationRepository;
    private final OrgApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public OrgProfileResponse profile(Long orgId) {
        return OrgProfileResponse.from(organization(orgId));
    }

    @Transactional
    public OrgProfileResponse updateProfile(Long orgId, OrgProfileUpdateRequest request) {
        Organization org = organization(orgId);

        if (!isBlank(request.getOrgName())) org.setOrgName(request.getOrgName().trim());
        if (!isBlank(request.getOrgType())) org.setOrgType(request.getOrgType().trim());
        if (!isBlank(request.getManagerName())) org.setManagerName(request.getManagerName().trim());
        if (request.getDepartment() != null) org.setDepartment(emptyToNull(request.getDepartment()));
        if (request.getDescription() != null) org.setDescription(emptyToNull(request.getDescription()));
        if (request.getLogoUrl() != null) org.setLogoUrl(emptyToNull(request.getLogoUrl()));

        organizationRepository.save(org);
        return OrgProfileResponse.from(org);
    }

    // 설정 행이 없으면 기본값으로 만들어 준다. 화면에서는 늘 값이 있는 것처럼 다룬다.
    @Transactional
    public NotificationSettingResponse notifications(Long orgId) {
        return NotificationSettingResponse.from(settingOf(orgId));
    }

    @Transactional
    public NotificationSettingResponse updateNotifications(
            Long orgId, NotificationSettingUpdateRequest request) {
        OrgNotificationSetting setting = settingOf(orgId);

        if (request.getNotifyTrend() != null) setting.setNotifyTrend(request.getNotifyTrend());
        if (request.getNotifyReport() != null) setting.setNotifyReport(request.getNotifyReport());

        notificationRepository.save(setting);
        return NotificationSettingResponse.from(setting);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> apiKeys(Long orgId) {
        return apiKeyRepository.findByOrgIdAndRevokedFalseOrderByCreatedAtDesc(orgId).stream()
                .map(ApiKeyResponse::from)
                .toList();
    }

    // 키 원문은 이 응답에만 담기고 저장되지 않는다. 화면에서 한 번 보여준 뒤에는
    // 다시 확인할 수 없으므로, 프론트에서 복사하도록 안내해야 한다.
    @Transactional
    public ApiKeyResponse issueApiKey(Long orgId, ApiKeyCreateRequest request) {
        String label = (request == null || isBlank(request.getLabel()))
                ? "데이터 연동 키"
                : request.getLabel().trim();

        String plainKey = generateKey();
        OrgApiKey key = OrgApiKey.builder()
                .orgId(orgId)
                .label(label)
                .keyPrefix(plainKey.substring(0, 11))
                .keyHash(passwordEncoder.encode(plainKey))
                .build();
        apiKeyRepository.save(key);

        return ApiKeyResponse.of(key, plainKey);
    }

    // 실제로 지우지 않고 revoked만 세운다. 어떤 키가 언제 폐기됐는지 남겨두기 위함.
    @Transactional
    public void revokeApiKey(Long orgId, Long keyId) {
        OrgApiKey key = apiKeyRepository.findByKeyIdAndOrgId(keyId, orgId)
                .orElseThrow(() -> new NoSuchElementException("API 키를 찾을 수 없습니다."));
        key.setRevoked(true);
        apiKeyRepository.save(key);
    }

    private Organization organization(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new NoSuchElementException("기관을 찾을 수 없습니다."));
    }

    private OrgNotificationSetting settingOf(Long orgId) {
        return notificationRepository.findByOrgId(orgId)
                .orElseGet(() -> notificationRepository.save(
                        OrgNotificationSetting.builder().orgId(orgId).build()));
    }

    private String generateKey() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String emptyToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
