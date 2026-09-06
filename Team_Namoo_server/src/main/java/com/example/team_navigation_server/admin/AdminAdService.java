package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.ad.Ad;
import com.example.team_navigation_server.ad.AdRepository;
import com.example.team_navigation_server.ad.AdSide;
import com.example.team_navigation_server.member.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminAdService {

    // SVG는 XSS 위험이 있어 허용하지 않는다 (ADMIN_CONSOLE_BACKEND_TODO 4-3).
    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp"
    );
    private static final long MAX_IMAGE_SIZE = 2L * 1024 * 1024;
    private static final Duration PRESIGN_EXPIRY = Duration.ofMinutes(5);

    private final AdRepository adRepository;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final String region;
    private final String cloudfrontDomain;

    public AdminAdService(AdRepository adRepository,
                           S3Presigner s3Presigner,
                           @Value("${aws.s3.bucket:}") String bucket,
                           @Value("${aws.s3.region}") String region,
                           @Value("${aws.s3.cloudfront-domain:}") String cloudfrontDomain) {
        this.adRepository = adRepository;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.region = region;
        this.cloudfrontDomain = cloudfrontDomain;
    }

    public List<AdminAdResponse> list(String page) {
        List<Ad> ads = (page == null || page.isBlank())
                ? adRepository.findAllByOrderByCreatedAtDesc()
                : adRepository.findAllByPageOrderByCreatedAtDesc(page);
        return ads.stream().map(AdminAdResponse::new).toList();
    }

    public AdminAdResponse create(AdminAdCreateRequest request, Member actingAdmin) {
        AdSide side = parseSide(request.getSide());
        if (request.getStartAt() != null && request.getEndAt() != null
                && !request.getEndAt().isAfter(request.getStartAt())) {
            throw new IllegalArgumentException("종료일시는 시작일시 이후여야 합니다.");
        }
        Ad ad = new Ad(request.getPage(), side, request.getImageUrl(), request.getLinkUrl(),
                request.getStartAt(), request.getEndAt(), actingAdmin);
        return new AdminAdResponse(adRepository.save(ad));
    }

    public void delete(Long id) {
        if (!adRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 광고입니다.");
        }
        // S3 오브젝트 정리는 보류 - ADMIN_CONSOLE_BACKEND_TODO 4-2 🟡, 필요해지면 S3 DeleteObject 추가.
        adRepository.deleteById(id);
    }

    public AdminAdImagePresignResponse presignImageUpload(AdminAdImagePresignRequest request) {
        if (bucket.isBlank()) {
            throw new IllegalStateException("AWS_S3_BUCKET 환경변수가 설정되지 않았습니다.");
        }
        String ext = ALLOWED_CONTENT_TYPES.get(request.getContentType());
        if (ext == null) {
            throw new IllegalArgumentException("이미지 형식은 png/jpg/webp만 허용됩니다.");
        }
        if (request.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("이미지 용량은 2MB를 초과할 수 없습니다.");
        }

        // 프론트가 아직 page/side를 안 넘겨서(doc 4-3 요청 계약이 contentType/size뿐) 폴더 없이 uuid 파일명만 쓴다.
        String key = "ads/" + UUID.randomUUID() + "." + ext;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(request.getContentType())
                .build();
        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(PRESIGN_EXPIRY)
                        .putObjectRequest(putObjectRequest)
                        .build());

        String imageUrl = cloudfrontDomain.isBlank()
                ? "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key)
                : "https://%s/%s".formatted(cloudfrontDomain, key);

        return new AdminAdImagePresignResponse(presigned.url().toString(), imageUrl);
    }

    private AdSide parseSide(String value) {
        try {
            return AdSide.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("슬롯 위치는 LEFT/RIGHT 중 하나여야 합니다.");
        }
    }
}
