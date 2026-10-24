@echo off
chcp 65001 >nul
setlocal
title TP5-Spring-Hibernate
mode con: cols=140 lines=46 >nul 2>&1
cd /d "%~dp0"

if not exist "target\classes\mliaedu\tp5\CatalogueDemo.class" goto :compiler
if not exist "target\lib\mysql-connector-j-8.0.33.jar" goto :compiler
goto :lancer

:compiler
echo Compilation du projet (mvn clean package -DskipTests)...
call mvn -B -q clean package -DskipTests
if errorlevel 1 goto :echec

:lancer
set "LOG_FORMAT=[%%4$s] %%5$s%%n"
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" "-Djava.util.logging.SimpleFormatter.format=%LOG_FORMAT%" -cp "target\classes;target\lib\*" mliaedu.tp5.CatalogueDemo
set "CODE=%ERRORLEVEL%"
echo.
echo Code de sortie : %CODE%  (0 = toutes les verifications sont conformes)
pause
exit /b %CODE%

:echec
echo.
echo [ERREUR] La compilation a echoue, corrigez les erreurs ci-dessus.
pause
exit /b 1
