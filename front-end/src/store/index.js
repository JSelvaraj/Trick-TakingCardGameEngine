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
    displayCardPool: [],
    currenttrump: '',
    myselfIndex: 0,
    curPlayerIndex: undefined,
    myHandCards: [],
    handCards: [],
    // cards1: {
    //       rank: '',
    //       suit: '',
    //       imgPath:''
    // }
    players: [],
    games: [],
    loadingReminder: '',
    gameMessage: '',
    loadNotComplete: true,


     //for bidding:
     suitenabledBID: '',
     numberofroundsenabledBID: '',
     doublingenabledBID: '',
     passingenabledBID: '',
     bidblindenabledBID: '',
     numberofroundsBID: [],
     suitsBID:[],
     isPlayerVulnBID: '',
     firstroundBID: ''
  },
  mutations: {

    //for bidding

    setSuitenabledBID(state, suitenabledINPUT){
      state.suitenabledBID = suitenabledINPUT
    },

    setNumberofroundsenabledBID(state, numberofroundsenabledINPUT){
      state.numberofroundsenabledBID = numberofroundsenabledINPUT
    },

    setNumberofroundsBID(state, numberofroundsINPUT){
      state.numberofroundsBID = numberofroundsINPUT
    },

    setDoublingenabledBID(state, doublingenabledINPUT){
      state.doublingenabledBID = doublingenabledINPUT
    },

    setPassingenabledBID(state, passingenabledINPUT){
      state.passingenabledBID = passingenabledINPUT
    },

    setBidblindenabledBID(state, bidlindenabledINPUT){
      state.bidblindenabledBID = bidlindenabledINPUT
    },

    setSuitsBID(state, suitsINPUT){
      state.suitsBID = suitsINPUT
    },

    setIsPlayerVulnBID(state, isPlayerVulnINPUT){
      state.isPlayerVulnBID = isPlayerVulnINPUT
    },

    setFirstroundBID(state, firstroundINPUT){
      state.firstroundBID = firstroundINPUT
    },
    //


    setLoadNotComplete (state, booleanTrigger) {
      state.loadNotComplete = booleanTrigger
    },

    setCurrentTrump (state, currenttrumpNew) {
      state.currenttrump = currenttrumpNew
    },

    changeSelectGameTypes_vx (state, item) {
      state.selectGameTypes_vx = item
    },

    dataUpdate (state, payload) {
      state.data = payload
    },

    loadingDisable (state) {
      state.loadEnable = false
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

    setDisplayCardPool (state, cardpool) {
      state.displayCardPool = cardpool
    },

    setMyHandCards (state, cards) {
      state.myHandCards = cards
    },

    setPlayerTurn (state, newPlayers) {
      state.players = newPlayers
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

    changeLoadingReminder (state, reminder) {
      state.loadingReminder = reminder
    },

    // initPlayerArr (state, n) {
    //   var j
    //   for (j = 0; j < n; j++) {
    //     state.players.push({ myturn: false })
    //   }
    // },

    setPlayerArr (state, arr) {
      state.players = arr
    },

    appendGameMessage (state, newMessage) {
      state.gameMessage = state.gameMessage + newMessage + '\n'
      // console.log('TEST GAME MESSAGE: ' + state.gameMessage)
    }
  },

  actions: {
    dataUpdate (context, payload) {
      context.commit('dataUpdate', payload)
    }
  },
  modules: {
  }
})
