Front-end sends requests to back-end as JSON objects.
Types of request:
 -  DiscoverGame
 -  StopDiscoverGame
 -  GetGameList
 -  StartGame
 -  SetHostPort
 -  SetDestAddress
 -  StartLocalGame
 
Every time the Back-end responds to a request, it must add the type of request its responding to in the JSONObject

------------------------------------------------------------------------
#### DiscoverGame messaging protocol:

1. GUI sends 'DiscoverGame' request to back-end
2. Back-end collects beacons for LOOPTIME (from discoverGames.java) and sends them as a JSONArray to the GUI. (Repeats until 3.)
3. When GUI wants to stop receiving JSONArray, it sends 'StopDiscoverGame'.

The structure of the message sent in 2:
{
    "type": "DiscoverGame" 
    "beacons":[JsonArray of received beacons]
}

-------------------------------------------------------------------------