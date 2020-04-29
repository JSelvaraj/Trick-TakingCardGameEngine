<template>
  <v-content>
    <v-container class="fill-height">
      <div class="loadingReminder_Cir">
        <h2>{{this.$store.state.loadingReminder}}</h2>
      </div>

    <v-progress-circular
    class="loadingCir"
      :size="70"
      :width="7"
      color="purple"
      indeterminate
    ></v-progress-circular>

    <v-btn @click="test1"> test</v-btn>
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
    isDisable: false
  }),

  async created () {
  },

  // TODO need to fix auto routing
  methods: {
    test1 () {
      this.$store.commit('loadingDisable')
      this.$router.push('/gameBoard')
      console.log(this.$store.state.loadEnable)
    }
  }

  // mounted: {

  //   checkGameStart () {
  //     if (this.$store.state.loadEnable===false) {
  //       this.$router.push('/gameBoard')
  //     }
  //   }

  // }
}
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
.loadingCir {
  position: absolute;
  left: 50%;
  top: 50%
}
 .loadingReminder_Cir {
  position:  absolute;
  left: 50%;
  top: 45%
  }
</style>
