<template>
  <div class="navigation">
    <nav>
      <router-link class="router-link" :to="`/Timeline`"><img src="./img/navigation/menu-calendar.png" /></router-link>
      <router-link class="router-link" :to="`/Search`"><img src="./img/navigation/menu-search.png" /></router-link>
      <router-link class="router-link" :to="`/Import`"><img src="./img/navigation/menu-import.png" /></router-link>
      <router-link class="router-link" :to="`/Batches`"><img src="./img/navigation/menu-batches.png" /></router-link>
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
div.navigation {
  text-align: right;
}
.router-link {
  margin-right: 50px;
  outline: none;
}
</style>

