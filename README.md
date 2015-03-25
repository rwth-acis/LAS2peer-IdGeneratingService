# LAS2peer-IdGeneratingService
LAS2peer-IdGeneratingService is a service that returns an ID when it is called.
The POST request needs to provide information:
  1. calling_service
  2. calling_method
  3. OIDC_user

The request information must be included in payload. The response contains the generated Id for this request.
The service will store the request information together with the generated ID and timestamp of the request in a MySQL database.

##Requirements

* Installed Java 7 JDK
* Installed Apache Ant
* Installed MySQL

##Build

First, create the Id Generating Service database, refer to: [Database](https://github.com/rwth-acis/LAS2peer-IdGeneratingService/tree/master/DB).

Then, build the Service:

```
ant all
```

##Start

To start the Service, use one of the available start scripts:
  
  * `Windows: bin/startNetwork.bat`
  * `Unix, Mac: bin/startNetwork.sh`

After successful start,Annotations Service is available under

  [http://localhost:8082/generateId](http://localhost:8082/generateId)
  

##License
LAS2peer-IdGeneratingService is freely distributable under the [MIT License](https://github.com/rwth-acis/las2peer-IdGeneratingService/blob/master/LICENSE).
