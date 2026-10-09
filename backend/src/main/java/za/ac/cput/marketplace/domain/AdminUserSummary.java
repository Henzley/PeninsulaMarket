package za.ac.cput.marketplace.domain;

public record AdminUserSummary(Long id, String fullName, String email, User.Role role) {
    public static AdminUserSummary from(User user) {
        return new AdminUserSummary(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
