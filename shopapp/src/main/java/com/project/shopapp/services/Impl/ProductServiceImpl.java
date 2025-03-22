package com.project.shopapp.services.Impl;

import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.InvalidParamException;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {
        System.out.println("productDTO: " + productDTO);
        Category existingCategory = categoryRepository.findById((long) productDTO.getCategoryId()).orElseThrow(() ->
                new DataNotFoundException(
                        "Cannot find category with id: " +
                                productDTO.getCategoryId()));

        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .description(productDTO.getDescription())
                .thumbnail(productDTO.getThumbnail())
                .category(existingCategory)
                .build();
        return productRepository.save(newProduct);
    }

    @Override
    public Product getProductById(Long productId) throws DataNotFoundException {
        return productRepository.findById(productId).
                orElseThrow(() -> new DataNotFoundException(
                "Cannot find product with id: " + productId
        ));
    }

    @Override
    public Page<ProductResponse> getAllProducts(PageRequest pageRequest) {
        // Lay danh sach san pham theo trang (page) va gioi han (limit)
        return productRepository.findAll(pageRequest).map(ProductResponse::fromProduct
        );
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAllProducts();
    }

    @Override
    public Product updateProduct(
            long id,
            ProductDTO productDTO
    )
            throws DataNotFoundException {
        Product existingProduct = getProductById(id);
        if(existingProduct != null){
            //Copy cac thuoc tinh tu DTO - Product
            // Co the su dung ModelMapper
            Category existingCategory = categoryRepository.findById((long) productDTO.getCategoryId()).orElseThrow(() ->
                    new DataNotFoundException(
                            "Cannot find category with id: " +
                                    productDTO.getCategoryId()));
             existingProduct.setName(productDTO.getName());
             existingProduct.setPrice(productDTO.getPrice());
             existingProduct.setCategory(existingCategory);
             existingProduct.setDescription(productDTO.getDescription());
             existingProduct.setThumbnail(productDTO.getThumbnail());
             return productRepository.save(existingProduct);
        }
        return null;
    }

    @Override
    public void deleteProduct(long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if(optionalProduct.isPresent()){
            productRepository.deleteById(id);
        }
    }

    @Override
    public boolean existsByName(String name) {
       return productRepository.existsByName(name);
    }

    @Override
    public ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO
    ) throws Exception {
        Product existingProduct =
                productRepository.findById(productId).orElseThrow(
                () -> new DataNotFoundException("Cannot find product with id: " + productImageDTO.getProductId()));
        ProductImage productImage = ProductImage
                .builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl()).build();

        //Khong cho insert qua 5 image cho mot san pham
        int size = productImageRepository.findByProductId(productId).size();
        if(size >= ProductImage.MAXIMUM_IMAGE_PER_PRODUCT){
            throw new InvalidParamException("Number of images be <= 5");
        }
        return productImageRepository.save(productImage);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchPhones(keyword);
    }
}
