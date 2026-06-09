package com.Ai.Controller;

import com.Ai.Service.AiserviceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Component
public class testAiController {
    @Autowired
    private AiserviceImpl aiserviceimpl;
    @GetMapping("/test")
    public String test(String name){
        int result = aiserviceimpl.getmag(name);
        return "查询结果：" + result;
    }
}
