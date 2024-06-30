:: jlink version can be found in META-INF dir in JLink.jar
set "mathematica_dir=C:\Program Files\Wolfram Research\Mathematica\14.0"
set jlink_version=5.2.0

copy "%mathematica_dir%\SystemFiles\Links\JLink\SystemFiles\Libraries\Windows-x86-64\JLinkNativeLibrary.dll" .
copy "%mathematica_dir%\SystemFiles\Links\JLink\SystemFiles\Libraries\Windows-x86-64\JLinkNativeLibrary.lib" .
CALL mvn install:install-file -Dfile="%mathematica_dir%\SystemFiles\Links\JLink\JLink.jar" -DgroupId="com.wolfram" -DartifactId="jlink" -Dversion="%jlink_version%" -Dpackaging="jar" -DgeneratePom=true
echo "if 'mvn' not found, install apache maven zip and add MAVEN_HOME env system var and %%MAVEN_HOME%%\bin to PATH env system var"
pause
