These WSTrust tests use the same certificates as the WSTrust tests in modules/testsuite/cxf-tests.

When there is need for these certificates to be updated, copy the new ones from: 

```
$PROJECT_ROOT/modules/testsuite/cxf-tests/src/test/resources/jaxws/samples/wsse/policy/trust/WEB-INF/stsstore.jks
$PROJECT_ROOT/modules/testsuite/cxf-tests/src/test/resources/jaxws/samples/wsse/policy/trust/META-INF/clientstore.jks
$PROJECT_ROOT/modules/testsuite/cxf-tests/src/test/resources/jaxws/samples/wsse/policy/trust/WEB-INF/servicestore.jks
```
into:

```
$PROJECT_ROOT/modules/testsuite/cloud-tests/k8s/wstrust/sts/src/main/resources/stsstore.jks
$PROJECT_ROOT/modules/testsuite/cloud-tests/k8s/wstrust/service/src/test/resources/META-INF/clientstore.jks
$PROJECT_ROOT/modules/testsuite/cloud-tests/k8s/wstrust/service/src/main/resources/servicestore.jks
```
