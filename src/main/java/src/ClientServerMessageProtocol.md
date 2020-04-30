Front-end sends requests to back-end as JSON objects.
Types of request:
 -  DiscoverGame
 -  StopDiscoverGame
 -  GetGameList
 -  HostGame
 -  JoinGame
 
Every time the Back-end responds to a request, it must add the type of request its responding to in the JSONObject

------------------------------------------------------------------------
#### DiscoverGame Messaging Protocol:

1. GUI sends 'DiscoverGame' request to back-end
2. Back-end collects beacons for LOOPTIME (from discoverGames.java) and sends them as a JSONArray to the GUI. (Repeats until 3.)
3. When GUI wants to stop receiving JSONArray, it sends 'StopDiscoverGame'.

Example response message:
```
{
    "type": "DiscoverGame", 
    "beacons":[JsonArray of received beacons(Beacon format is in supergroupcode)] 
}
```
-------------------

#### GetGameList Messaging Protocol:
Example response message:
```
{
    "type": "GetGameList",
    "games": [
            {
                "path": "Games/whist.json",
                "gamedesc": {<Whist Description>}
            },
            {
                "path": "Games/spades.json",
                "gamedesc": {<Spades Description>}
        ]
}            
```


-------------------------------------------------------------------------
#### HostGame Messaging Protocol:
Request from GUI:
```
{
    "type":"HostGame"
}
```
Example response message:
```
{
    "type": "HostGame",
    "aiplayers": 1,
    "gamepath": "Games/whist.json",
    "enableRdmEvents":false
    "port":number
}
```
Then the game starts gathering players. As each player connects, their information is sent to the front end:
```
{
    "type":"playerjoin",
    "playerindex":1,
    "player": {player object}
}
```
the {player object} is from the supergroup code, copied here for ease:

```
{
    ip: string,
    port: number,
    name: string 
}
```
  
------------

#### JoinGame Messaging Protocol:
Request from GUI:
```
{
    "type":"JoinGame"
}
```
```
{
    "type":"JoinGame",
    "address":"pc3-013-l",
    "port":10293,
    "localport":6969,
}
```

#### Player Number
When joining a game, this message tells the front-end what the local player's player index is:
```
{
    type:playernumber,
    index: int,
}
```



##GamePlay Messaging
These are the messages that will occur once a game is started.


### 1. GameServer Connect
The game has its own websocket, the address of this websocket is sent along the old websocket when the game starts.
It expects a connection as response.
```
{
    "type":"gamesetup",
    "port":"6969" /* This is just an example number the number is randomly chosen normally */
}
```
### 2. Player Hands at the top of the trick
At the start of each trick every player's hand is sent to the front-end. An example JSON object is shown below:
```
{
    "type":"playerhands",
    "players":
    [{
        "playerindex":1,
        "hand":
        [
            {
                "suit":"SPADES",
                "rank":"QUEEN"
            } 
        ],              /* assume there usually be more than one card in this array */ 
    }] /* Assume there will be more than one player in this array*
}
```

###Swap Hands
Request to swap hands
```
{
    "type":swaphands,
    "choosingplayer":1, /* index of the player choosing the cards to swap */
    "otherplayer":3     /* index of the player having a card swapped */
}
```
###Swap Card
Request to swap card:
```
{
    "type":"getswap",
    "choosingplayer":1,
    "otherplayer":3
}
```
Response:
```
{
    "type":"getswap",
    "choosingplayer":1,
    "otherplayer":3,
    "choosingplayercard":
    {
        "rank":"TWO",
        "suit":"CLUBS"
    },
    "otherplayercard":
    {
        "rank":"ACE",
        "suit":"SPADES"
    }
}
```
### Making a bid
Request:
```
{
    "type":"makebid",
    "validbids":
    [ /* Bids are in same format as supergroup schema*/
        {
            type: "bid",
            doubling: boolean,
            suit: Suit,
            value: number,
            blindBid : boolean
        },
        {
            type: "bid",
            doubling: boolean,
            suit: Suit,
            value: number,
            blindBid : boolean
        }
    ],
    "isPlayerVuln":boolean /* Don't worry about what this is, just copy it to response */
}
```
Response Format:
```
{
    "type":"makebid",
    "bid":
    {
        "type": "bid",
        "doubling": boolean,
        "suit": Suit,
        "value": number,
        "blindBid" : boolean
        "isPlayerVuln":boolean 
    }
}
```
### Telling front-end the current trump suit
Message format:
```
{
    "type":"currenttrump",
    "suit":suit
}
```

###Playing a card in the Trick
First the Back-end makes a request to the front end, with a list of valid cards in the players hand:
```
{
    "type":"getCard",
    "playerhand": {json array of hand}
    "validcards":
    [{
        "rank":"TWO",
        "suit":"SPADES"
    },
    {   
        "rank":"ACE",
        "suit":"CLUBS"
    }]
}
```
It then expects a response from the Front end with the card played:
```
{
    "type":"playcard",
    "card":
    {
        "rank":"ACE",
        "suit":"CLUBS"
    }
}
```
###Broadcast plays
Every card(including the one played by the local player) is sent to the front-end when it is played. In the following format:
```
{
    "type":"cardplayed",
    "playerindex":1,
    "card":
    {
        "rank":"ACE",
        "suit":"CLUBS"
    }
}
```
### Special Card Played
```
{
    "type":"specialcard",
    "player":1,
    "team":1,
    "cardtype":"bomb"
}
```
###Winning Card
At the end of each trick the winning card and the owner of the winning card is sent to  the front-end:
```
{
    "type":"winningcard",
    "card":
    {
        "rank":"ACE",
        "suit":"CLUBS"
    }
    "playerindex":1
}
```
### Trump Broken
When the trump suit is played for the first time in a game that needs to break the trump suit the following message is sent:
```
{
    "type":"trumpbroken"
}
```
### Game ended
When the game ends a message is sent to the GUI:
```
{
    "type":"roundendmessage"
    "scores":
    [{
        "teamnumber":number,
        "teamscore":number,
    },
    {
        "teamnumber":number,
        "teamscore":number,
    }]
}
```
### match ended
When the Match ends a message is sent to the GUI:
```
{
    "type":"roundendmessage"
    "scores":
    [{
        "teamnumber":number,
        "teamscore":number,
    },
    {
        "teamnumber":number,
        "teamscore":number,
    }]
}
```

        


    