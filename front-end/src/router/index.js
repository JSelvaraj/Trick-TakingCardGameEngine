import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/gameDiscover',
    name: 'GameDiscover',
    component: () => import('../views/GameDiscover.vue')
  },
  {
    path: '/gameRoom',
    name: 'GameRoom',
    component: () => import('../views/GameRoom.vue')
  },
  {
    path: '/hostGame',
    name: 'HostGame',
    component: () => import('../views/HostGame.vue')
  },
  {
    path: '/gameBoard',
    name: 'GameBoard',
    component: () => import('../views/GameBoard.vue')
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
