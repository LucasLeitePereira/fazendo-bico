@echo off
rem Gera dist\FazendoBico.jar: o jogo compilado mais a pasta recursos, num arquivo so.
rem Para jogar: java -jar FazendoBico.jar (ou dois cliques, se o Java estiver instalado).

cd /d "%~dp0"
call "%~dp0compilar.bat"
if errorlevel 1 exit /b 1

if exist dist rmdir /s /q dist
mkdir dist
echo Main-Class: controle.Jogo> dist\manifesto.txt

if defined JDK_JAVA6 (
    "%JDK_JAVA6%\bin\jar" cfm dist\FazendoBico.jar dist\manifesto.txt -C bin . -C recursos .
) else (
    jar cfm dist\FazendoBico.jar dist\manifesto.txt -C bin . -C recursos .
)

if errorlevel 1 (
    echo.
    echo Erro ao gerar o jar.
    exit /b 1
)
del dist\manifesto.txt
echo Gerado dist\FazendoBico.jar
