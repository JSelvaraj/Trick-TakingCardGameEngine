<template>
  <v-content>
    <h1>
      GAME DISCOVER
    </h1>
    
    <!--<v-data-table
    :headers="headers"
    :items="games"
    :single-expand="singleExpand"
    :expanded.sync="expanded"
    item-key="name"
    class="elevation-1"
  > -->
    <!-- <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title>Expandable Table</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-switch v-model="singleExpand" label="Single expand" class="mt-2"></v-switch>
      </v-toolbar>
    </template> -->
    
    <!--
    <template v-slot:expanded-item="{ headers, item }">
     
    </template>
  </v-data-table>
 -->
<!-- 
<v-container class="bv-example-row">

 <v-row no-gutters>
      <template v-for="n in 8">
        <v-col :key="n">
          <v-card
            class="pa-2"
            outlined
            tile
          >
            <v-btn>
            JOIN
            </v-btn>
            
          </v-card>
        </v-col>
        <v-responsive
          v-if="n === 4"
          :key="`width-${n}`"
          width="100%"
        ></v-responsive>
      </template>
    </v-row>

</v-container> -->

<v-data-table :headers="headers" :items="this.$store.state.games">
      <template v-slot:item="row">
          <tr>
            <td>{{row.item.name}}</td>
            <td>{{row.item.curPlayers}}</td>
            <td>{{row.item.ip}}</td>
            <td>{{row.item.port}}</td>
            <td>
                <v-btn class="mx-2" dark small color="red" @click="onButtonClick(row.item)">
                    JOIN
                </v-btn>
            </td>
          </tr>
      </template>
    </v-data-table>


<v-btn class="backBTNGD" @click="toHome">Back</v-btn>

  <v-btn @click="stopDiscover">Refresh</v-btn>
  </v-content>
</template>

<script>
import PostService from "../PostService";
import Mermaid from "mermaid";
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
    },
    isDisable: false,
    headers:[
      {text:"name",value:"name"},
      {text:"current players",value:"curPlayers"},
      // {text:"room size",value:"roomsize"},
      //{name:"haha",ip:"1.1.1.1",port:"9999"}
      {text:"ip address",value:"ip"},
      {text:"port",value:"port"}
    ],
    games:[]
  }),

  created() {
  },
  methods: {
    stopDiscover() {
      // PostService.insertPosts("StopDiscoverGame");
      this.$socket.sendObj({type:"DiscoverGame"});
    },

    onButtonClick(item) {
      this.$socket.sendObj({type:"JoinGame",address:item.ip,port:item.port,localport:"9091"})
    },

    toHome() {
      this.$router.push("/");
    },
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

.backBTNGD{
  margin: 5%;
}

</style>
