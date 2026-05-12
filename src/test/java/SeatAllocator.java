import com.example.entity.Seat;

import java.util.*;
import java.util.stream.Collectors;

public class SeatAllocator {

    public static List<Seat> allocateSeats(List<Seat> seats, int n) {
        // 按行分组
        Map<Integer, List<Seat>> rowMap = seats.stream()
                .collect(Collectors.groupingBy(Seat::getRow));

        // 按行扫描
        List<Integer> sortedRows = new ArrayList<>(rowMap.keySet());
        Collections.sort(sortedRows);

        for (int rowNum : sortedRows) {
            List<Seat> rowSeats = rowMap.get(rowNum);
            rowSeats.sort(Comparator.comparingInt(Seat::getCol));

            List<Seat> emptySegment = new ArrayList<>();
            for (Seat s : rowSeats) {
                if (s.getStatus() == 0) {
                    emptySegment.add(s);
                    if (emptySegment.size() == n) {
                        return emptySegment; // 找到连续座位
                    }
                } else {
                    emptySegment.clear();
                }
            }
        }

        // 没有连续段，返回零散座位（靠前）
        List<Seat> result = new ArrayList<>();
        for (int rowNum : sortedRows) {
            List<Seat> rowSeats = rowMap.get(rowNum);
            rowSeats.sort(Comparator.comparingInt(Seat::getCol));
            for (Seat s : rowSeats) {
                if (s.getStatus() == 0) {
                    result.add(s);
                    if (result.size() == n) {
                        return result;
                    }
                }
            }
        }

        return new ArrayList<>(); // 没有足够座位
    }

    public static void main(String[] args) {
        List<Seat> seats = Arrays.asList(
                new Seat(3,1,"3排1座",0),
                new Seat(3,2,"3排2座",0),
                new Seat(3,3,"3排3座",0),
                new Seat(3,4,"3排4座",0),
                new Seat(3,5,"3排5座",0),
                new Seat(3,6,"3排6座",0),
                new Seat(3,7,"3排7座",0),
                new Seat(3,8,"3排8座",0),
                new Seat(3,9,"3排9座",0),
                new Seat(3,10,"3排10座",0),
                new Seat(2,1,"2排1座",0),
                new Seat(2,2,"2排2座",0),
                new Seat(2,3,"2排3座",0),
                new Seat(2,4,"2排4座",0),
                new Seat(2,5,"2排5座",0),
//                new Seat(2,6,"2排6座",0),
//                new Seat(2,7,"2排7座",0),
//                new Seat(2,8,"2排8座",0),
                new Seat(2,9,"2排9座",0),
                new Seat(2,10,"2排10座",0)
        );

        List<Seat> allocated = allocateSeats(seats, 3);
        for (Seat s : allocated) {
            System.out.println("分配座位: " + s.getSeatCode());
        }
    }
}