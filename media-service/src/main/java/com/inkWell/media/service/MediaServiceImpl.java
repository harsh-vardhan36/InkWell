package com.inkWell.media.service;

import com.inkWell.media.domain.entity.Media;
import com.inkWell.media.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final S3Client s3Client;
    private final org.springframework.web.client.RestTemplate restTemplate;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Override
    public Media uploadMedia(MultipartFile file, Long uploaderId) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);

        Media media = Media.builder()
                .fileName(fileName)
                .originalName(file.getOriginalFilename())
                .fileUrl(fileUrl)
                .mimeType(file.getContentType())
                .sizeKb(file.getSize() / 1024)
                .uploaderId(uploaderId)
                .isDeleted(false)
                .build();

        return mediaRepository.save(media);
    }

    @Override
    public Media uploadFromUrl(String url, Long uploaderId) throws IOException {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IOException("Failed to fetch image from URL: " + url);
        }

        byte[] bytes = response.getBody();
        String contentType = response.getHeaders().getContentType() != null ? response.getHeaders().getContentType().toString() : "image/jpeg";
        String extension = contentType.contains("/") ? contentType.split("/")[1] : "jpg";
        String fileName = UUID.randomUUID().toString() + "." + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);

        Media media = Media.builder()
                .fileName(fileName)
                .originalName("url_upload_" + fileName)
                .fileUrl(fileUrl)
                .mimeType(contentType)
                .sizeKb((long) (bytes.length / 1024))
                .uploaderId(uploaderId)
                .isDeleted(false)
                .build();

        return mediaRepository.save(media);
    }

    @Override
    public Optional<Media> getMediaById(Long id) {
        return mediaRepository.findById(id);
    }

    @Override
    public List<Media> getMediaByUploader(Long uploaderId) {
        return mediaRepository.findByUploaderId(uploaderId);
    }

    @Override
    public List<Media> getMediaByPost(Long postId) {
        return mediaRepository.findByLinkedPostId(postId);
    }

    @Override
    public void deleteMedia(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        
        // Delete from S3
        s3Client.deleteObject(builder -> builder.bucket(bucketName).key(media.getFileName()));
        
        // Soft delete in DB
        media.setDeleted(true);
        mediaRepository.save(media);
    }

    @Override
    public Media updateAltText(Long id, String altText) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        media.setAltText(altText);
        return mediaRepository.save(media);
    }

    @Override
    public void linkToPost(Long mediaId, Long postId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        media.setLinkedPostId(postId);
        mediaRepository.save(media);
    }

    @Override
    public void unlinkFromPost(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        media.setLinkedPostId(null);
        mediaRepository.save(media);
    }

    @Override
    public List<Media> getAllMedia() {
        return mediaRepository.findAll();
    }

    @Override
    public void cleanupDeleted() {
        List<Media> deletedMedia = mediaRepository.findByIsDeleted(true);
        mediaRepository.deleteAll(deletedMedia);
    }
}
