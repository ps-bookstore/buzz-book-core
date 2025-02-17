package store.buzzbook.core.controller.image;

import lombok.RequiredArgsConstructor;
import store.buzzbook.core.service.image.ImageService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageUploadController {

	private final ImageService imageService;

	@PostMapping("/upload")
	public ResponseEntity<List<String>> uploadImages(@RequestPart("files") List<MultipartFile> files) {
		return imageService.uploadImages(files);
	}
}
