<template>
  <v-content>
    <v-container >

        <div class="reminder">
            test
        </div>
        <v-btn class="playerPosition1"> Player 1</v-btn>
        <v-btn class="playerPosition2"> Player 2</v-btn>
        <v-btn class="playerPosition3"> Player 3</v-btn>
        <v-btn class='playerPosition4'> Player 4</v-btn>
        <v-btn class="displayCard"> current card</v-btn>

        <div class='playerHand'>
        <v-btn style ='poker1'> Card 1
        <!-- <img src='../assets/img/0C.png'> -->
        </v-btn>
        <v-btn> Card 2</v-btn>
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
        <v-btn> Card 13</v-btn>
         </div>


         <modal>


         </modal>
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

created() {
    this.$options.sockets.onmessage = (data) => {

      console.log(data)

      const temp = JSON.parse(data.data)
      // console.log(temp.beacons)
      var beacon;
      var numOfPlayers = '';
      // for(beacon of temp.beacons) {
      //   var beaconsAP = []
      //   beaconsAP = beacon.split(":")
      //   console.log(beaconsAP)
      //   numOfPlayers = beaconsAP[1] + '/' + beaconsAP[2]
      //   console.log(numOfPlayers)
      //   this.games.push(
      //     {name:beaconsAP[0], curPlayers:numOfPlayers, ip:beaconsAP[3], port:beaconsAP[4]}
      //   )
      // }

      // console.log(beacon)
    
    } 
  },
  methods: {
  }
}
</script>

<style>

.playerPosition1 {
	/* height: 30%;
	width: 30%; */
  position: absolute;
	top: 30%;
	left: 30%;
}
.playerPosition2 {
  position: absolute;
	top: 10%;
	/* width: 30%;
	height: 30%; */
	left: 50%;
}
.playerPosition3 {
  position: absolute;
	top: 30%;
	/* width: 30%;
	height: 30%; */
	left: 70%;
}
.playerPosition4 {
  position: absolute;
	/* width: 30%;
	height: 30%; */
	top: 55%;
	left: 50%;
}

.displayCard{
  position: absolute;
  	top: 30%;
	left: 50%;
}

.playerHand {
	position: absolute;
  width: 50%;
	height: 20%;
	left: 28%;
	bottom: 10%;
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

</style>
