package com.castle.aws.s3.service;

import com.castle.code.error.S3ErrorCode;
import com.castle.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUrlService {

    private final S3ImageService s3ImageService;

    /**
     * S3에 사진을 업로드합니다.
     */
    @Transactional
    public String uploadImage(MultipartFile uploadImage, Long memberId) {
        String uuid = UUID.randomUUID().toString().replace("-", ""); // 하이픈 제거
        String shortUuid = uuid.substring(0, 8); // 앞 8자리만 사용
        String fileName = "Chat/" + memberId + "/chatImage/" + shortUuid;
        // 파일 확장자 검사
        if (!isImageFile(uploadImage)) {
            throw new S3Exception(S3ErrorCode.INVALID_IMAGE_EXTENSION);
        }
        return s3ImageService.uploadImage(uploadImage, fileName);
    }

    /**
     * S3에서 사진을 삭제합니다.
     */
    public void deleteImage(String imageUrl) {
        s3ImageService.deleteImage(imageUrl);
    }

    /**
     * 파일 확장자가 이미지 파일인지 확인합니다.
     */
    private boolean isImageFile(MultipartFile uploadImage) {
        String filename = uploadImage.getOriginalFilename();
        String fileExtension = Objects.requireNonNull(filename).substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return fileExtension.equals("jpg") || fileExtension.equals("jpeg") || fileExtension.equals("png");
    }
}
