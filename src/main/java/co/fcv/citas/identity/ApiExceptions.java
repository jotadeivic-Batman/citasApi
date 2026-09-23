package co.fcv.citas.identity;
import java.util.*; import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*;
class ConflictException extends RuntimeException { ConflictException(String code){super(code);} } class UnauthorizedException extends RuntimeException { UnauthorizedException(String code){super(code);} }
@RestControllerAdvice class ApiExceptions {
 record ApiError(int status,String code,String message){}
 @ExceptionHandler(ConflictException.class) ResponseEntity<ApiError> conflict(ConflictException e){return error(HttpStatus.CONFLICT,e.getMessage(),"La solicitud entra en conflicto con un dato existente.");}
 @ExceptionHandler(UnauthorizedException.class) ResponseEntity<ApiError> unauthorized(UnauthorizedException e){return error(HttpStatus.UNAUTHORIZED,e.getMessage(),"No fue posible autenticar la solicitud.");}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e){return error(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","La solicitud contiene datos inválidos.");}
 private ResponseEntity<ApiError> error(HttpStatus s,String code,String message){return ResponseEntity.status(s).body(new ApiError(s.value(),code,message));}
}
