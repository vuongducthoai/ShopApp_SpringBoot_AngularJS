package com.project.shopapp.controllers;

import com.github.javafaker.Faker;
import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.responses.ProductListResponse;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ) {
        //Tạo Pageable từ thông tin trang và gioi hạn
        PageRequest pageRequest = PageRequest.of(page,
                limit,
                Sort.by("createdAt").descending());
        Page<ProductResponse> productPage = productService.getAllProducts(pageRequest);
        //Lấy tổng số trang
        int totalPages  = productPage.getTotalPages();
        List<ProductResponse> products = productPage.getContent();
        ProductListResponse productListResponse =
                ProductListResponse
                        .builder()
                        .products(products)
                        .totalPages(totalPages)
                        .build();
        return  ResponseEntity.ok(productListResponse);
    }



    @GetMapping("/allProduct")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    //http://localhost:8080/api/products/6
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(
            @PathVariable("id") Long productId
    ) {
        Product existingProduct;
        try {
            existingProduct = productService.getProductById(productId);
            return ResponseEntity.ok(ProductResponse.fromProduct(existingProduct));
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") long id) {
        try {
            productService.deleteProduct(id);
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("Product deleted with ID: " + id);
    }

    @PostMapping(value = "")
    //POST: http://localhost:8088/v1/api/products
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result
    ) {
        System.out.println(productDTO.getDescription());
        try {
            System.out.println("CategoryID: " + productDTO.getCategoryId());
            //Validate attribute ProductDTO
            if(result.hasErrors()) {
                List<String> errorsMessage = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorsMessage);
            }

            //Save product into database
            Product newProduct = productService.createProduct(productDTO);
            return ResponseEntity.ok(newProduct);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //POST: http://localhost:8088/v1/api/products
    public ResponseEntity<?> uploadImages(
            @PathVariable("id") Long productId,
            @RequestParam("files") List<MultipartFile> files)
            throws Exception {
        //Get List file image of productDTO
        Product existingProduct = productService.getProductById(productId);
        files = files == null ? new ArrayList<MultipartFile>() : files;
        if(files.size() > ProductImage.MAXIMUM_IMAGE_PER_PRODUCT) {
            return ResponseEntity.badRequest().body("You can only upload maximum 5 images");
        }
        List<ProductImage> productImageList = new ArrayList<>();
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
                    existingProduct.getId(),
                    ProductImageDTO.builder()
                            .imageUrl(fileName)
                            .build()
            );
            productImageList.add(productImage);
        }
        return ResponseEntity.ok(productImageList);
    }

    public String storeFile(
            MultipartFile file
    ) throws IOException {
        if(isImageFile(file) && file.getOriginalFilename() != null) {
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
        } else {
            throw new IllegalArgumentException("Invalid image file");
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType  = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

   // @PostMapping("/generateFakeProducts")
    private ResponseEntity<?> generateFakeProducts(){
        Faker faker = new Faker();
        for(int i = 0; i < 1_000_000; i++) {
            String productName = faker.commerce().productName();
            if(productService.existsByName(productName)) {
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float) faker.number().numberBetween(10, 90_000_000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId(faker.number().numberBetween(1, 4)) // Xóa dấu đóng ngoặc thừa
                    .build();
            try {
                productService.createProduct(productDTO);

            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Fake Products created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable long id,
            @RequestBody ProductDTO productDTO){

        try {
            Product updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/search")
    public List<Product> searchPhones(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

}
