user=$1
password=$2
curl -v -X POST -H "Content-Type:application/json" http://localhost:8080/nci_protege/login -d "{\"user\":\"$user\", \"password\":\"$password\"}" 
