<template>
  <v-content>
    <div>
      GAME DISCOVER
    </div>
    <v-data-table
    :headers="headers"
    :items="games"
    :single-expand="singleExpand"
    :expanded.sync="expanded"
    item-key="name"
    show-expand
    class="elevation-1"
  >
    <!-- <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title>Expandable Table</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-switch v-model="singleExpand" label="Single expand" class="mt-2"></v-switch>
      </v-toolbar>
    </template> -->
    <template v-slot:expanded-item="{ headers, item }">
      <td :colspan="headers.length">More info about {{ item.name }}</td>
    </template>
  </v-data-table>
  <v-btn @click="stopDiscover">Stop Discover</v-btn>
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
    headers:[{text:"name",value:"name"},
    {text:"ip address",value:"ip"},
    {text:"port",value:"port"}],
    games:[{name:"haha",ip:"1.1.1.1",port:"9999"}]
  }),

  async created() {
  },
  methods: {
    stopDiscover(){
      // PostService.insertPosts("StopDiscoverGame");
    this.$socket.sendObj({type:"StopDiscoverGame"});
    }
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
