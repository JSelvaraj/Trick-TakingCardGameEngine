<template>
  <div id="app">
    <v-app id="inspire">
      <v-app-bar app color="indigo" dark>
        <v-toolbar-title>CS3099 Trick Taker</v-toolbar-title>
      </v-app-bar>
      <router-view />
      <v-footer color="indigo" app>
        <span class="white--text">&copy;2019</span>
      </v-footer>
    </v-app>
  </div>
</template>

<style lang="less">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#nav {
  padding: 30px;

  a {
    font-weight: bold;
    color: #2c3e50;

    &.router-link-exact-active {
      color: #42b983;
    }
  }
}
</style>

<script>
export default {
  props: {
    source: String
  },
  data: () => ({
    drawer: null,
    error: ""
  }),
  methods: {
    toHome() {
      this.$router.push("/");
    },
    toGraph() {
      this.$router.push("/graph");
    },
    toText() {
      this.$router.push("/text");
    },
    toMixed() {
      this.$router.push("/mixed");
    },
    toConfirm() {
      this.$router.push("/confirmation");
    }
  }
};
</script>
