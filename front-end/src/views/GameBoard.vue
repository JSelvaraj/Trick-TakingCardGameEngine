/* eslint-disable no-tabs */
<template>
  <v-content>
    <v-container >
        <h1>
      GAME BOARD
    </h1>
        <v-btn class="playerPositionW"
        > 
        
        <i>Player 1</i>
        <v-icon>mdi-account</v-icon>
        </v-btn>
        <v-btn class="playerPositionN"
       
        > 
        <i>Player 2</i>
        <v-icon>mdi-account</v-icon>
        </v-btn>
        <v-btn class="playerPositionE"
      
        > 
        <i>Player 3</i>
        <v-icon>mdi-account</v-icon>
        </v-btn>
        <v-btn class='playerPositionS'
           
         @click="changeP1"> 
         <i>Player {{this.$store.state.myselfIndex}}</i>
        <v-icon>mdi-account</v-icon>
         </v-btn>

         <div class="displayCard">
           <div v-for="(item,i) in this.$store.state.displayCardPool" :key='i'>

             <div class="playingCards fourColours faceImages simpleCards inText rotateHand">
                <v-btn> 
                  {{item.rank}}
                  {{item.suit}}
                  </v-btn>
             </div>
            
           </div>
         </div>
       
        <div class="playerHand">
        <div v-for="(item,i) in this.$store.state.myHandCards" :key='i'>
        <v-btn @click="sendCard(item.rank,item.suit)"> {{item.suit}} {{item.rank}}</v-btn>


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
   

        <v-btn @giveBID()> TEST FOR BIDDING</v-btn>
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
    card_1_path: ''
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

    giveBID(){
      this.$socket.sendObj({
    "type":"givebid",
    "playerindex":0,
    "doubling": true,
    "suit": CLUBS,
    "value": 5,
    "blindBid" : true,
    "isPlayerVuln":true ,
    "firstround": true
        
      })


//       {


// }
    }

  //   scrollToElement() {
  //   const el = this.$el.getElementsByClassName('textArea')[0];

  //   if (el) {
  //     el.scrollIntoView();
  //   }
  // }
  },


  computed:{
  },

  mounted(){
    let elmnt = document.getElementById('bottom');
    elmnt.scrollIntoView(false);
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
  overflow-y: scroll;
  display: flex;
  flex-direction: column-reverse;
  width: 15%;
	height: 20%;
  	top: 25%;
	left: 45%;
}

.playerHand {
	position: absolute;
  align-items: stretch;
  width: 50%;
	height: 20%;
	left: 28%;
	bottom: 10%;
  text-align: start;
  border:3px solid black;
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
	left: 0%;
	bottom: 0%;
},

.currentTrumpDP{
  	left: 0%;
	bottom: 30%;
}
</style>
