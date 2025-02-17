package store.buzzbook.core.service.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final AmazonS3 amazonS3;
	private final String bucketName = "ps-store-bucket";

	public ResponseEntity<List<String>> uploadImages(List<MultipartFile> files) {
		try {
			List<String> uploadedUrls = files.stream().map(file -> {
				try {
					return uploadFileToS3(file);
				} catch (IOException e) {
					throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
				}
			}).collect(Collectors.toList());

			return ResponseEntity.ok(uploadedUrls);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(null);
		}
	}

	private String uploadFileToS3(MultipartFile file) throws IOException {
		String key = "images/" + file.getOriginalFilename();
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());

		amazonS3.putObject(bucketName, key, file.getInputStream(), metadata);
		return amazonS3.getUrl(bucketName, key).toString();
	}
}
