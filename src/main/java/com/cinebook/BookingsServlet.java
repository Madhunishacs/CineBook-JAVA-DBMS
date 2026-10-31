package com.cinebook;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.security.SecureRandom;

@WebServlet("/api/bookings")
public class BookingsServlet extends HttpServlet {
    private static final SecureRandom RANDOM = new SecureRandom();

    // JSON body: {"showId":1,"seats":["A1","A2"],"paymentMethod":"UPI (simulation)","snacks":true,"coupon":"CINE10"}
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<?,?> body;
        try { body=Json.GSON.fromJson(req.getReader(),Map.class); }
        catch(Exception e) { Json.error(resp,400,"Invalid JSON body"); return; }
        if(body==null || !(body.get("showId") instanceof Number) || !(body.get("seats") instanceof List<?> seats) || seats.isEmpty()) {
            Json.error(resp,400,"showId and at least one seat are required"); return;
        }
        int showId=((Number)body.get("showId")).intValue();
        boolean snacks=Boolean.TRUE.equals(body.get("snacks"));
        String coupon=String.valueOf((body.containsKey("coupon") ? body.get("coupon") : ""));
        String payment=String.valueOf((body.containsKey("paymentMethod") ? body.get("paymentMethod") : "UPI (simulation)"));
        Set<String> seatSet=new LinkedHashSet<>();
        for(Object o:seats) {
            String seat=String.valueOf(o).trim().toUpperCase(Locale.ROOT);
            if(!seat.matches("[A-E][1-8]")) { Json.error(resp,400,"Invalid seat code: "+seat); return; }
            seatSet.add(seat);
        }
        if(seatSet.size()!=seats.size()) { Json.error(resp,400,"Duplicate seats in request"); return; }

        String code="CB"+Long.toString(Math.abs(RANDOM.nextLong()),36).toUpperCase(Locale.ROOT).substring(0,8);
        try(Connection c=Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement check=c.prepareStatement("SELECT id FROM shows WHERE id=? FOR UPDATE")) {
                    check.setInt(1,showId); try(ResultSet r=check.executeQuery()){if(!r.next()){c.rollback();Json.error(resp,404,"Show not found");return;}}
                }
                for(String seat:seatSet) {
                    try(PreparedStatement ps=c.prepareStatement("SELECT id FROM booking_seats WHERE show_id=? AND seat_code=? AND status='CONFIRMED'")) {
                        ps.setInt(1,showId); ps.setString(2,seat);
                        try(ResultSet rs=ps.executeQuery()){if(rs.next()){c.rollback();Json.error(resp,409,"Seat "+seat+" is already booked");return;}}
                    }
                }
                BigDecimal total=BigDecimal.ZERO;
                for(String seat:seatSet) {
                    char row=seat.charAt(0);
                    total=total.add(new BigDecimal(row=='E'?"400":(row=='C'||row=='D'?"250":"150")));
                }
                if(snacks) total=total.add(new BigDecimal("180"));
                BigDecimal discount="CINE10".equalsIgnoreCase(coupon)?total.multiply(new BigDecimal("0.10")).setScale(0,java.math.RoundingMode.HALF_UP):BigDecimal.ZERO;
                BigDecimal amount=total.subtract(discount);
                int bookingId;
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO bookings (booking_code,show_id,total_amount,discount_amount,amount,status,payment_method,payment_status,snacks) VALUES (?,?,?,?,?,'CONFIRMED',?,'SIMULATED',?)",Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1,code); ps.setInt(2,showId); ps.setBigDecimal(3,total); ps.setBigDecimal(4,discount); ps.setBigDecimal(5,amount); ps.setString(6,payment); ps.setBoolean(7,snacks); ps.executeUpdate();
                    try(ResultSet keys=ps.getGeneratedKeys()){keys.next();bookingId=keys.getInt(1);}
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO booking_seats (booking_id,show_id,seat_code,status) VALUES (?,?,?,'CONFIRMED')")) {
                    for(String seat:seatSet){ps.setInt(1,bookingId);ps.setInt(2,showId);ps.setString(3,seat);ps.addBatch();} ps.executeBatch();
                }
                c.commit();
                Json.send(resp,201,Map.of("bookingId",bookingId,"code",code,"showId",showId,"seats",seatSet,"total",total,"discount",discount,"amount",amount,"status","CONFIRMED","paymentStatus","SIMULATED","snacks",snacks));
            } catch(Exception e){c.rollback();throw e;} finally {c.setAutoCommit(true);}
        } catch(SQLException e) { getServletContext().log("Booking API failed",e); Json.error(resp,500,"Booking failed. Check server logs and database."); }
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code=req.getParameter("code");
        if(code==null || code.isBlank()){Json.error(resp,400,"Pass a booking code, e.g. ?code=CB...");return;}
        String sql="SELECT b.booking_code,b.show_id,b.total_amount,b.discount_amount,b.amount,b.status,b.payment_method,b.payment_status,b.snacks,s.show_time,m.title FROM bookings b JOIN shows s ON s.id=b.show_id JOIN movies m ON m.id=s.movie_id WHERE b.booking_code=?";
        try(Connection c=Db.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setString(1,code);try(ResultSet rs=ps.executeQuery()){
            if(!rs.next()){Json.error(resp,404,"Booking not found");return;}
            Map<String,Object> b=new LinkedHashMap<>();b.put("code",rs.getString("booking_code"));b.put("showId",rs.getInt("show_id"));b.put("movie",rs.getString("title"));b.put("showTime",rs.getTimestamp("show_time").toString());b.put("total",rs.getBigDecimal("total_amount"));b.put("discount",rs.getBigDecimal("discount_amount"));b.put("amount",rs.getBigDecimal("amount"));b.put("status",rs.getString("status"));b.put("paymentMethod",rs.getString("payment_method"));b.put("paymentStatus",rs.getString("payment_status"));b.put("snacks",rs.getBoolean("snacks"));
            List<String> bookedSeats=new ArrayList<>();try(PreparedStatement seatPs=c.prepareStatement("SELECT seat_code FROM booking_seats WHERE booking_id=(SELECT id FROM bookings WHERE booking_code=?)")){seatPs.setString(1,code);try(ResultSet seatRs=seatPs.executeQuery()){while(seatRs.next())bookedSeats.add(seatRs.getString(1));}}
            b.put("seats",bookedSeats);Json.send(resp,200,b);
        }}catch(SQLException e){getServletContext().log("Ticket API failed",e);Json.error(resp,500,"Could not retrieve ticket.");}
    }
}
