package co.fcv.citas.identity;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/auth") class AuthController {
 private final AuthService service; AuthController(AuthService service){this.service=service;}
 @PostMapping("/register") ResponseEntity<RegisteredUser> register(@Valid @RequestBody RegisterRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));}
 @PostMapping("/login") SessionResponse login(@Valid @RequestBody LoginRequest request){return service.login(request);}
 @PostMapping("/refresh") SessionResponse refresh(@Valid @RequestBody RefreshRequest request){return service.refresh(request.refreshToken());}
 record RegisterRequest(@NotBlank String firstNames,@NotBlank String lastNames,@NotBlank String documentType,@NotBlank String documentNumber,@Email @NotBlank String email,@NotBlank String phone,@Size(min=8,max=72) String password){}
 record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
 record RefreshRequest(@NotBlank String refreshToken){}
 record RegisteredUser(Long id,String email,String role){}
 record SessionResponse(String accessToken,String refreshToken,String tokenType,long accessExpiresInSeconds){}
}
