<template>
  <v-content>
    <div>
      Host a game
    </div>
    <div>
     <v-col class="d-flex" cols="12" sm="6">
        <v-select
          :items="gameTypes"
          v-model="selectGameTypes"
          label="Select a game type"
          @click= getNumOfPlayer()
          solo
        ></v-select>
        <p>Number of players:</p>
        <v-select
          :items="numOfPlayers"
          label="Select number of players"
          solo
        ></v-select>
        </v-col>
        </div>
        <div>
          <v-btn @click="toHome">Back</v-btn>
          <v-btn @click="toGameRoom">Host</v-btn>
        </div>
  </v-content>
</template>

<script>
import PostService from "../PostService";
import Axios from "axios";
import qs from "qs";
export default {
  
  props: {
    source: String
  },
  data: () => ({
    posts: [],
    error: "",
    answer: {
      id: "",
      gameTypes:[],
      numOfPlayers:[],
      selectGameTypes: null
    },
    isDisable: false
  }),

  async created() {
    this.gameTypes = ["Whist","Contract Whist","Spades","One Trick Pony"],
    this.numOfPlayers = [1,2,3,4];
  },
  methods: {
    getNumOfPlayer() {
      // console.log(this.selectGameTypes)
      // if(this.selectGameTypes==="Whist"||this.selectGameTypes ==="Contract Whist"||this.selectGameTypes==="Spades"){
      //   this.numOfPlayers = [1,2,3,4]
      // }else if(this.selectGameTypes==="One Trick Pony"){
      //   this.numOfPlayers = [1,2]
      // }
    },
    toGameRoom() {
      this.$router.push("/gameRoom");
      //PostService.insertPosts("HostGame");
      this.$socket.sendObj({type:"HostGame"});
    },
    toHome() {
      this.$router.push("/");
    },
  },
  mounted(){
    this.getNumOfPlayer();
  }
};

</script>

<style>
svg {
  width: 100%;
}
p {
  text-align: left;
}
.subq {
  text-align: left;
}
.vslider {
  width: 100%;
}
.submit {
  width: 100%;
}
</style>
