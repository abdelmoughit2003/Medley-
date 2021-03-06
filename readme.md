#Medly
### Medication Management
Medly medication management software was designed and created as a
capstone project for [Codeup][1]. The product frontend was built
using HTML5, CSS3/Materialize, and paralax. The backend of the project
was built on java, utilizing Spring, Hibernate and mysql. 

![medly picture](src/main/resources/static/img/medly01.png "Medly")

The application requires users to register. Once registration is complete,
a text message, using [Twillio][2], will be sent to the phone number you used
to register with. 

![medly picture](src/main/resources/static/img/medly02.png "Medly")

After you have registered, you will be brought to the login page where 
you would enter your credentials. After logging in, you will be directed
to the main dashboard where you could enter your medication/prescriptions, view alerts,
and create upcoming evnts as reminders. 
![medly picture](src/main/resources/static/img/medly03.png "Medly")


[1]: http://www.codeup.com/    "Codeup" 
[2]: http://www.twillio.com/    "Twillio" 
[3]: http://www.codeup.com/    "Codeup" 

```java
@Service("twillioSvc")
public class TwillioService {
    ...
    public void sendSMS(String body, String to) {
            try {
                TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
    
                // Build a filter for the MessageList
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("Body", body));
                params.add(new BasicNameValuePair("To", to));
                params.add(new BasicNameValuePair("From", TWILIO_NUMBER));
    
                MessageFactory messageFactory = client.getAccount().getMessageFactory();
                Message message = messageFactory.create(params);
                System.out.println(message.getSid());
            }
            catch (TwilioRestException e) {
                System.out.println(e.getErrorMessage());
            }
        }
    ...
    }
```