Front-end sends requests to back-end as JSON objects.
Types of request:
 -  DiscoverGame - When you want to gather UDP beacons for available games.
 -  StopDiscoverGame - When you want to stop looking for new game beacons.
 -  GetGameList
 -  StartGame
 -  SetHostPort
 -  SetDestAddress
 -  StartLocalGame

{
    "type": "DiscoverGame" | "
}



------------------------------------------------------------------------
DiscoverGame messaging protocol:

1. GUI sends 'DiscoverGame' request to back-end
2. Back-end collects beacons for LOOPTIME (from discoverGames.java) and sends them as a JSONArray to the GUI. (Repeats until 3.)
3. When GUI wants to stop receiving JSONArray, it sends 'StopDiscoverGame'.

-------------------------------------------------------------------------