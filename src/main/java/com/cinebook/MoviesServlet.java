package com.cinebook;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/movies")
public class MoviesServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Map<String,Object>> movies = new ArrayList<>();
        String sql = "SELECT id,title,genre,duration_minutes,rating,description FROM movies WHERE active=1 ORDER BY id";
        try (Connection c=Db.getConnection(); PreparedStatement ps=c.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            while(rs.next()) {
                Map<String,Object> m=new LinkedHashMap<>();
                m.put("id",rs.getInt("id")); m.put("title",rs.getString("title"));
                m.put("genre",rs.getString("genre")); m.put("durationMinutes",rs.getInt("duration_minutes"));
                m.put("rating",rs.getBigDecimal("rating")); m.put("description",rs.getString("description"));
                movies.add(m);
            }
            Json.send(resp,200,movies);
        } catch(SQLException e) { getServletContext().log("Movie API failed",e); Json.error(resp,500,"Could not load movies. Check database configuration."); }
    }
}
