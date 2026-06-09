package com.example.controller;

import com.example.dto.BitmapResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seat/bitmap")
public class SeatBitmapController {

    private final com.example.service.SeatBitmapServiceImpl seatBitmapService;

    public SeatBitmapController(com.example.service.SeatBitmapServiceImpl seatBitmapService) {
        this.seatBitmapService = seatBitmapService;
    }

    /**
     * 将两个 redis bitmap 做 OR，并返回前 totalBits 位的 0/1 数组
     * 示例: GET /seat/bitmap/or?keyA=seat:reserved:123&keyB=seat:sold:123&totalBits=20
     */
    @GetMapping("/or")
    public ResponseEntity<BitmapResponse> orBitmap(
            @RequestParam String keyA,
            @RequestParam String keyB,
            @RequestParam int totalBits
    ) {
        BitmapResponse resp = seatBitmapService.orAndReadBits(keyA, keyB, totalBits);
        return ResponseEntity.ok(resp);
    }
}
