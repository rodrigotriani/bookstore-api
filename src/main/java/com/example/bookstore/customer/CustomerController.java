package com.example.bookstore.customer;

import com.example.bookstore.common.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @Operation(summary = "Create a customer")
    @ApiResponse(responseCode = "201", description = "Customer created")
    @ApiResponse(responseCode = "400", description = "Invalid payload",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Duplicate email",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.create(request);
        return ResponseEntity.created(URI.create("/api/customers/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "List all customers")
    @ApiResponse(responseCode = "200", description = "List of customers")
    public List<CustomerResponse> list() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by id")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "404", description = "Customer not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public CustomerResponse get(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a customer")
    @ApiResponse(responseCode = "200", description = "Customer updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Customer not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Duplicate email",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer", description = "Refused with 409 when the customer has orders.")
    @ApiResponse(responseCode = "204", description = "Customer deleted")
    @ApiResponse(responseCode = "404", description = "Customer not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Customer has orders",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
