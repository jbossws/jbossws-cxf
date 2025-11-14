# WSTrust testing requires multiple certificates in multiple keystores for all entitites to be able to trust the require counterparts
# This script will generate the keystores, for example in case  stored certificates already expired

CLIENT_STORE=META-INF/clientstore.jks
ACTAS_STORE=WEB-INF/actasstore.jks
SERVICE_STORE=WEB-INF/servicestore.jks
STS_STORE=WEB-INF/stsstore.jks

# First we must delete the existing once otherwise keytool execution would fail
rm $CLIENT_STORE $ACTAS_STORE $SERVICE_STORE $STS_STORE

# We need to generate a self-signed key pair in a keystore for each entity tests use, make it valid with past date
# Beware different -keypass and -storepass values, matching what tests use!
# If we ever switch from JKS to PKCS12 format where -keypass value is ignored and both passwords are set to the same value, we will need to update values used in tests.
keytool -genkeypair -validity 10000 -startdate "-2d" -storetype JKS -keyalg RSA -sigalg SHA256withRSA -keysize 2048 \
        -alias mystskey -keystore $STS_STORE -keypass stskpass -storepass stsspass \
        -dname "EMAILADDRESS=sts@sts.com, CN=www.sts.com, OU=IT Department, O=Sample STS -- NOT FOR PRODUCTION, L=Baltimore, ST=Maryland, C=US"
keytool -genkeypair -validity 10000 -startdate "-2d" -storetype JKS -keyalg RSA -sigalg SHA256withRSA -keysize 2048 \
        -alias myservicekey -keystore $SERVICE_STORE -keypass skpass -storepass sspass \
        -dname "EMAILADDRESS=service@service.com, CN=www.service.com, OU=IT Department, O=Sample Web Service Provider -- NOT FOR PRODUCTION, L=Buffalo, ST=New York, C=US"
keytool -genkeypair -validity 10000 -startdate "-2d" -storetype JKS -keyalg RSA -sigalg SHA256withRSA -keysize 2048 \
        -alias myclientkey -keystore $CLIENT_STORE -keypass ckpass -storepass cspass \
        -dname "EMAILADDRESS=client@client.com, CN=www.client.com, OU=IT Department, O=Sample Client -- NOT FOR PRODUCTION, L=Niagara Falls, ST=New York, C=US"
keytool -genkeypair -validity 10000 -startdate "-2d" -storetype JKS -keyalg RSA -sigalg SHA256withRSA -keysize 2048 \
        -alias myactaskey -keystore $ACTAS_STORE -keypass aspass -storepass aapass \
        -dname "CN=www.actas.com, OU=IT Department, O=Sample ActAs Web Service -- NOT FOR PRODUCTION, L=Dayton, ST=Ohio, C=US"

# Each entity must export its public certificate to a file so it can be imported into the others' trust stores
keytool -export -rfc -keystore $STS_STORE     -alias mystskey     -file mystskey.cer     -storepass stsspass
keytool -export -rfc -keystore $SERVICE_STORE -alias myservicekey -file myservicekey.cer -storepass sspass
keytool -export -rfc -keystore $CLIENT_STORE  -alias myclientkey  -file myclientkey.cer  -storepass cspass
keytool -export -rfc -keystore $ACTAS_STORE   -alias myactaskey   -file myactaskey.cer   -storepass aapass

# Each keystore imports the certificates of the entities it needs to trust
keytool -importcert -noprompt -trustcacerts -file myactaskey.cer   -alias myactaskey   -keystore $STS_STORE     -storepass stsspass
keytool -importcert -noprompt -trustcacerts -file myclientkey.cer  -alias myclientkey  -keystore $STS_STORE     -storepass stsspass
keytool -importcert -noprompt -trustcacerts -file myservicekey.cer -alias myservicekey -keystore $STS_STORE     -storepass stsspass

keytool -importcert -noprompt -trustcacerts -file mystskey.cer     -alias mystskey     -keystore $SERVICE_STORE -storepass sspass

keytool -importcert -noprompt -trustcacerts -file myactaskey.cer   -alias myactaskey   -keystore $CLIENT_STORE  -storepass cspass
keytool -importcert -noprompt -trustcacerts -file myservicekey.cer -alias myservicekey -keystore $CLIENT_STORE  -storepass cspass
keytool -importcert -noprompt -trustcacerts -file mystskey.cer     -alias mystskey     -keystore $CLIENT_STORE  -storepass cspass

keytool -importcert -noprompt -trustcacerts -file mystskey.cer     -alias mystskey     -keystore $ACTAS_STORE   -storepass aapass
