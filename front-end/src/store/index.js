import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    selectGameTypes_vx:''
  },
  mutations: {
    changeSelectGameTypes_vx(state,item){
      state.selectGameTypes_vx = item;
    }
  },
  actions: {
  },
  modules: {
  }
})
