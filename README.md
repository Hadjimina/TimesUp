# TimesUp
## How to send & receive messages from the server 
1. Make sure your class extends `ServerIOActivity`
  - `... extends ServerIOActivity` instead of `... extends AppCompatActivity`
  - You will now have to implement the `callback(DecodeMessage message)` function
    - This is where you will receive a reply from the server once it is available
    - Check out the protocol file section in the "RECEIVING" section to see what type of response you get
    - If you want to access one of the 4 header values simply "get it"
      - Example you want to get the "returnType": `message.getReturnType()` returns the returnType
    - If you want to access something in the body of the message you have to "get its type". 
      - Example you want to get a String array value with name "wordList": `message.getStringArray("wordList")` returns the wordList as a String array
      - Example you want to get a String value with name "activePlayer": `message.getString("activePlayer")` returns the activePlayer value as a String array
2. Change the callback activity to the current activity
  - `setCallbackActivity(this);`
    - setCallbackActivity() gets inhertied from ServerIOActivity
4. Send encoded messages to the server
  - `sendMessage(encodeMessage)`
    - sendMessage() get inherited from ServerIOActivity
  - Here encodedMessage is an object of class EncodeMessage which is basically the counterpart of DecodeMessage
  - Again check the protocol to see what kind of things it is used for (this time the "SENDING" part)
  - You can simply put all the data you need to send into the constructor of the EncodeMessage, the rest will be handled by the implementation of the constructor.
  
## Example of joining a game & receiving the response

    public class SomeActivit extends ServerIOActivity{
    
    ...
    //Some vars
    ..
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          
          setCallbackActivity(this);
          
          ...
          //Some code
          int gameId = ... //get those values through shared prefs 
          int clientId = ...
          int teamId = ...
          ...
          
          
          EncodeMessage messageToSend = new EncodeMessage(gameId, clientId, teamId);
          sendMessage(messageToSend);
          
        }
        
        public void callback(DecodeMessage message){
          ...
          //Handle response here
          ...
        }
    }
    
    
    
      
