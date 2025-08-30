@echo off
echo ========================================
echo Running Spring Boot Tests
echo ========================================

echo.
echo 1. Running Unit Tests...
echo ----------------------------------------
gradlew test --tests "*UnitTest" --info

echo.
echo 2. Running Integration Tests...
echo ----------------------------------------
gradlew test --tests "*IntegrationTest" --info

echo.
echo 3. Running All Tests...
echo ----------------------------------------
gradlew test --info

echo.
echo ========================================
echo Test Execution Complete
echo ========================================
