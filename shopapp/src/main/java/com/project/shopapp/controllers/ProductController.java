package com.project.shopapp.controllers;

import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.services.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    @GetMapping("")
    public ResponseEntity<String> getProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ) {
        return ResponseEntity.ok("getProducts here");
    }


    //http://localhost:8080/api/products/6
    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(
            @PathVariable("id") String productId
    ) {
        return ResponseEntity.ok("Product with ID: " + productId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") long id) {
        return ResponseEntity.ok("Product deleted with ID: " + id);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //POST: http://localhost:8088/v1/api/products
    public ResponseEntity<?> createProduct(
            @Valid @ModelAttribute("productDTO") ProductDTO productDTO,
            BindingResult result
    ) {
        try {
            if(result.hasErrors()) {
                List<String> errorsMessage = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorsMessage);
            }

            Product newProduct = productService.createProduct(productDTO);
            List<MultipartFile> files = productDTO.getFiles();
            files = files == null ? new ArrayList<MultipartFile>() : files;
            for (MultipartFile file : files) {
                if(file.getSize() == 0){
                    continue;
                }
                    // Kiểm tra kích thước file và định dạng
                    if(file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File is too large");
                    }

                    String contentType = file.getContentType();
                    if(contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body("File must be image");
                    }

                    // Lưu file và cập nhật thumbnail trong DTO
                    String fileName = storeFile(file);
                   // Lưu vào DB hoặc xử lý thêm nếu cần
                 ProductImage productImage = productService.createProductImage(
                            newProduct.getId(),
                            ProductImageDTO.builder()
                                    .imageUrl(fileName)
                                    .build()
                    );
            }

            return ResponseEntity.ok("Product created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    public String storeFile(
            MultipartFile file
    )
            throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        //Thêm UUID vào trước tên file để đảm bảo tên file là duy nhất mục đích để không bị ghi đè nếu upload cùng tên file đã tồn tại trên sever.
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        //Đường dẫn đến thư mục mà ban muốn lưu file
        java.nio.file.Path uploadDir = Paths.get("uploads");
        if(!Files.exists(uploadDir)) { //Tao thu muc nay neu chua ton tai uploads
            Files.createDirectories(uploadDir);
        }
        //Đường dẫn đầy đủ đến file
        java.nio.file.Path  destinations = Paths.get(uploadDir.toString(), uniqueFileName);

        //Sao chep file vao thu muc dich
        Files.copy(file.getInputStream(), destinations, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFileName;
    }
}
