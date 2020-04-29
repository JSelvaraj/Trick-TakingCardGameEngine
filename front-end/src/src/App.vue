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
    error: ''
  }),
  methods: {
    toHome () {
      this.$router.push('/')
    },
    toGraph () {
      this.$router.push('/graph')
    },
    toText () {
      this.$router.push('/text')
    },
    toMixed () {
      this.$router.push('/mixed')
    },
    toConfirm () {
      this.$router.push('/confirmation')
    }
  },

  mounted () {
    this.$options.sockets.onmessage = (data) => {
      console.log(data)

      const temp = JSON.parse(data.data)
      console.log(temp.type)

      switch (temp.type) {
        case 'DiscoverGame':
          console.log(temp.beacons)
          var beacon
          var numOfPlayers = ''
          var tempGamesArr = []
          for (beacon of temp.beacons) {
            var beaconsAP = []
            beaconsAP = beacon.split(':')
            console.log(beaconsAP)
            numOfPlayers = beaconsAP[1] + '/' + beaconsAP[2]
            console.log(numOfPlayers)
            tempGamesArr.push(
              { name: beaconsAP[0], curPlayers: numOfPlayers, ip: beaconsAP[3], port: beaconsAP[4] }
            )
          }
          this.$store.commit('refreshGames', tempGamesArr)
          // console.log(beacon)
          break

        case 'playerjoin':
          var tempLoadingReminder;

          tempLoadingReminder = 'player: port '+ temp.player.port + ' ip ' + temp.player.ip+'joined';
           this.$store.commit('changeLoadingReminder', tempLoadingReminder)
          break

        default:
          console.log('TEST' + temp.type)
      }
    }
  }
}
</script>
