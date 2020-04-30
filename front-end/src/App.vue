<template>
  <div id="app">
    <v-app id="inspire">
      <v-app-bar app color="indigo" dark>
        <v-toolbar-title>CS3099 Trick Taker</v-toolbar-title>
      </v-app-bar>
      <router-view />
      <v-footer color="indigo" app>
        <span class="white--text">&copy;2019</span>
      </v-footer>
    </v-app>
  </div>
</template>

<style lang="less">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#nav {
  padding: 30px;

  a {
    font-weight: bold;
    color: #2c3e50;

    &.router-link-exact-active {
      color: #42b983;
    }
  }
}
</style>

<script>
export default {
  props: {
    source: String
  },
  data: () => ({
    drawer: null,
    error: ''
  }),
  methods: {
    toHome () {
      this.$router.push('/')
    },
    toGraph () {
      this.$router.push('/graph')
    },
    toText () {
      this.$router.push('/text')
    },
    toMixed () {
      this.$router.push('/mixed')
    },
    toConfirm () {
      this.$router.push('/confirmation')
    },

       getRank(rank){
      switch (rank){

        case 'ACE':
          return A
        break

        case 'TWO':
          return 2
        break

        case 'THREE':
          return 3
        break

        case 'FOUR':
          return 4
        break

        case 'FIVE':
          return 5
        break

        case 'SIX':
          return 6
        break

        case 'SEVEN':
          return 7
        break

        case 'EIGHT':
          return 8
        break

        case 'NINE':
          return 9
        break

        case 'TEN':
          return 10
        break

        case 'JACK':
          return J
        break

        case 'QUEEN':
          return Q
        break

        case 'KING':
          return K
        break

        default:
          return null;
          break
      }
    },

      getSuit(suitInput){
      switch(suitInput){
        case 'SPADE':
          return ''
        break

        default:
          return null;
          break
      }
    },

  },

  mounted () {
    this.$options.sockets.onmessage = (data) => {
      console.log(data)

      const temp = JSON.parse(data.data)
      console.log(temp.type)
    
      switch (temp.type) {
        case 'DiscoverGame':
          console.log(temp.beacons)
          var beacon
          var numOfPlayers = ''
          var tempGamesArr = []
          for (beacon of temp.beacons) {
            var beaconsAP = []
            beaconsAP = beacon.split(':')
            console.log(beaconsAP)
            numOfPlayers = beaconsAP[1] + '/' + beaconsAP[2]
            console.log(numOfPlayers)
            tempGamesArr.push(
              { name: beaconsAP[0], curPlayers: numOfPlayers, ip: beaconsAP[3], port: beaconsAP[4] }
            )
          }
          this.$store.commit('refreshGames', tempGamesArr)
          // console.log(beacon)
          break

        case 'playerjoin':
          var tempLoadingReminder

          tempLoadingReminder = 'player: port ' + temp.player.port + ' ip ' + temp.player.ip + 'joined'
          this.$store.commit('changeLoadingReminder', tempLoadingReminder)
          break

        case 'cardplayed':

          var tempPlayers = []
          tempPlayers = this.$store.state.players
          var p
          for (p in tempPlayers) {
            if (p === temp.playerindex) {
              tempPlayers[p].myturn = true
            } else {
              tempPlayers[p].myturn = false
            }
          }

          this.$store.commit('setPlayerTurn', tempPlayers)
          this.$store.commit('setCurPlayerIndex', temp.playerindex)
          console.log('PLAYER NUMBERS: ' + this.$store.state.players.length)
          var tempDPCardPool = this.$store.state.displayCardPool

          // if(tempDPCardPool.length>3){
          //   tempDPCardPool = []
          // }

          const tempDPCard = {
            rank: temp.card.rank, // TODO
            suit: temp.card.suit,
            imgPath: ''
          }
          tempDPCardPool.push(tempDPCard)

          this.$store.commit('setDisplayCard', tempDPCard)
          this.$store.commit('setDisplayCardPool', tempDPCardPool)
          break

        case 'playerhands':

          this.$store.commit('setLoadNotComplete', false)


         var tempHandCards = []

          var player
          var card
          for (player in temp.players) {
            if (this.$store.state.myselfIndex === temp.players[player].playerindex) {
              console.log('PLAYER ' + temp.players[player].playerindex)
              for (card in temp.players[player].hand) {
                const tempHandCard = { rank: '', suit: '', todisable: true }
                console.log('CARD ' + temp.players[player].hand[card])
                tempHandCard.rank = temp.players[player].hand[card].rank
                tempHandCard.suit = temp.players[player].hand[card].suit

                console.log(tempHandCard.rank)
                console.log(tempHandCard.suit)

                tempHandCards.push(tempHandCard)
              }
            }
          }
          console.log('TEST myselfIndex' + this.$store.state.myselfIndex)
          console.log('TEST players' + temp.players[0].hand[0].suit)

          var test1
          for (test1 in tempHandCards) {
            console.log('TEST_THC ' + tempHandCards[test1].suit + ' ' + tempHandCards[test1].rank)
          }

          console.log('TEST tempHandCards' + tempHandCards)
          this.$store.commit('setMyHandCards', tempHandCards)


          break

        // TODO
        case 'getCard':
          // var a
          // var b
          // var getCardMessage = 'Vaild Cards: \n'
          // var gccount

          // var tempHandCards = []

          // var player
          // var card
          // for (player in temp.playerhand.players) {
            // if (this.$store.state.myselfIndex === temp.playerhand.players[player].playerindex) {
            //   console.log('PLAYER ' + temp.playerhand.players[player].playerindex)
              // for (card in temp.playerhand) {
              //   const tempHandCard = { rank: '', suit: '', todisable: true }
              //   // console.log('CARD ' + temp.playerhand.players[player].hand[card])
              //   tempHandCard.rank = temp.playerhand[card].rank
              //   tempHandCard.suit = temp.playerhand[card].suit

              //   console.log(tempHandCard.rank)
              //   console.log(tempHandCard.suit)

              //   tempHandCards.push(tempHandCard)
              // }
            // }
          // }
          // console.log('TEST myselfIndex' + this.$store.state.myselfIndex)
          // console.log('TEST players' + temp.playerhand.players[0].hand[0].suit)

          // var test1
          // for (test1 in tempHandCards) {
          //   console.log('TEST_THC ' + tempHandCards[test1].suit + ' ' + tempHandCards[test1].rank)
          // }

          // console.log('TEST tempHandCards' + tempHandCards)
          // this.$store.commit('setMyHandCards', tempHandCards)




          // var tempHandCardsVaildCheck = []
          // tempHandCardsVaildCheck = this.$store.state.myHandCards

          // for (a in tempHandCardsVaildCheck) {
          //   for (b in temp.validcards) {
          //     if ((tempHandCardsVaildCheck[a].rank === temp.validcards[b].rank) && (tempHandCardsVaildCheck[a].suit === temp.validcards[b].suit)) {
          //       tempHandCardsVaildCheck[a].todisable = false
          //     }
          //   }
          // }

          // for (gccount in temp.validcards) {
          //   getCardMessage = getCardMessage + 'Rank: ' + temp.validcards[gccount].rank + ' Suit: ' + temp.validcards[gccount].suit + '\n'
          // }
          // this.$store.commit('appendGameMessage', getCardMessage)
          // this.$store.commit('setMyHandCards', tempHandCardsVaildCheck)
          // break
          
          var a
          var b
          var getCardMessage = 'Vaild Cards: \n'
          var gccount

          var tempHandCardsVaildCheck = []
          tempHandCardsVaildCheck = this.$store.state.myHandCards

          for (a in tempHandCardsVaildCheck) {
            for (b in temp.validcards) {
              if ((tempHandCardsVaildCheck[a].rank === temp.validcards[b].rank) && (tempHandCardsVaildCheck[a].suit === temp.validcards[b].suit)) {
                tempHandCardsVaildCheck[a].todisable = false
              }
            }
          }

          for (gccount in temp.validcards) {
            getCardMessage = getCardMessage + 'Rank: ' + temp.validcards[gccount].rank + ' Suit: ' + temp.validcards[gccount].suit + '\n'
          }
          this.$store.commit('appendGameMessage', getCardMessage)
          this.$store.commit('setMyHandCards', tempHandCardsVaildCheck)
          break



        case 'winningcard':
          // {"type":"winningcard","card":{"rank":"JACK","suit":"DIAMONDS"},"playerindex":0}
          // {"type":"trumpbroken"}
          // {"type":"gameendmessage","scores":[{"teamnumber":0,"teamscore":1},{"teamnumber":1,"teamscore":0}]}
          // {"type":"gameendmessage","scores":[{"teamnumber":0,"teamscore":5},{"teamnumber":1,"teamscore":0}]}
          // {"type":"matchendmessage","scores":[{"teamnumber":0,"teamscore":5},{"teamnumber":1,"teamscore":0}]}
          // Team: (0, 2)     5
          // Team: (1, 3)     0

          var winningMessage = 'Winning card: ' + temp.card.rank + ' ' + temp.card.suit + '\n' + 'Playerindex: ' + temp.playerindex + ' win this trick \n'

          setTimeout(5000)

          var tempDPCardPoolClear = []

          this.$store.commit('setDisplayCardPool', tempDPCardPoolClear)
          this.$store.commit('appendGameMessage', winningMessage)
          break

        case 'trumpbroken':

          var trumpbrokenMessage = 'Trump Broken \n'
          this.$store.commit('appendGameMessage', trumpbrokenMessage)
          break

        case 'gameendmessage':
          var gameendMessage = 'Game End: \n '

          var gecount
          for (gecount in temp.scores) {
            var tempMessageForGE = 'Team Number: ' + temp.scores[gecount].teamnumber + ' Score: ' + temp.scores[gecount].teamscore + '\n'
            gameendMessage = gameendMessage + tempMessageForGE
          }
          this.$store.commit('appendGameMessage', gameendMessage)
          break

        case 'matchendmessage':

          var matchendMessage = 'Match End: \n '

          var mecount
          for (mecount in temp.scores) {
            var tempMessageForME = 'Team Number: ' + temp.scores[mecount].teamnumber + ' Score: ' + temp.scores[mecount].teamscore + '\n'
            matchendMessage = matchendMessage + tempMessageForME
          }
          this.$store.commit('appendGameMessage', matchendMessage)
          break

        case 'currenttrump':

          var currenttrumpMessage = 'Current Trump: ' + temp.suit + '\n'

          this.$store.commit('setCurrentTrump', currenttrumpMessage)
          this.$store.commit('appendGameMessage', currenttrumpMessage)
          break

        case 'invalidCardMessage':
          alert('Invalid Card!')
          break


        case 'invalidBidMessage':
          alert('Invalid Bid')
          break


        case 'makebid':

          this.$store.commit('setLoadNotComplete', false)

          console.log('setSuitenabledBID '+ temp.suitenabled)
          console.log('setNumberofroundsenabledBID '+ temp.numberofroundsenabled)
          console.log('setDoublingenabledBID ' + temp.doublingenabled)
          console.log('setPassingenabledBID ' + temp.passingenabled)
          console.log('setBidblindenabledBID '+ temp.bidblindenabled)
          console.log('setIsPlayerVulnBID '+  temp.isPlayerVuln)
          console.log('setFirstroundBID '+ temp.firstround)

          this.$store.commit('setSuitenabledBID', temp.suitenabled)
          this.$store.commit('setNumberofroundsenabledBID', temp.numberofroundsenabled)
          this.$store.commit('setDoublingenabledBID', temp.doublingenabled)
          this.$store.commit('setPassingenabledBID', temp.passingenabled)
          this.$store.commit('setBidblindenabledBID', temp.bidlindenabled)
          this.$store.commit('setIsPlayerVulnBID', temp.isPlayerVuln)
          this.$store.commit('setFirstroundBID', temp.firstround)

          var tempNumberofroundsBID = [];
          var tempSuitBID = [];
          
          var tsb;
          var tnfb;
          
          if(temp.suitenabled){
            for(tsb in temp.suits){
              tempSuitBID.push(temp.suits[tsb])
              console.log(temp.suits[tsb])
            }
          }

          for (tnfb in temp.numberofrounds){
            tempNumberofroundsBID.push(temp.numberofrounds[tnfb])
            console.log(temp.numberofrounds[tnfb])
          }
          //arrays
          this.$store.commit('setSuitsBID', tempSuitBID)
          this.$store.commit('setNumberofroundsBID', tempNumberofroundsBID)
          break
        default:
          console.log('TEST' + temp.type)
      }
    }
  }
}
</script>
