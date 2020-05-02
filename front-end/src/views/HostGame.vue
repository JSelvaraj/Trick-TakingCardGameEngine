<template>
  <v-content>

    <div class="title">
    <h1>
      Host a game
    </h1>
    </div>
    <div>

     <p class="gameTypeTitle">Game Type:</p>
        
        
        <v-select
        class="gameType"
          :items="gameTypes"
          v-model="selectGameTypes"
          label="Select a game type"
          @change = test(selectGameTypes)
          solo
        ></v-select>
       
       
        <p class="numAIplayerTitle">Number of AI players:</p>
        <v-select
          class="numAIplayer"
          :items="numOfAIPlayers_AP"
          v-model="selectNumOfAIPlayer"
          label="Select number of AI players"
          @change = test2()
          solo
        ></v-select>
        <v-checkbox class="rdmeventCheckBox"
        v-model="rdmeventANSWER" label="Random Event"></v-checkbox>
        </div>
        <div>
          <v-btn class="backBTN" @click="toHome">Back</v-btn>
          <v-btn class="hostBTN" @click="toGameRoom">Host</v-btn>
        </div>
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
      id: '',
      gameTypes: [],
      numOfAIPlayers: [],
      selectGameTypes: '',
      selectNumOfAIPlayer: null,
      rdmeventANSWER: false
    },
    isDisable: false
  }),

  created () {
    this.gameTypes = ['Whist', 'Contract Whist', 'Spades', 'One Trick Pony','Bridge'],
    // this.numOfAIPlayers = [0, 1, 2, 3]
     this.selectGameTypes = '',
     this.selectNumOfAIPlayer = null,
     this.rdmeventANSWER = false
  },

  computed: {
    numOfAIPlayers_AP: function () {
      console.log(this.$store.state.selectGameTypes_vx)
      if (this.$store.state.selectGameTypes_vx === 'Whist' || this.$store.state.selectGameTypes_vx === 'Contract Whist' || this.$store.state.selectGameTypes_vx === 'Spades'||this.$store.state.selectGameTypes_vx === 'Bridge') {
        return [0, 1, 2, 3]
      } else if (this.$store.state.selectGameTypes_vx === 'One Trick Pony') {
        return [0, 1]
      } else {
        return [0]
      }
    },

    gamePath_AP: function () {
      console.log("TEST GP"+this.$store.state.selectGameTypes_vx)
      if (this.$store.state.selectGameTypes_vx === 'Whist') {
        return "Games/whist.json"
      } else if (this.$store.state.selectGameTypes_vx === 'Contract Whist') {
        return "Games/contractWhist.json"
      } else if (this.$store.state.selectGameTypes_vx === 'Spades'){
        return "Games/spades.json"
      } else if (this.$store.state.selectGameTypes_vx === 'One Trick Pony') {
        return "Games/OneTrickPony.json"
      } else if(this.$store.state.selectGameTypes_vx === 'Bridge'){
        return "Games/bridge.json"
      }else {
        return null
      }
    }

  },

  methods: {
    // getNumOfPlayer(item) {
    //   console.log(this.selectGameTypes)
    //   if(item==="Whist"||item ==="Contract Whist"||item==="Spades"){
    //     this.numOfAIPlayers = [0,1,2,3]
    //   }else if(item==="One Trick Pony"){
    //     this.numOfAIPlayers = [0,1,2]
    //   }
    // },

    // test (item) {
    //   // this.numOfAIPlayers = [0,1,2]
    //   console.log(item)"Games/whist.json"
    //   if (item === 'Whist' || item === 'Contract Whist' || item === 'Spades') {
    //     this.numOfAIPlayers = [0, 1, 2, 3]
    //   } else if (item === 'One Trick Pony') {
    //     this.numOfAIPlayers = [0,1,2]
    //     console.log(this.numOfAIPlayers)
    //   //  this.$set(this.numOfAIPlayers,[0,1,2])
    //   }
    // },
    // NEW PORT NUM FOR GAME: 60001

    toGameRoom () {
      this.$router.push('/gameRoom')
      this.$store.commit('setMyselfIndex',0)


      var i;
    var players_temp = []
    for (i = 0; i < 4; i++) {
      players_temp.push({ myturn: false })
    }
    this.$store.commit('setPlayerArr', players_temp)

      // PostService.insertPosts("HostGame");
      this.$socket.sendObj({
        type: "HostGame",
        aiplayers: this.selectNumOfAIPlayer,
        gamepath: this.gamePath_AP,
        enableRdmEvents: this.rdmeventANSWER,
        port: "55555"
      })
    },
    toHome () {
      this.$router.push('/')
    },

    test(item){
      // console.log("TEST1"+this.selectGameTypes)
      // this.selectGameTypes = item
      // console.log("TEST2"+this.selectGameTypes)
      // computed.numOfAIPlayers_AP()

      console.log(item)
      this.$store.commit('changeSelectGameTypes_vx',item)
    },

    test2(){
      console.log(this.selectNumOfAIPlayer)
    }
  }
}

</script>

<style>
svg {
  width: 100%;
}
p {
  text-align: left;
}

.title{
  position: absolute;
  top: 20%;
	left: 44%;
  /* width: 17%; */
}

.gameTypeTitle{
  position: absolute;
  top: 27%;
	left: 40%;
  width: 17%;
}

.gameType{
  position: absolute;
  	top: 30%;
	left: 40%;
  width: 17%;
}

.numAIplayerTitle{
  position: absolute;
  top: 37%;
	left: 40%;
}

.numAIplayer{
  position: absolute;
  width: 17%;
  top: 40%;
	left: 40%;
}


.rdmeventCheckBox{
  position: absolute;
  width: 17%;
  top: 45%;
	left: 40%;
}

.backBTN{
  position: absolute;
  top: 50%;
	left: 40%;
}

.hostBTN{
  position: absolute;
  top: 50%;
	left: 55%;
}

</style>
