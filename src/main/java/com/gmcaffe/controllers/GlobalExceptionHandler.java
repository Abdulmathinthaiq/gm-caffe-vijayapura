package com.gmcaffe.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class GlobalExceptionHandler implements ErrorController {

    @GetMapping("/error")
    public String handleError(
            HttpServletRequest request,
            Model model,
            @RequestAttribute(value = "javax.servlet.error.exception", required = false) Exception exception) {
        
        // Get error details
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exceptionObj = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        Integer statusCode = null;
        if (status != null) {
            statusCode = Integer.valueOf(status.toString());
        }
        
        // Log the error for debugging
        System.out.println("========================================");
        System.out.println("ERROR OCCURRED:");
        System.out.println("Status Code: " + statusCode);
        System.out.println("Error Message: " + errorMessage);
        
        if (exceptionObj != null) {
            Throwable throwable = (Throwable) exceptionObj;
            System.out.println("Exception: " + throwable.getClass().getName());
            System.out.println("Exception Message: " + throwable.getMessage());
        }
        System.out.println("========================================");
        
        // Add error details to model
        model.addAttribute("statusCode", statusCode != null ? statusCode : 500);
        model.addAttribute("errorMessage", errorMessage != null ? errorMessage : "An unexpected error occurred");
        
        if (exceptionObj != null) {
            Throwable throwable = (Throwable) exceptionObj;
            model.addAttribute("exceptionType", throwable.getClass().getSimpleName());
            
            // Don't expose internal details in production
            String detailedMessage = throwable.getMessage();
            if (detailedMessage != null && detailedMessage.length() > 100) {
                detailedMessage = detailedMessage.substring(0, 100) + "...";
            }
            model.addAttribute("exceptionMessage", detailedMessage);
        }
        
        // For specific error codes, redirect appropriately
        if (statusCode != null) {
            if (statusCode == 403) {
                model.addAttribute("errorTitle", "Access Forbidden");
                return "error";
            } else if (statusCode == 404) {
                model.addAttribute("errorTitle", "Page Not Found");
                return "error";
            } else if (statusCode == 500) {
                model.addAttribute("errorTitle", "Server Error");
                return "error";
            }
        }
        
        model.addAttribute("errorTitle", "Oops! Something went wrong");
        return "error";
    }
    
    /**
     * Custom error page template
     */
    @GetMapping("/error-page")
    public String customError() {
        return "error";
    }
}

