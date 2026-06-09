package com.Ai;

import com.Ai.AopLog;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ChatClientAspect {

    @Before("@annotation(aopLog)")
    public void beforeChatOnce(AopLog aopLog) {
        System.out.println("切面执行成功");
    }
}