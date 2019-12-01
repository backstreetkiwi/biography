<template>
  <div id="search">
    <Query @queryFired="fillGallery"/>
    <Gallery :mediaFiles="galleryMediaFiles" />
  </div>
</template>

<script>
import axios from "axios";
import Query from './Query'
import Gallery from './Gallery'

export default {
  name: 'Search',
  components: {
    Query,
    Gallery
  },
  data: function() {
    return {
      baseUrl: "http://localhost:8080/",
      galleryMediaFiles: []
    };
  },  
  methods: {
    fillGallery: function(queryString, queryModifier) {
      this.galleryMediaFiles = [];
      if(queryString==null) {
        return;
      }
      
      this.galleryMediaFiles = [ {'title': 'Searched for "' + queryString + queryModifier + '" ...', 'mediaFiles': []} ];
      var restUrl = this.baseUrl + "rest/search/?q=" + encodeURIComponent(queryString) + "&mode=" + queryModifier;    
      axios({ method: "GET", "url": restUrl }).then(result => {
          for (var key in result.data.mediaFiles) {
              result.data.mediaFiles[key].thumbnailUrl = this.baseUrl + "file/" + result.data.mediaFiles[key].fileName + "/300";
              result.data.mediaFiles[key].fileUrl = this.baseUrl + "file/" + result.data.mediaFiles[key].fileName + "/raw";
          }
          result.data.mediaFiles.sort(function(a,b) { return a.fileName < b.fileName });
          this.galleryMediaFiles[0]['mediaFiles'] = result.data.mediaFiles;
      }, error => {
        alert('Error')
      });
    }
  }  
}
</script>

<style>
</style>
