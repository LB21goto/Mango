package com.example;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SeatDataGenerator {

    public static void main(String[] args) {
        Long programId = 1L;
        int totalRows = 40;
        int seatsPerRow = 50;

        List<String> sqls = new ArrayList<>();

        // 1. 生成 SQL 逻辑（和之前一样）
        for (int row = 1; row <= totalRows; row++) {
            for (int col = 1; col <= seatsPerRow; col++) {
                String seatNo = String.format("A-%02d-%02d", row, col);
                BigDecimal price = new BigDecimal("800.00");
                if (row <= 5) price = new BigDecimal("1200.00");

                String sql = String.format(
                        "INSERT INTO `test_seat` (`program_id`, `seat_no`, `row_num`, `col_num`, `price`, `status`) VALUES (%d, '%s', %d, %d, %s, 1);",
                        programId, seatNo, row, col, price.toString()
                );
                sqls.add(sql);
            }
        }

        // 2. 写入到文件 (重点在这里！)
        String filePath = "D:\\seats_data1.sql"; // 写到你的D盘根目录

        try (FileWriter writer = new FileWriter(filePath)) {
            // 先写节目数据
            writer.write("INSERT INTO `t_program` (`id`, `name`, `start_time`, `status`) VALUES (1, '测试节目', NOW(), 1);\n");

            // 再写座位数据
            for (String sql : sqls) {
                writer.write(sql + "\n");
            }
            System.out.println("成功！SQL文件已生成在：" + filePath);
            System.out.println("请去数据库工具中导入该文件。");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
