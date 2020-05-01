/* eslint-disable no-mixed-spaces-and-tabs */
/* eslint-disable no-tabs */
<template>
  <v-content>
    <v-container >
        <h1>
      GAME BOARD
    </h1>
        <v-btn fab color="purple" class="playerPositionW" v-if="playerDISABLE_W"
        >
        <!-- <i>Player 1</i> -->
        <v-icon color="white">mdi-account</v-icon>

        </v-btn>
        <v-btn fab color="purple" class="playerPositionN"

        >
        <!-- <i>Player 2</i> -->
        <v-icon color="white">mdi-account</v-icon>
        </v-btn>
        <v-btn fab color="purple" class="playerPositionE"
        v-if="playerDISABLE_E"
        >
        <!-- <i>Player 3</i> -->
        <v-icon color="white">mdi-account</v-icon>
        </v-btn>
        <v-btn fab color="purple" class='playerPositionS'

         @click="changeP1">
         <!-- <i>Player {{this.$store.state.myselfIndex}}</i> -->
        <v-icon color="white">mdi-account</v-icon>
         </v-btn>

         <div class="displayCard">
           <div v-for="(item,i) in this.$store.state.displayCardPool" :key='i'>
<!-- playingCards fourColours faceImages simpleCards inText rotateHand" -->
             <!-- <div :class="'card rank-'+item.rankDP+' '+item.suitDPC"> -->
               <!-- <div class="playingCards simpleCards"> -->
              
                      <!-- <img height="300px"
                              width="200px" 
                              :src="item.imgPath"></img> -->
                    <v-btn class="singleCard"
                    large
                    v-bind:color="(item.suit==='HEARTS'|| item.suit==='DIAMONDS') ? 'red' : 'black'"
                    >
                        <p style="word-wrap: break-word;white-space: pre-line;  color: white; font-size: small">
             {{item.rank}} 
          {{item.suit}}</p> 
                    </v-btn>
           </div>
         </div>

        <div class="playerHand">
        <div v-for="(item,i) in this.$store.state.myHandCards" :key='i'>
        
          <!-- <div @click="sendCard(item.rank,item.suit)">
          <v-img height="300px"
            width="200px" 
            :src="imgPath(item.imgPath)"></v-img>
          </div> -->

          <v-btn class="singleCard" 
          large
           v-bind:color="(item.suit==='HEARTS'|| item.suit==='DIAMONDS') ? 'red' : 'black'"
          @click="sendCard(item.rank,item.suit)">
           <p style="word-wrap: break-word;white-space: pre-line;  color: white; font-size: small">
             {{item.rank}} 
          {{item.suit}}</p> 
          </v-btn>

<!-- <span class="rank">{{this.getRank(item.rank)}}</span>
                  <span class="suit">&spades;</span> -->

        <!-- <v-btn> Card 1 -->
        <!-- <img src='../assets/img/0C.png'> -->
        <!-- </v-btn> -->
        <!-- <v-btn> Card 2</v-btn>
        <v-btn> Card 3</v-btn>
        <v-btn> Card 4</v-btn>
        <v-btn> Card 5</v-btn>
        <v-btn> Card 6</v-btn>
        <v-btn> Card 7</v-btn>
        <v-btn> Card 8</v-btn>
        <v-btn> Card 9</v-btn>
        <v-btn> Card 10</v-btn>
        <v-btn> Card 11</v-btn>
        <v-btn> Card 12</v-btn>
        <v-btn> Card 13</v-btn> -->
<!-- v-bind:color="this.$store.state.players[0].myturn===false ? 'blue':'red'" -->
         </div>
         </div>

         <!-- <v-textarea
      class="textArea"
      label="Game Message"
      no-resize
      rows="10"
      :value="this.$store.state.gameMessage"
    ></v-textarea> -->

      <div id='bottom' class="textArea">
        <p style=" word-wrap: break-word;white-space: pre-line;">{{this.$store.state.gameMessage}}</p>
      </div>

          <p> {{this.$store.state.currenttrump}}</p>
        <!-- <v-btn @click="giveBID()"> TEST FOR BIDDING</v-btn> -->

<!-- <v-dialog v-model="downtemp"></v-dialog> -->

<!-- v-slot:activator="{ on }" -->
<v-dialog v-model="this.$store.state.bidblindPOPUP" persistent max-width="350">
      <v-card>
        <v-card-title class="headline">Do you want to bid blind?</v-card-title>
        <!-- <v-card-text>Let Google help apps determine location. This means sending anonymous location data to Google, even when no apps are running.</v-card-text> -->
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="green darken-1" text @click="bidblindANSWERNO()">NO</v-btn>
          <v-btn color="green darken-1" text @click="bidblindANSWERYES()">YES</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>


<v-dialog v-model="this.$store.state.mainPOPUP" persistent max-width="350">
      <v-card>
        <v-card-title class="headline">    MAKE BID</v-card-title>
         <p 
         v-if="this.$store.state.numberofroundsenabledBID"
         >Number of rounds:</p>
         <v-select 
         v-if="this.$store.state.numberofroundsenabledBID"
          :items="this.$store.state.numberofroundsBID"
          v-model="numberofroundsANSWER"
          label="Select number of rounds"
          solo
        ></v-select> 
        <p v-if="this.$store.state.suitenabledBID">Suit:</p>
        <v-select v-if="this.$store.state.suitenabledBID"
          :items="this.$store.state.suitsBID"
          label="Select suit"
          v-model="suitsANSWER"
          solo
        ></v-select>
        <v-checkbox 
        v-if="this.$store.state.doublingenabledBID"
        v-model="doublingANSWER" class="mx-2" label="Doubling"></v-checkbox>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn 
          v-if="this.$store.state.passingenabledBID"
          color="green darken-1" text @click="giveBID_PASS()">PASS</v-btn>
          <v-btn color="green darken-1" text @click="giveBID()">BID</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <div class="dummyBoard" >
      <p style=" word-wrap: break-word;white-space: pre-line;"> {{this.$store.state.dummyplayerReminder}}</p>
    </div>
    </v-container>
  </v-content>
</template>

<script>
import PostService from '../PostService'
import Axios from 'axios'
import qs from 'qs'
export default {

  props: {
    source: String
  },
  data: () => ({
    posts: [],
    error: '',
    answer: {
      id: ''
    },
    isDisable: false,
    card_1_path: '',
    downtemp: true,
    numberofroundsANSWER: '',
    bidblindANSWER: false,
    doublingANSWER: false
  }),

  created () {
  // this.$store.commit('initPlayerArr',4)

    //     this.$options.sockets.onmessage = (data) => {

    //       console.log(data)

    //       const temp = JSON.parse(data.data)
    //       // console.log(temp.beacons)
    //       // var beacon;
    //       // var numOfPlayers = '';
    //       // for(beacon of temp.beacons) {
    //       //   var beaconsAP = []
    //       //   beaconsAP = beacon.split(":")
    //       //   console.log(beaconsAP)
    //       //   numOfPlayers = beaconsAP[1] + '/' + beaconsAP[2]
    //       //   console.log(numOfPlayers)
    //       //   this.games.push(
    //       //     {name:beaconsAP[0], curPlayers:numOfPlayers, ip:beaconsAP[3], port:beaconsAP[4]}
    //       //   )
    //       // }
    //       // console.log(beacon)
    //     }
  },
  methods: {
    changeP1 () {
      this.$store.dispatch('setPlayerTurn_AC', 0)
    },

    sendCard (rank_input, suit_input) {
      this.$socket.sendObj({
        type: 'playcard',
        playerindex: this.$store.state.myselfIndex,
        card: {
          rank: rank_input,
          suit: suit_input
        }
      })
    },

    giveBID () {
      this.$socket.sendObj({
        type: 'givebid',
        playerindex: this.$store.state.myselfIndex,
        doubling: this.doublingANSWER,
        suit: this.$store.state.suitsANSWER,
        value: this.numberofroundsANSWER,
        blindBid: this.bidblindANSWER,
        isPlayerVuln: this.$store.state.isPlayerVulnBID,
        firstround: this.$store.state.firstroundBID
      })
      this.$store.commit('setMainPOPUP', false)
    },

    giveBID_PASS () {
      this.$socket.sendObj({
        type: 'givebid',
        playerindex: this.$store.state.myselfIndex,
        doubling: this.doublingANSWER,
        suit: this.$store.state.suitsANSWER,
        value: -2,
        blindBid: this.bidblindANSWER,
        isPlayerVuln: this.$store.state.isPlayerVulnBID,
        firstround: this.$store.state.firstroundBID
      })
      this.$store.commit('setMainPOPUP', false)
    },

    bidblindANSWERNO(){
      this.$store.commit('setBidblindPOPUP',false)
      this.bidblindANSWER = false;
      
      let vm = this;
      setTimeout(function(){
        vm.$store.commit('setMainPOPUP', true)
      },5000)
    },

    bidblindANSWERYES(){
      this.$store.commit('setBidblindPOPUP',false)
      this.bidblindANSWER = true;
        this.$store.commit('setMainPOPUP', true)
      
    },


    imgPath(item){ return require(item)}
    //   scrollToElement() {
    //   const el = this.$el.getElementsByClassName('textArea')[0];

  //   if (el) {
  //     el.scrollIntoView();
  //   }
  // }
  },

  computed: {
    playerDISABLE_W: function () {
      if (this.$store.state.players.length < 2) {
        return false
      } else {
        return true
      }
    },

     playerDISABLE_E: function () {
      if (this.$store.state.players.length < 3) {
        return false
      } else {
        return true
      }
    },

    suitsANSWER:{
      get(){
        return this.$store.state.suitsANSWER
      },
      set(value){
        this.$store.commit('setSuitsANSWER',value)
      }
    }

  },

  mounted () {
    const elmnt = document.getElementById('bottom')
    elmnt.scrollIntoView(false)
  }
}
</script>

<style>

 @import '../assets/cards/cards.css';

.playerPositionW {
  position: absolute;
	top: 30%;
	left: 30%;
}

/*
.playerPosition1_Selected {
  position: absolute;
	top: 30%;
	left: 30%;
  color: red;
} */

.playerPositionN {
  position: absolute;
	top: 10%;
	/* width: 30%;
	height: 30%; */
	left: 50%;
}
.playerPositionE {
  position: absolute;
	top: 30%;
	/* width: 30%;
	height: 30%; */
	left: 70%;
}
.playerPositionS {
  position: absolute;
	/* width: 30%;
	height: 30%; */
	top: 55%;
	left: 50%;
}

.displayCard{
  position: absolute;
  /* overflow-y: scroll; */
  display: flex;
  flex-direction: column-reverse;
  width: 15%;
	height: 20%;
  	top: 25%;
	left: 50%;
}

.playerHand {
	position: absolute;
  align-items: stretch;
  width: 50%;
	height: 20%;
	left: 28%;
	bottom: 10%;
  text-align: start;
  /* border:3px solid black; */
   text-align:center;
   display: flex;
  flex-direction: row;
  /* overflow: hidden */
}

.v-btn.center{
  position: relative;
  left: 30px;
  border: 3px solid #73AD21;
}

.reminder {
	position: absolute;
	left: 50%;
	top: 0%;
}

.textArea {
  overflow-y: scroll;
  display: flex;
  flex-direction: column-reverse;
  position: absolute;
  width: 25%;
	height: 40%;
	left: 1%;
	bottom: 0%;
},

.currentTrumpDP{
  	left: 0%;
	bottom: 30%;
}

.dummyBoard {
  display: flex;
  flex-direction: column-reverse;
  position: absolute;
  width: 25%;
	height: 40%;
	right: 0%;
	top: 0%;
}

.singleCard{
  width: 3%;
	height: 6%;
   display: flex;
  flex-direction: column;
  align-items: center;
  margin: 1%;
  /* width: 15%;
	height: 30%; */

}
</style>
