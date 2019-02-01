<template>
  <div id="app">
    <DateSelector @dayChanged="updateDay"/>
    <Gallery :day="day" />
  </div>
</template>

<script>
import Gallery from './components/Gallery'
import DateSelector from './components/DateSelector'

export default {
  name: 'App',
  components: {
    DateSelector,
    Gallery
  },
  data: function() {
    return {
      day: ""
    };
  },  
  methods: {
    updateDay(day) {
      this.day = "/"+ day.slice(0,4) + "/"+ day.slice(5,7) + "/"+ day.slice(8,10) + "/";
    }
  }  
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
