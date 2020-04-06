<template>
  <v-content>
    <v-container class="fill-height">
    
    </v-container>
  </v-content>
</template>

<script>
import PostService from "../PostService";
import Axios from "axios";
import qs from "qs";
export default {
  name: "GraphQuestion",
  props: {
    source: String
  },
  data: () => ({
    posts: [],
    error: "",
    answer: {
      id: "",
    },
    isDisable: false
  }),

  async created() {
  },
  methods: {
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
