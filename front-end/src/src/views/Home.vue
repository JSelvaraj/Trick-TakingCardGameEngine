<template>
  <v-content>
    <v-container
        class="fill-height"
        fluid
      >
        <v-row
          align="center"
          justify="center"
        >
          <v-col class="text-center">
       <div>
              <v-btn color="primary" @click="toHostGame"> HOST A GAME </v-btn>
            </div>
            <br>
            <div>
               <v-btn color="primary" @click="toGameDiscover"> FIND A GAME </v-btn>
            </div>
          </v-col>
        </v-row>
    </v-container>
  </v-content>
</template>

<script>
import PostService from "../PostService";
export default {
  name: "Home",

  props: {
    source: String
  },
  data: () => ({
  }),
  methods:{
      toHostGame() {
      this.$router.push("/hostGame");
    },
    toGameDiscover() {
      this.$router.push("/gameDiscover");
      //PostService.insertPosts("DiscoverGame");
      this.$socket.sendObj({type:"DiscoverGame"});
    },
  }
};
</script>
<style>
h1 {
  text-align: top;
}
</style>
