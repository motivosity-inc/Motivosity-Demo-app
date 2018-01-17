Motivosity - Javascript Demo

This sample shows a pure javascript browser to server interaction.

How to use:
- Deploy the 'motivosity-demo' directory into an existing web server environment.
- Copy the fully qualified domain of the directory's location
- In Motivosity, enable the 'Javascript' Motivosity integration in the Setup->Integrations area and paste the domain into the CORS field.
- In Motivosity, click on 'View Details' of the newly created integration and copy the app_id.
- Go to your-server/motivosity-demo/index.html and paste the app_id into the right field and fill out the other fields appropriately.
- Click on 'Sign in'
- Click on 'List Motivosity Users'



Motivosity - OAuth Demo

This sample project shows a server to server OAuth 2 authorization flow between a webserver and Motivosity.

How to bring to life:
- this project uses maven for resolving dependencies so first you have to install Java 8 and Maven (https://maven.apache.org/)
- to resolve dependencies simple type "mvn clean install"
- to start the app import the project in your IDE and then execute the OauthDempApplication class. It starts up an embedded Tomcat server on port 8090
- type http://localhost:8090 in your browser


How to test:
- If you click on the "List Motivosity users" button the app tries to call the Motivosity user endpoint. If you have a valid access token to Motivosity stored in your server's VM then you have to see the list of usernames in the page,
- If it is a fresh start and you don't have access token to MV server then click on the Authorize link. This link brings you to Motivosity's application authorization page where you have to authorize your app


App data:
Have a look at the DemoController.java file. You have to set the right values in the class variables.
To get your own test app registered with Motivosity please send me a mail (support@motivosity.com) with the following info:
- your application’s name
- the scopes your app would like to use
- the IPs your demo and/or production server will run on
After we registered your app we will send you back all the required data what you have to set in DemoController.java.


The authorization flow
1. First your user clicks on the authorize link on your page that calls the DemoController.authorize() method. This method builds an authorization request URI with the right app info and redirects your user's browser to this URI.
2. Next your user have to sign in to Motivosity and have to authorize your app to give access to Motivosity. Later your user is able to revoke the authorization by going to Setup/Addons/Applications and click on the 'Revoke' button.
3. After the successful authorization your user is redirected back to the pre-registered (and sent in the request as a parameter) URI together with Motivosity's temporary code attached as a parameter. In this sample app the REDIRECT_URI is 'http://localhost:9080/app/authorize/code'
4. The DemoController.checkCode() method catches this redirection and - as the next step in the OAUth 2 flow - checks if the passed  code is valid and is really coming from Motivosity's auth server. This method will call back Motivosity in the background with the 'code' and as a result has to get a valid access token and the expires in parameter.
5. You have to store this access token carefully. It is a server to server token, never give it to your users. Although In our demo app it is stored as a static variable, but never use this solution. It is only for being simple.
