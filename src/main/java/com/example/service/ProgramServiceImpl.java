package com.example.service;

import com.example.dto.ProgramDTO;
import com.example.entity.Program;
import com.example.mapper.ProgramMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProgramServiceImpl implements ProgramService {

    private final ProgramMapper programMapper;

    @Override
    public ProgramDTO getProgramById(Long id) {
        Program program = programMapper.selectById(id);
        if (program == null) {
            throw new RuntimeException("节目不存在");
        }

        ProgramDTO dto = new ProgramDTO();
        dto.setId(program.getId());
        dto.setTitle(program.getTitle());
        dto.setSubtitle(program.getSubtitle());
        dto.setCategory(program.getCategory());
        dto.setVenue(program.getVenue());
        dto.setSession(program.getSession());
        dto.setAddress(program.getAddress());
        dto.setDescription(program.getDescription());
        dto.setCoverImage(program.getCoverImage());
        // --- 新增开始 ---
// 既然只有一个场次，那就手动把它包成一个数组给前端
        if (program.getSession() != null && !program.getSession().isEmpty()) {
            Map<String, Object> singleSession = new HashMap<>();
            singleSession.put("id", 1); // 随便给个ID，前端渲染要用
            singleSession.put("time", program.getSession()); // 主要内容
            singleSession.put("venue", program.getVenue());   // 顺便带上场馆

            List<Map<String, Object>> list = new ArrayList<>();
            list.add(singleSession);

            dto.setSessions(list); // 赋值给 sessions 字段
        }
// --- 新增结束 ---

        return dto;
    }
}
