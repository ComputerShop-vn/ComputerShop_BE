package sp26.group3.computer.sba301_computershop.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudinaryService {
    
    String uploadBrandLogo(MultipartFile file);
    
    String uploadProductImage(MultipartFile file);
    
    List<String> uploadProductImages(MultipartFile[] files);
    
    void deleteFile(String fileUrl);
}
