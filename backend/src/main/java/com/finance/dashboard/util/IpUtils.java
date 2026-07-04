package com.finance.dashboard.util;
import jakarta.servlet.http.HttpServletRequest;

public final class IpUtils {
    private IpUtils() {}
    public static String resolveIp(HttpServletRequest req) {
        String f = req.getHeader("X-Forwarded-For");
        if (f != null && !f.isBlank()) { String ip = f.split(",")[0].trim(); return ip.length()<=45?ip:req.getRemoteAddr(); }
        String r = req.getHeader("X-Real-IP");
        return (r!=null && !r.isBlank() && r.length()<=45) ? r.trim() : req.getRemoteAddr();
    }
}
