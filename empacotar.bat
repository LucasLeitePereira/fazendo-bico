@echo off
rem Gera dist\FazendoBico.jar: o jogo compilado, a pasta recursos e a JInput, num arquivo so.
rem Para jogar: java -jar FazendoBico.jar (ou dois cliques, se o Java estiver instalado).

cd /d "%~dp0"
call "%~dp0compilar.bat"
if errorlevel 1 exit /b 1

set JAR=jar
if defined JDK_JAVA6 set JAR="%JDK_JAVA6%\bin\jar"

if exist dist rmdir /s /q dist
mkdir dist\bibliotecas
rem As classes da JInput entram soltas no jar do jogo (sem o META-INF delas).
pushd dist\bibliotecas
%JAR% xf ..\..\lib\jinput-2.0.7.jar net
%JAR% xf ..\..\lib\jutils-1.0.0.jar net
popd

echo Main-Class: controle.Jogo> dist\manifesto.txt
rem Evita o aviso de acesso nativo do Java 24 ou mais novo (as versoes antigas ignoram a linha).
echo Enable-Native-Access: ALL-UNNAMED>> dist\manifesto.txt

%JAR% cfm dist\FazendoBico.jar dist\manifesto.txt -C bin . -C recursos . -C dist\bibliotecas .

if errorlevel 1 (
    echo.
    echo Erro ao gerar o jar.
    exit /b 1
)
del dist\manifesto.txt
rmdir /s /q dist\bibliotecas
echo Gerado dist\FazendoBico.jar
