@echo off
echo ========================================
echo AUTH SERVICE API TEST - STEP BY STEP
echo ========================================
echo.

set BASE_URL=http://localhost:8081
set TEST_EMAIL=alice@pharmacy.com

echo.
echo [STEP 1] Starting Docker services...
echo ----------------------------------------
docker-compose up -d mysql redis rabbitmq
echo.

echo [STEP 2] Starting email-service...
echo ----------------------------------------
docker-compose up -d email-service
echo.

echo [STEP 3] Starting auth-service...
echo ----------------------------------------
docker-compose up -d auth-service
echo.

echo [STEP 4] Waiting for services to start (10 seconds)...
timeout /t 10 /nobreak > nul
echo.

echo ========================================
echo TESTING NEW API ENDPOINTS
echo ========================================
echo.

echo [TEST 1] Request Login OTP
echo ----------------------------------------
curl -s -X POST %BASE_URL%/api/auth/request-login-otp ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\": \"%TEST_EMAIL%\"}"
echo.
echo.

echo [STEP 5] Get OTP from Redis
echo ----------------------------------------
echo Run this command to see the OTP:
echo docker exec redis redis-cli HGETALL "otp:%TEST_EMAIL%"
echo.
docker exec redis redis-cli HGETALL "otp:%TEST_EMAIL%"
echo.

echo [TEST 2] Resend Verification Email
echo ----------------------------------------
curl -s -X POST "%BASE_URL%/api/auth/resend-verification?email=%TEST_EMAIL%"
echo.
echo.

echo [TEST 3] Verify Email (will fail - needs real token)
echo ----------------------------------------
curl -s -X GET "%BASE_URL%/api/auth/verify-email?token=test-token"
echo.
echo.

echo [TEST 4] Existing Login (for comparison)
echo ----------------------------------------
curl -s -X POST %BASE_URL%/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\": \"%TEST_EMAIL%\", \"password\": \"Alice@123\"}"
echo.
echo.

echo ========================================
echo DEBUG COMMANDS
echo ========================================
echo.
echo Check email-service logs:
echo docker logs email-service
echo.
echo Check RabbitMQ queues:
echo docker exec rabbitmq rabbitmqctl list_queues
echo.
echo Check Redis keys:
echo docker exec redis redis-cli KEYS otp:*
echo.
echo Check auth-service logs:
echo docker logs auth-service
echo.
echo ========================================
pause
