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

    getRank (rank) {
      switch (rank) {
        case 'ACE':
          return 'A'
          break

        case 'TWO':
          return '2'
          break

        case 'THREE':
          return '3'
          break

        case 'FOUR':
          return '4'
          break

        case 'FIVE':
          return '5'
          break

        case 'SIX':
          return '6'
          break

        case 'SEVEN':
          return '7'
          break

        case 'EIGHT':
          return '8'
          break

        case 'NINE':
          return '9'
          break

        case 'TEN':
          return '10'
          break

        case 'JACK':
          return 'J'
          break

        case 'QUEEN':
          return 'Q'
          break

        case 'KING':
          return 'K'
          break

        default:
          return null
          break
      }
    },

    // getRankC (rank) {
    //       switch (rank) {
    //         case 'ACE':
    //           return 'a'
    //           break

    //         case 'TWO':
    //           return '2'
    //           break

    //         case 'THREE':
    //           return '3'
    //           break

    //         case 'FOUR':
    //           return '4'
    //           break

    //         case 'FIVE':
    //           return '5'
    //           break

    //         case 'SIX':
    //           return '6'
    //           break

    //         case 'SEVEN':
    //           return '7'
    //           break

    //         case 'EIGHT':
    //           return '8'
    //           break

    //         case 'NINE':
    //           return '9'
    //           break

    //         case 'TEN':
    //           return '10'
    //           break

    //         case 'JACK':
    //           return 'j'
    //           break

    //         case 'QUEEN':
    //           return 'q'
    //           break

    //         case 'KING':
    //           return 'k'
    //           break

    //         default:
    //           return null
    //           break
    //       }
    //     },
    getSuit (suitInput) {
      switch (suitInput) {
        case 'SPADE':
          return 'S'
          break

        case 'CLUBS':
          return 'C'
          break

        case 'HEARTS':
          return 'H'
          break

        case 'DIAMONDS':
          return 'D'
          break

        default:
          return null
          break
      }
    }

    // getSuitC (suitInput) {
    //   switch (suitInput) {
    //     case 'SPADE':
    //       return 'spades'
    //       break

    //     case 'CLUBS':
    //       return 'clubs'
    //       break

    //     case 'HEARTS':
    //       return 'hearts'
    //       break

    //     case 'DIAMONDS':
    //       return 'diams'
    //       break

    //     default:
    //       return null
    //       break
    //   }
    // }
  },

  mounted () {
    this.$options.sockets.onmessage = (data) => {
      console.log(data)

      const temp = JSON.parse(data.data)
      console.log(temp.type)

      var initPlayers = false

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

          // TODO FOR PLAYER INDICATOR

          // var tempPlayers = []
          // tempPlayers = this.$store.state.players
          // var p
          // for (p in tempPlayers) {
          //   if (p === temp.playerindex) {
          //     tempPlayers[p].myturn = true
          //   } else {
          //     tempPlayers[p].myturn = false
          //   }
          // }

          // this.$store.commit('setPlayerTurn', tempPlayers)
          // this.$store.commit('setCurPlayerIndex', temp.playerindex)

          // console.log('PLAYER NUMBERS: ' + this.$store.state.players.length)

          var tempDPCardPool = this.$store.state.displayCardPool
          var cardplayedMessage = 'Card Played by Player ' + temp.playerindex + ':\n Rank: ' + temp.card.rank + ' Suit: ' + temp.card.suit + '\n'
          // if(tempDPCardPool.length>3){
          //   tempDPCardPool = []
          // }

          const tempDPCard = {
            rank: temp.card.rank, // TODO
            suit: temp.card.suit,
            rankDP: this.getRank(temp.card.rank),
            // rankDPC: this.getRankC(temp.card.rank),
            suitDP: this.getSuit(temp.card.suit),
            // suitDPC: this.getSuitC(temp.card.suit),
            imgPath: '../assets/img/' + this.getRank(temp.card.rank) + this.getSuit(temp.card.suit) + '.svg'
          }
          tempDPCardPool.push(tempDPCard)

          this.$store.commit('appendGameMessage', cardplayedMessage)

          this.$store.commit('setDisplayCard', tempDPCard)
          this.$store.commit('setDisplayCardPool', tempDPCardPool)
          break

        case 'playernumber':
          this.$store.commit('changemyselfIndex', temp.index)
          break

        case 'playerhands':

          this.$store.commit('setLoadNotComplete', false)

          if (initPlayers === false) {
            var tempPlayersArr = []
            var playercount
            for (playercount in temp.players) {
              var playerTEMP = { playerindex: temp.players[playercount].playerindex, myturn: false }
              tempPlayersArr.push(playerTEMP)
            }
            this.$store.commit('setPlayers', tempPlayersArr)
          }

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
                tempHandCard.rankDP = this.getRank(temp.players[player].hand[card].rank)
               // tempHandCard.rankDPC = this.getRankC(temp.players[player].hand[card].rank)
                tempHandCard.suitDP = this.getSuit(temp.players[player].hand[card].suit)
               // tempHandCard.suitDPC = this.getSuitC(temp.players[player].hand[card].suit)
                tempHandCard.imgPath = '../assets/img/' + this.getRank(temp.players[player].hand[card].rank) + this.getSuit(temp.players[player].hand[card].suit) + '.svg'
                console.log(tempHandCard.rank)
                console.log(tempHandCard.suit)

                tempHandCards.push(tempHandCard)
              }
            }
          }
          console.log('TEST myselfIndex' + this.$store.state.myselfIndex)
          // console.log('TEST players' + temp.players[0].hand[0].suit)

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
          this.$store.commit('appendGameMessage', winningMessage)
          const vm = this
          setTimeout(function () {
            var tempDPCardPoolClear = []
            vm.$store.commit('setDisplayCardPool', tempDPCardPoolClear)
          }, 5000)

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

          if (this.$store.state.bidlindenabled) {
            this.$store.commit('setBidblindPOPUP', true)
          }
          this.$store.commit('setMainPOPUP', true)
          break

        case 'makebid':

          this.$store.commit('setLoadNotComplete', false)

          console.log('setSuitenabledBID ' + temp.suitenabled)
          console.log('setNumberofroundsenabledBID ' + temp.numberofroundsenabled)
          console.log('setDoublingenabledBID ' + temp.doublingenabled)
          console.log('setPassingenabledBID ' + temp.passingenabled)
          console.log('setBidblindenabledBID ' + temp.bidblindenabled)
          console.log('setIsPlayerVulnBID ' + temp.isPlayerVuln)
          console.log('setFirstroundBID ' + temp.firstround)

          this.$store.commit('setSuitenabledBID', temp.suitenabled)
          this.$store.commit('setNumberofroundsenabledBID', temp.numberofroundsenabled)
          this.$store.commit('setDoublingenabledBID', temp.doublingenabled)
          this.$store.commit('setPassingenabledBID', temp.passingenabled)
          this.$store.commit('setBidblindenabledBID', temp.bidlindenabled)
          this.$store.commit('setIsPlayerVulnBID', temp.isPlayerVuln)
          this.$store.commit('setFirstroundBID', temp.firstround)

          this.$store.commit('setBidblindPOPUP', temp.bidlindenabled)
          if (!temp.bidlindenabled) {
            this.$store.commit('setMainPOPUP', true)
          }
          var tempNumberofroundsBID = []
          var tempSuitBID = []

          var tsb
          var tnfb

          if (temp.suitenabled) {
            for (tsb in temp.suits) {
              tempSuitBID.push(temp.suits[tsb])
              console.log(temp.suits[tsb])
            }
          }

          for (tnfb in temp.numberofrounds) {
            tempNumberofroundsBID.push(temp.numberofrounds[tnfb])
            console.log(temp.numberofrounds[tnfb])
          }
          // arrays
          this.$store.commit('setSuitsBID', tempSuitBID)
          this.$store.commit('setNumberofroundsBID', tempNumberofroundsBID)
          break

        case 'bid':
          var tempBidMessage = 'Player ' + temp.playerindex + '\'s BID: \n'
          
          if(temp.value === -2){
          tempBidMessage = tempBidMessage + 'Value: '+ 'PASS' + '\n'
          } else {
          tempBidMessage = tempBidMessage + 'Value: '+ temp.value + '\n'
          }

          tempBidMessage = tempBidMessage + ' Doubling: ' + temp.doubling + '\n Blind Bid: ' + temp.blindBid + '\n '

          
          if(temp.suit === null){
          tempBidMessage = tempBidMessage + 'NO TRUMP' + '\n'
          } else {
          tempBidMessage = tempBidMessage + temp.suit + '\n'
          }
          this.$store.commit('appendGameMessage', tempBidMessage)
          break

        case 'dummyplayer':
          var tempDummyplayer = 'Player ' + temp.playerindex + 'is Dummy Player\n' + 'Dummy Player Hand: \n'
          var dummyplayerHandCount
          for (dummyplayerHandCount in temp.playerhand) {
            tempDummyplayer = tempDummyplayer + ' Rank: ' + temp.playerhand[dummyplayerHandCount].rank + ' Suit: ' + temp.playerhand[dummyplayerHandCount].suit + '\n'
          }
          this.$store.commit('setDummyplayerReminder', tempDummyplayer)
          this.$store.commit('appendGameMessage', tempDummyplayer)
          break

        case 'specialcard':
          var specialcardMessage = 'Player ' + temp.playerindex + ' played SPECIAL CARD: ' + temp.cardtype + '\n'
          this.$store.commit('appendGameMessage', specialcardMessage)
          alert(specialcardMessage)
          break

        case 'handswap':
          var swaphandsMessage = ''
          var swaphandsCount
          for (swaphandsCount in temp.playerswapped) {
            swaphandsMessage = swaphandsMessage + ' Player ' + temp.playerswapped[swaphandsCount] + ' \, \n'
          }
          swaphandsMessage = swaphandsMessage + 'Swapped Hands'

          this.$store.commit('appendGameMessage', swaphandsMessage)
          alert(swaphandsMessage)
          break

        case 'aitakeover':
          var aitakeoverMessage = 'AI is taking over playing the cards(and bidding) for Player ' + temp.playerindex + ' !\m'
          alert(aitakeoverMessage)
          this.$store.commit('appendGameMessage', aitakeoverMessage)
          break

        case 'roundendmessage':
          var roundendmessage = 'SCORES: \n'
          var roundendTeamCount
          var roundendTeamCount_innerloop
          for (roundendTeamCount in temp.scores) {
            roundendmessage = roundendmessage + 'Team number: ' + temp.teamnumber + '\n'
            roundendmessage = roundendmessage + 'Members: [ '
            for (roundendTeamCount_innerloop in temp.scores[roundendTeamCount].members) {
              roundendmessage = roundendmessage + temp.scores[roundendTeamCount].members[roundendTeamCount_innerloop] + ' '
            }
            roundendmessage = roundendmessage + ' ] \n'
            roundendmessage = roundendmessage + ' Team Score ' + temp.teamscore + '\n\n'
          }

          this.$store.commit('appendGameMessage', roundendmessage)
        default:
          console.log('TEST' + temp.type)
      }
    }
  }
}
</script>
