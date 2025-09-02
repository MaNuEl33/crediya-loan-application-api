package co.com.crediya.model.user.gateways;

public interface TokenProvider {
    boolean validateToken(String token);
    String getEmailFromToken(String token);
    String getRoleFromToken(String token);
}
