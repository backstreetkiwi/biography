<template>
  <div id="timeline">
    <DateSelector @dayChanged="fillGalleryFromTimelline"/>
    <Gallery :mediaFiles="galleryMediaFiles" />
  </div>
</template>

<script>
import axios from "axios";
import Gallery from './Gallery'
import DateSelector from './DateSelector'

export default {
  name: 'Timeline',
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
</style>
