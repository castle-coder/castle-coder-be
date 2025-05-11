package com.castle.aws.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.castle.code.error.S3ErrorCode;
import com.castle.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * S3에 이미지를 업로드 및 삭제하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class S3ImageService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public final AmazonS3 amazonS3;

    /**
     * S3에 파일 업로드
     */
    public String uploadImage(MultipartFile file, String fileName) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);

            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new S3Exception(S3ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * S3에서 파일 삭제
     */
    public void deleteImage(String imageUrl) {
        // TODO: S3 Bucket 주기적 청소 시 해당 메서드 삭제 고려
        if (!doesImageExist(imageUrl)) {
            throw new S3Exception(S3ErrorCode.IMAGE_NOT_FOUND_ERROR);
        }

        String fileName = getDecodedFileName(imageUrl);

        amazonS3.deleteObject(bucketName, fileName);
    }

    /**
     * S3에 URL이 있는지 확인
     */
    private boolean doesImageExist(String imageUrl) {
        String fileName = getDecodedFileName(imageUrl);

        return amazonS3.doesObjectExist(bucketName, fileName);
    }

    /**
     * 디코딩된 파일 이름 반환
     */
    private String getDecodedFileName(String imageUrl) {
        try {
            // "https://bucket.s3.amazonaws.com/Chat/123/chatImage/abcd.png"
            // → "/Chat/123/chatImage/abcd.png"
            String path = new java.net.URI(imageUrl).getPath();

            // 앞의 '/' 제거하고 디코딩
            return URLDecoder.decode(path.substring(1), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new S3Exception(S3ErrorCode.IMAGE_NOT_FOUND_ERROR);
        }
    }
}