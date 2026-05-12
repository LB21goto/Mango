package com.Ai;

import com.Ai.mapper.Aimapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiserviceImpl implements Aiservice {
    @Autowired
    private Aimapper aimapper;

    public int getmag(String name) {
        Integer result = aimapper.findMagzineByName(name);
        return result != null ? result : 0;
    }

    @Override
    public int changemag(String name, int num) {
        Integer result = aimapper.changeMagzineByName(name,num);
        return result != null ? result : 0;
    }
}
