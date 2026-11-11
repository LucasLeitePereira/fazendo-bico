@echo off
rem Roda o jogo. Compile antes com compilar.bat.
cd /d "%~dp0"
java -cp bin;recursos controle.Jogo
