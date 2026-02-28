package com.gmcaffe.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class GlobalExceptionHandler implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            System.out.println("ERROR STATUS: " + statusCode);
            System.out.println("ERROR MESSAGE: " + errorMessage);
            
            if (exception != null) {
                Throwable throwable = (Throwable) exception;
                System.out.println("EXCEPTION: " + throwable.getClass().getName());
                System.out.println("EXCEPTION MESSAGE: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        }
        
        // Redirect to admin login page with logout success message
        return "redirect:/admin/login?logout=true";
    }
}
