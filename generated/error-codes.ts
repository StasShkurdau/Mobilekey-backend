// Auto-generated. Do not edit manually.
// Run: ./gradlew generateErrorCodes

export const API_ERRORS = {
  "auth.login_already_taken": "Login already taken",
  "auth.email_already_taken": "Email already taken",
  "auth.invalid_credentials": "Invalid credentials",
  "auth.invalid_refresh_token": "Invalid refresh token",
  "auth.refresh_token_expired": "Refresh token is expired or revoked",
  "auth.user_not_found": "User not found",
  "auth.reset_code_expired": "Reset code expired or not found",
  "auth.invalid_reset_code": "Invalid reset code",
  "user.user_not_found": "User not found",
  "user.login_already_taken": "Login already taken",
  "user.email_already_taken": "Email already taken",
} as const;

export type ApiErrorCode = keyof typeof API_ERRORS;
