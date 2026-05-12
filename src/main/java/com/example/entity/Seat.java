package com.example.entity;

public class Seat {
    private int row;
    private int col;
    private String seatCode;
    private int status; // 0=可选, 1=已售

    public Seat(int row, int col, String seatCode, int status) {
        this.row = row;
        this.col = col;
        this.seatCode = seatCode;
        this.status = status;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getStatus() { return status; }
    public String getSeatCode() { return seatCode; }
}