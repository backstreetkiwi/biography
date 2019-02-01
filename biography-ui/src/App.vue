<template>
  <div id="app">
    <DateSelector @dayChanged="fillGalleryFromTimelline"/>
    <Gallery :mediaFiles="galleryMediaFiles" />
  </div>
</template>

<script>
import axios from "axios";
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
      baseUrl: "http://localhost:8080/",
      galleryMediaFiles: []
    };
  },  
  methods: {
    fillGalleryFromTimelline: function(day) {
      if(day==null) {
        this.galleryMediaFiles = [];
        return;
      }
      var restUrl = this.baseUrl + "rest/mediafiles/" + day.slice(0,4) + "/"+ day.slice(5,7) + "/"+ day.slice(8,10) + "/";    
      axios({ method: "GET", "url": restUrl }).then(result => {
          for (var key in result.data.mediaFiles) {
              result.data.mediaFiles[key].thumbnailUrl = this.baseUrl + "file/" + result.data.mediaFiles[key].fileName + "/200";
          }
          this.galleryMediaFiles = result.data.mediaFiles;
      }, error => {
          this.galleryMediaFiles = [];
      });
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
