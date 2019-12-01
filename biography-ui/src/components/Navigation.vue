<template>
  <div>
    <nav>
      <router-link class="spacing" v-for="routes in links" 
      v-bind:key="routes.id"
      :to="`${routes.page}`">{{routes.text}}</router-link>
    </nav>
  </div>
</template>

<script>
export default {
  name: 'Navigation',
  data() {
    return {
      links: [
        {
          id: 0,
          text: 'Timeline',
          page:'/Timeline'
        },
        {
          id: 1,
          text: 'Search',
          page:'/Search'
        },
        {
          id: 2,
          text: 'Import',
          page:'/Import'
        },
        {
          id: 3,
          text: 'Batches',
          page:'/Batches'
        }
      ]
    }
  }
}
</script>
<style>
.spacing {
  margin-right: 10px;
}
</style>

