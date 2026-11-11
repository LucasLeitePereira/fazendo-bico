@echo off
rem Compila o jogo para a pasta bin.
rem Se a variavel JDK_JAVA6 apontar para um JDK 6, 7 ou 8, compila no modo Java 6
rem (garante que o codigo nao usa nada de versoes mais novas).
rem Sem a variavel, usa o javac que estiver no PATH.

cd /d "%~dp0"
if exist bin rmdir /s /q bin
mkdir bin

if defined JDK_JAVA6 (
    "%JDK_JAVA6%\bin\javac" -encoding UTF-8 -source 1.6 -target 1.6 -Xlint:all,-options -sourcepath src -d bin src\controle\Jogo.java
) else (
    javac -encoding UTF-8 -sourcepath src -d bin src\controle\Jogo.java
)

if errorlevel 1 (
    echo.
    echo Erro na compilacao.
    exit /b 1
)
echo Compilado em bin\
