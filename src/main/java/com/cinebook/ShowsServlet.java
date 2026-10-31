package com.cinebook;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/shows")
public class ShowsServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String movieId=req.getParameter("movieId");
        if(movieId==null) { Json.error(resp,400,"movieId is required"); return; }
        List<Map<String,Object>> shows=new ArrayList<>();
        String sql="SELECT id,movie_id,show_time,screen_name FROM shows WHERE movie_id=? AND show_time >= NOW() ORDER BY show_time";
        try(Connection c=Db.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,Integer.parseInt(movieId));
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) { Map<String,Object> s=new LinkedHashMap<>(); s.put("id",rs.getInt("id")); s.put("movieId",rs.getInt("movie_id")); s.put("showTime",rs.getTimestamp("show_time").toString()); s.put("screen",rs.getString("screen_name")); shows.add(s); }
            }
            Json.send(resp,200,shows);
        } catch(NumberFormatException e) { Json.error(resp,400,"movieId must be a number"); }
          catch(SQLException e) { getServletContext().log("Shows API failed",e); Json.error(resp,500,"Could not load shows. Check database configuration."); }
    }
}
