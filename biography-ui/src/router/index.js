import Vue from 'vue'
import Router from 'vue-router'

import Timeline from '@/components/Timeline'
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
    }
  ]
})
