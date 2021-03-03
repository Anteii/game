package com.onlinegame.game.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request){
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null){
            Integer statusCode = Integer.valueOf(status.toString());
            switch (statusCode){
                case 404:
                    return "error-404";
                case 500:
                    return "error-500";
                case 403:
                    return "error-403";
            }
        }
        return "error";
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}
