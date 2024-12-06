# шпаргалка по консольным командам
mvn clean package -DskipTests=true
mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
docker build -t osandman/rzd-monitoring .
docker run -p 8080:8080 osandman/rzd-monitoring
docker run -P osandman/rzd-monitoring
docker compose -f [FILE NAME] up