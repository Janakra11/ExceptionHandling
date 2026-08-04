Handle Exceptions in Spring Boot:

Why Is Exception Handling Important in Clean Code?
When writing clean code, we strive for clarity and simplicity. 
If exceptions are handled poorly, they can clutter the code and make it harder to understand. 
Poor exception handling often leads to:

Obscured logic: Catching broad exceptions or handling them incorrectly can make it hard to discern the flow of the application.
Inconsistent responses: Having inconsistent ways of handling exceptions can make it difficult for other developers to understand 
how the app responds to errors. Hard-to-maintain code: Without clear separation of concerns, exception handling code becomes 
tangled with business logic, making it more difficult to change and extend. Clean exception handling allows you to make your 
app more predictable, maintainable, and robust.

Best Practices for Exception Handling in Spring Boot
1. Use Specific Exceptions Over Generic Ones
   A clean coder avoids using generic exceptions like Exception or RuntimeException because they provide no meaningful information about the error. 
2. use more specific exceptions. In Spring Boot, you can create custom exceptions to represent specific error conditions.

Do: Define Custom Exception Class

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
Don’t: Use Generic Exceptions

public class MyController {
@GetMapping("/resource")
public String getResource() {
try {
        // Do something that could fail
    } catch (Exception e) {
        throw new RuntimeException("Something went wrong");
        }
    }
}
2. Handle Exceptions Using @ControllerAdvice
   Spring Boot offers @ControllerAdvice as a centralized exception handler, making your code more organized and maintainable. 
   It allows you to catch exceptions globally and return custom error responses without cluttering business logic with error handling.

Do: Use @ControllerAdvice to Handle Exceptions Globally

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
Don’t: Handle Exceptions Locally in Controllers

@RestController
public class MyController {

    @GetMapping("/resource")
    public String getResource() {
        try {
            // Code that could throw an exception
        } catch (Exception e) {
            return "Internal Server Error";
        }
    }
}
3. Use Checked Exceptions for Recoverable Errors and Unchecked for Unrecoverable
   Checked exceptions should be used for situations where the caller can recover or take action.
   Unchecked exceptions (typically extending RuntimeException) are meant for situations where the error is beyond recovery, such as programming mistakes or unexpected conditions.
   Best Practices:
   Checked exceptions should be used for errors like validation failures, missing data, etc.
   Unchecked exceptions should be used for situations where the application cannot recover, such as null pointer exceptions or database connection failures.
   Best Practice for Checked Exceptions
   
   public class InvalidDataException extends Exception {
       public InvalidDataException(String message) {
             super(message);
       }
   }

public class UserService {
    public User findUserById(String id) throws InvalidDataException {
        if (id == null || id.isEmpty()) {
            throw new InvalidDataException("Invalid user ID");
        }
        // logic to find the user
        return new User();
       }
    }
    Best Practice for Unchecked Exceptions
    public class DatabaseConnectionException extends RuntimeException {
        public DatabaseConnectionException(String message) {
            super(message);
        }
}

public class DatabaseService {
    public void connect() {
        try {
               // Attempt to connect to the database
        } catch (Exception e) {
                throw new DatabaseConnectionException("Could not connect to the database");
        }
    }
}
4. Provide Meaningful Error Messages
   Error messages should be clear, informative, and user-friendly. Always avoid exposing sensitive information (like stack traces) in production environments.

Get praveen sharma’s stories in your inbox
Join Medium for free to get updates from this writer.

Enter your email
Subscribe

Remember me for faster sign in

Do: Use Custom Error Messages with HTTP Status Codes

@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorDetails> handleResourceNotFound(ResourceNotFoundException ex) {
    ErrorDetails errorDetails = new ErrorDetails("RESOURCE_NOT_FOUND", ex.getMessage());
    return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
}
Don’t: Expose Stack Traces in Production

@ExceptionHandler(Exception.class)
public ResponseEntity<String> handleGenericException(Exception ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
}
5. Use @ResponseStatus for HTTP Error Codes
   Spring provides the @ResponseStatus annotation, which allows you to automatically associate HTTP status codes with exceptions.

Do: Use @ResponseStatus to Simplify Exception Handling

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
         super(message);
    }
}
Don’t: Use Multiple Custom ResponseEntity Wrappers

@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
}
Key Things to Focus on During Code Review
When conducting code reviews for exception handling, here are a few key things to focus on:

1. Consistency in Exception Handling
   Ensure that the exception handling strategy is consistent across the codebase. Use the same structure for throwing, catching, and responding to exceptions. Avoid mixing checked and unchecked exceptions unnecessarily.

2. Meaningful Error Messages
   Review error messages for clarity and ensure they provide useful information. Avoid vague messages like “An error occurred.”

3. Proper HTTP Status Codes
   Ensure that appropriate HTTP status codes are returned with the response. For example:

404 for ResourceNotFoundException
400 for client errors like invalid input
500 for internal server errors
4. Global vs Local Exception Handling
   Check if exceptions are handled globally using @ControllerAdvice or locally in controllers. Global exception handling makes code more maintainable.

5. Avoiding Catch-All Exceptions
   Ensure that exceptions aren’t being caught and ignored without providing meaningful recovery or response. Avoid catching Exception or RuntimeException unless absolutely necessary.


****_********Common Spring Boot Status CodesCategoryCodeHttpStatus Enum ConstantTypical REST 

Use Case 2xx (Success)200      HttpStatus.OK Standard successful GET, PUT, or PATCH.
                     201       HttpStatus.CREATED Successful POST resulting in a new resource.
                     204       HttpStatus.NO_CONTENT Successful DELETE returning an empty body.

    4xx (Client Error)
                     400       HttpStatus.BAD_REQUEST Invalid JSON input, bad payload syntax, or missing values.
                     401       HttpStatus.UNAUTHORIZED The user lacks valid authentication credentials.
                     403       HttpStatus.FORBIDDEN The user is authenticated but lacks access permissions.
                     404       HttpStatus.NOT_FOUND The requested resource ID does not exist.
                     405       HttpStatus.METHOD_NOT_ALLOWED Wrong HTTP method used (e.g., POST instead of GET).
                     409       HttpStatus.CONFLICT State conflict (e.g., trying to register an email already taken).
    5xx (Server Error)
                     500       HttpStatus.INTERNAL_SERVER_ERROR Unhandled exceptions or internal database failures.
                     503       HttpStatus.SERVICE_UNAVAILABLE Server is overloaded or down for maintenance.************_
