package sp26.group3.computer.sba301_computershop.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.service.CloudinaryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadBrandLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Attempted to upload empty brand logo");
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "brands",
                            "resource_type", "image"
                    )
            );
            
            String fileUrl = result.get("secure_url").toString();
            log.info("Brand logo uploaded to Cloudinary successfully: {}", fileUrl);
            
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to upload brand logo to Cloudinary", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public String uploadProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Attempted to upload empty product image");
            return null;
        }

        try {
            String publicId = "products/" + UUID.randomUUID().toString();

            String resourceType = "auto";
            if (file.getContentType() != null) {
                if (file.getContentType().startsWith("video/")) {
                    resourceType = "video";
                } else if (file.getContentType().startsWith("image/")) {
                    resourceType = "image";
                }
            }

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", resourceType,
                            "folder", "computer-shop"
                    )
            );

            String fileUrl = (String) uploadResult.get("secure_url");
            log.info("Product image uploaded to Cloudinary successfully: {}", fileUrl);

            return fileUrl;

        } catch (IOException ex) {
            log.error("Failed to upload product image to Cloudinary", ex);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public List<String> uploadProductImages(MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();

        if (files == null || files.length == 0) {
            log.warn("No product images provided to upload");
            return fileUrls;
        }

        for (MultipartFile file : files) {
            String fileUrl = uploadProductImage(file);
            if (fileUrl != null) {
                fileUrls.add(fileUrl);
            }
        }

        log.info("Uploaded {} product images to Cloudinary successfully", fileUrls.size());
        return fileUrls;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("Attempted to delete file with empty URL");
            return;
        }

        try {
            String publicId = extractPublicIdFromUrl(fileUrl);

            if (publicId != null) {
                String resourceType = "image";
                if (fileUrl.contains("/video/")) {
                    resourceType = "video";
                } else if (fileUrl.contains("/raw/")) {
                    resourceType = "raw";
                }

                Map<String, Object> result = cloudinary.uploader().destroy(
                        publicId,
                        ObjectUtils.asMap("resource_type", resourceType)
                );

                log.info("File deleted from Cloudinary: {} - Result: {}", publicId, result.get("result"));
            }

        } catch (Exception ex) {
            log.error("Failed to delete file from Cloudinary: {}", fileUrl, ex);
            throw new AppException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    private String extractPublicIdFromUrl(String fileUrl) {
        try {
            String[] parts = fileUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String afterUpload = parts[1];

            int slashIndex = afterUpload.indexOf('/');
            if (slashIndex != -1) {
                afterUpload = afterUpload.substring(slashIndex + 1);
            }

            int lastDotIndex = afterUpload.lastIndexOf('.');
            if (lastDotIndex != -1) {
                afterUpload = afterUpload.substring(0, lastDotIndex);
            }

            return afterUpload;

        } catch (Exception ex) {
            log.error("Failed to extract public_id from URL: {}", fileUrl, ex);
            return null;
        }
    }
}
