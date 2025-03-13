package com.project.shopapp.controllers;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.services.Impl.CategoryServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Danh dau class này la mot Restfull Controller va cac phuong thuc nay se tu dong tra ve JSON hoac cac kieu du lieu khac
@RequestMapping("${api.prefix}/categories") // Dinh nghia duong dan co so (base path) cho tat ca cac API trong controller nay. Moi API trong class nay se bat dau voi /api/v1/categories
//Dependency Injection
@RequiredArgsConstructor
public class CategoryController {
        private final CategoryServiceImpl categoryService;

        @PostMapping("")
        //Neu tham so truyen vao la 1 object thi sao ? => Data Transfer Object = Request Object
           /*
              @RequesBody: Tu dong anh xa du lieu JSON tu request thanh doi tuong CategoryDTO.
              @Valid: Kich hoat kiem tra validation dua tren các annotation trong CategoryDTO
              Biding result: Chua danh sach loi validation(neu co).
           */
        // ? de nhan nhieu kieu du lieu hon
        public ResponseEntity<?> createCategory(@RequestBody @Valid CategoryDTO categoryDTO, BindingResult result) {
            if(result.hasErrors()) { // Kiem tra xem co loi validation neu khong
                List<FieldError> fieldErrors = result.getFieldErrors(); // Lay danh sach tat ca loi validation
                List<String> errorMessages = fieldErrors.stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages); // Lay loi dau tien va tra ve voi ma loi 400 Bad Request
            }
            categoryService.createCategory(categoryDTO);
            return ResponseEntity.ok(String.format("Insert category successfully, %s", categoryDTO.getName()));
        }

        //Hien thi tat ca cac category
        @GetMapping("")//http://localhost:8088/api/v1/categories?page=1&limit=10
        //Xử lý HTTP GET request tại đường dẫn /api/v1/categories (do @RequestMapping đã định nghĩa base path).
        public ResponseEntity<List<Category>> getAllCategories(
            @RequestParam("page")  int page,  // "page" là request tu client | int page là code java chung ta quy dinh
            @RequestParam("limit") int limit
        ) { // ReponseEntity giup tra ve HTTP reponse voi du lieu va ma trang thai
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories); // Tra ve chuoi "All cateogry" kem ma 200 OK
        }

        @PutMapping("/{id}")
        public ResponseEntity<?> updateCategory(
                @PathVariable Long id,
                @RequestBody CategoryDTO categoryDTO, BindingResult result) {
            if(result.hasErrors()) { // Kiem tra xem co loi validation neu khong
                List<FieldError> fieldErrors = result.getFieldErrors(); // Lay danh sach tat ca loi validation
                List<String> errorMessages = fieldErrors.stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages); // Lay loi dau tien va tra ve voi ma loi 400 Bad Request
            }
            categoryService.updateCategory(id, categoryDTO);
            return ResponseEntity.ok("Update category successfullys");
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok("Delete category with id " + id+ " successfully");
        }
}
