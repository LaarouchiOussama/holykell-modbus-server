mvn install -DskipTests
$tag = Read-Host -Prompt 'Please provide the current release version'
docker build --build-arg RELEASE_VERSION=$tag -t hathoute/exprom-modbus-server -t hathoute/exprom-modbus-server:$tag .