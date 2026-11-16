@echo off
rem Roda o jogo. Compile antes com compilar.bat.
cd /d "%~dp0"
java -cp bin;recursos;lib\jinput-2.0.7.jar;lib\jutils-1.0.0.jar controle.Jogo
