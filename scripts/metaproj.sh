user=$1
password=$2
curl --cacert cacert.pem -v -X POST -H "Content-Type:application/json" https://localhost:8080/nci_protege/login -d "{\"user\":\"$user\", \"password\":\"$password\"}" | jq --raw-output '. | .userid, .token' > usertok

res=""
for i in `cat usertok`
do
    res="${res}${i}"
    res="${res}:"
done
AUTH=`echo -n ${res%?} | openssl enc -base64 | tr -d "\n"`
echo $AUTH

curl --cacert cacert.pem -v -H "Authorization: Basic ${AUTH}" https://localhost:8080/nci_protege/meta/metaproject | jq '.'
