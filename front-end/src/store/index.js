import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    selectGameTypes_vx: '',
    data: undefined,
    loadEnable: true,
    displayCard: {
      rank: '',
      suit: '',
      imgPath: ''
    },
    currenttrump: '',
    myselfIndex: 0,
    curPlayerIndex: undefined,
    myHandCards:[],
    handCards: [],
    // cards1: {
    //       rank: '',
    //       suit: '',
    //       imgPath:''
    // }
    players: [],
    games: [],
    loadingReminder:''
  },
  mutations: {
    changeSelectGameTypes_vx (state, item) {
      state.selectGameTypes_vx = item
    },

    dataUpdate (state, payload) {
      state.data = payload
    },

    loadingDisable (state) {
      state.loadEnable = false;
    },

    setMyselfIndex (state, n) {
      state.myselfIndex = n
    },

    setCurPlayerIndex (state, index) {
      state.curPlayerIndex = index
    },

    setDisplayCard (state, card) {
      state.displayCard.rank = card.rank
      state.displayCard.suit = card.suit
      state.displayCard.imgPath = card.imgPath
    },

    setMyHandCards(state,cards){
      state.myHandCards = cards
    },

    setPlayerTurn (state, index) {
      var i
      for (i = 0; i < players.length; i++) {
        if (i === index) {
          state.players[i].myturn = true
        } else {
          state.players[i].myturn = false
        }
      }
    },

    addPlayers (state) {
      state.players.push({ myturn: false })
    },

    removeHandCards (state) {
      state.handCards = []
    },

    addHandCard (state, card) {
      state.handCards.push(card)
    },

    refreshGames (state, newGames) {
      state.games = []
      state.games = newGames
    },

    changeLoadingReminder(state,reminder){
      state.loadingReminder = reminder
    },

    // initPlayerArr (state, n) {
    //   var j
    //   for (j = 0; j < n; j++) {
    //     state.players.push({ myturn: false })
    //   }
    // },

    setPlayerArr (state, arr) {
      state.players = arr;
   }
  },

  actions: {
    dataUpdate (context, payload) {
      context.commit('dataUpdate', payload)
    },
   

    setPlayerTurn_AC(context, index){
      context.commit('setPlayerTurn',index)
    } 

  },

   
  modules: {
  }
})
