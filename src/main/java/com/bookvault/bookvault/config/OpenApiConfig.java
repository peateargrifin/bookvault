// package com.bookvault.bookvault.config;

// import io.swagger.v3.oas.annotations.OpenAPIDefinition;
// import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
// import io.swagger.v3.oas.annotations.info.Info;
// import io.swagger.v3.oas.annotations.security.SecurityScheme;
// import org.springframework.context.annotation.Configuration;

// // 📘 CONCEPT: OpenAPI / Swagger Configuration
// // Because we added @SecurityRequirement(name = "Bearer Authentication") to our controllers,
// // Swagger needs to know what "Bearer Authentication" actually means.
// // If we don't define it here, Swagger UI will crash with a 500 Internal Server Error when trying to load!
// @Configuration
// @OpenAPIDefinition(info = @Info(title = "BookVault API", version = "v1"))
// @SecurityScheme(
//     name = "Bearer Authentication",
//     type = SecuritySchemeType.HTTP,
//     bearerFormat = "JWT",
//     scheme = "bearer"
// )
// public class OpenApiConfig {
// }
