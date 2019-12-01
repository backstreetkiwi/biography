import Vue from 'vue'
import Router from 'vue-router'

import Timeline from '@/components/Timeline'
import Search from '@/components/Search'
import Batches from '@/components/Batches'
import Import from '@/components/Import'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/timeline',
      name: 'Timeline',
      component: Timeline
    },
    {
      path: '/import',
      name: 'Import',
      component: Import
    },
    {
      path: '/search',
      name: 'Search',
      component: Search
    },
    {
      path: '/batches',
      name: 'Batches',
      component: Batches
    }
  ]
})
