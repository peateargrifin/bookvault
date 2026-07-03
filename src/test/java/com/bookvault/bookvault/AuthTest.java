package com.bookvault.bookvault;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.bookvault.bookvault.dto.request.RegisterRequest;
import com.bookvault.bookvault.service.AuthService;
import com.bookvault.bookvault.repository.AuthorRepository;

@SpringBootTest
public class AuthTest {
    @Autowired AuthService authService;
    @Autowired AuthorRepository authorRepository;
    
    @Test
    public void testRegister() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test Author");
        req.setEmail("testauthor@test.com");
        req.setPassword("Password123!");
        
        authService.register(req);
        System.out.println("TOTAL AUTHORS: " + authorRepository.count());
    }
}
