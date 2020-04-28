import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    selectGameTypes_vx:'',
    data: undefined
  },
  mutations: {
    changeSelectGameTypes_vx(state,item){
      state.selectGameTypes_vx = item;
    },

    dataUpdate(state, payload){
      state.data = payload;
    }

  },
  actions: {
    dataUpdate (context, payload){
        context.commit('dataUpdate', payload)
    }
  },
  modules: {
  }
})
