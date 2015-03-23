# LAS2peer-IdGeneratingService
LAS2peer-IdGeneratingService is a service that returns an ID when it is called.
The request needs to provide information:
  1. calling_service
  2. calling_method
  3. OIDC_user

The request information must be included in payload. The response contains the generated Id for this request.
The service will store the request information together with the generated ID and timestamp of the request in a MySQL database.
