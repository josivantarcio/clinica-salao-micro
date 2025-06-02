@echo off
echo ======================================
echo Iniciando ambiente ClinicaSalao Microservices
echo ======================================

echo.
echo 1. Iniciando Discovery Service...
start "Discovery Service" cmd /k "cd discovery-service && mvn clean spring-boot:run"

echo.
echo Aguardando Discovery Service inicializar (15 segundos)...
timeout /t 15 /nobreak > nul

echo.
echo 2. Iniciando API Gateway...
start "API Gateway" cmd /k "cd api-gateway && mvn clean spring-boot:run"

echo.
echo 3. Iniciando Auth Service...
start "Auth Service" cmd /k "cd auth-service && mvn clean spring-boot:run"

echo.
echo 4. Iniciando Client Service...
start "Client Service" cmd /k "cd client-service && mvn clean spring-boot:run"

echo.
echo 5. Iniciando Finance Service...
start "Finance Service" cmd /k "cd finance-service && mvn clean spring-boot:run"

echo.
echo 6. Iniciando Appointment Service...
start "Appointment Service" cmd /k "cd appointment-service && mvn clean spring-boot:run"

echo.
echo ======================================
echo Todos os serviços foram iniciados!
echo ======================================
echo.
echo Endpoints disponíveis:
echo - Discovery Service: http://localhost:8761
echo - API Gateway: http://localhost:8080
echo - Auth Service: http://localhost:8081
echo - Client Service: http://localhost:8082
echo - Finance Service: http://localhost:8083
echo - Appointment Service: http://localhost:8084
echo.
echo Pressione qualquer tecla para encerrar este console...
pause > nul
