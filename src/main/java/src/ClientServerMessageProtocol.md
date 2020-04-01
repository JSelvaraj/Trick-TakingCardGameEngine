Front-end sends requests to back-end as JSON objects.
Types of request:
 -  DiscoverGame
 -  StopDiscoverGame
 -  GetGameList
 -  StartGame
 
Every time the Back-end responds to a request, it must add the type of request its responding to in the JSONObject

------------------------------------------------------------------------
#### DiscoverGame Messaging Protocol:

1. GUI sends 'DiscoverGame' request to back-end
2. Back-end collects beacons for LOOPTIME (from discoverGames.java) and sends them as a JSONArray to the GUI. (Repeats until 3.)
3. When GUI wants to stop receiving JSONArray, it sends 'StopDiscoverGame'.

Example response message:

{

    "type": "DiscoverGame", 
    "beacons":[JsonArray of received beacons]
    
}

-------------------

#### GetGameList Messaging Protocol:
Example response message:

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



-------------------------------------------------------------------------
#### StartGame Messaging Protocol:

Example response message:

{

    "type": "StartGame",
    "port": 6969,
    "aiplayers": 1,
    "gamepath": "Games/whist.json"
}
  