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
    "gamepath": "Games/whist.json"
}
```
Then the game starts gathering players. As each player connects, their information is sent to the front end:
```
{
    "type":"gameplay",
    "subtype":"playerjoin",
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
