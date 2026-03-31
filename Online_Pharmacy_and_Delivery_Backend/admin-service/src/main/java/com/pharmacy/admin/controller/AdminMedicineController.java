package com.pharmacy.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.admin.dto.request.CategoryRequestDto;
import com.pharmacy.admin.dto.request.MedicineRequestDto;
import com.pharmacy.admin.dto.response.ApiResponse;
import com.pharmacy.admin.dto.response.CategoryResponseDto;
import com.pharmacy.admin.dto.response.MedicineResponseDto;
import com.pharmacy.admin.service.AdminCategoryService;
import com.pharmacy.admin.service.AdminMedicineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Medicine & Category Management", description = "Admin CRUD for medicines, categories, and inventory alerts")
public class AdminMedicineController {

    private final AdminMedicineService medicineService;
    private final AdminCategoryService categoryService;

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @GetMapping("/categories/active")
    @Operation(summary = "Get active categories only")
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getActiveCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getActiveCategories()));
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryById(id)));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(@Valid @RequestBody CategoryRequestDto dto) {
        CategoryResponseDto created = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", created));
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update a category")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.updateCategory(id, dto)));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Soft-delete a category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }

    @GetMapping("/medicines")
    @Operation(summary = "Get all medicines (active + inactive)")
    public ResponseEntity<ApiResponse<List<MedicineResponseDto>>> getAllMedicines() {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getAllMedicines()));
    }

    @GetMapping("/medicines/active")
    @Operation(summary = "Get active medicines only")
    public ResponseEntity<ApiResponse<List<MedicineResponseDto>>> getActiveMedicines() {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getActiveMedicines()));
    }

    @GetMapping("/medicines/{id}")
    @Operation(summary = "Get medicine by ID")
    public ResponseEntity<ApiResponse<MedicineResponseDto>> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getMedicineById(id)));
    }

    @GetMapping("/medicines/search")
    @Operation(summary = "Search medicines by name, generic name, or manufacturer")
    public ResponseEntity<ApiResponse<List<MedicineResponseDto>>> searchMedicines(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(medicineService.searchMedicines(q, page, size)));
    }

    @GetMapping("/medicines/category/{categoryId}")
    @Operation(summary = "Get medicines by category")
    public ResponseEntity<ApiResponse<List<MedicineResponseDto>>> getMedicinesByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getMedicinesByCategory(categoryId)));
    }

    @PostMapping("/medicines")
    @Operation(summary = "Add a new medicine to the catalog")
    public ResponseEntity<ApiResponse<MedicineResponseDto>> addMedicine(@Valid @RequestBody MedicineRequestDto dto) {
        MedicineResponseDto created = medicineService.addMedicine(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medicine added successfully", created));
    }

    @PutMapping("/medicines/{id}")
    @Operation(summary = "Update medicine details")
    public ResponseEntity<ApiResponse<MedicineResponseDto>> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Medicine updated", medicineService.updateMedicine(id, dto)));
    }

    @PatchMapping("/medicines/{id}/stock")
    @Operation(summary = "Update stock quantity")
    public ResponseEntity<ApiResponse<MedicineResponseDto>> updateStock(
            @PathVariable Long id,
            @RequestParam @Parameter(description = "New stock count (>= 0)") int stock) {
        return ResponseEntity.ok(ApiResponse.success("Stock updated", medicineService.updateStock(id, stock)));
    }

    @DeleteMapping("/medicines/{id}")
    @Operation(summary = "Soft-delete a medicine")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.success("Medicine removed from catalog", null));
    }

    @GetMapping("/medicines/alerts/low-stock")
    @Operation(summary = "Get medicines with low stock")
    public ResponseEntity<ApiResponse<List<MedicineResponseDto>>> getLowStockMedicines() {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getLowStockMedicines()));
    }

    @GetMapping("/medicines/alerts/expiring")
    @Operation(summary = "Get medicines expiring within 30 days")
    public ResponseEntity<ApiResponse<List<MedicineResponseDto>>> getExpiringMedicines() {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getExpiringMedicines()));
    }

    @GetMapping("/medicines/alerts/expired")
    @Operation(summary = "Get medicines already expired")
    public ResponseEntity<ApiResponse<List<MedicineResponseDto>>> getExpiredMedicines() {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getExpiredMedicines()));
    }

    @GetMapping("/medicines/alerts/summary")
    @Operation(summary = "Get quick inventory alert counts")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getAlertSummary() {
        Map<String, Integer> summary = Map.of(
                "lowStock", medicineService.getLowStockMedicines().size(),
                "expiring", medicineService.getExpiringMedicines().size(),
                "expired", medicineService.getExpiredMedicines().size());

        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
