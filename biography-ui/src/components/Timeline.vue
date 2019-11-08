<template>
  <div id="timeline">
    <DateSelector @monthChanged="fillGalleryFromTimelline"/>
    <GalleryToggleDescription :descriptionOn="galleryShowDescription" @toggled="descriptionToggled"/>
    <Gallery :mediaFiles="galleryMediaFiles" :showDescription="galleryShowDescription" />
  </div>
</template>

<script>
import axios from "axios";
import Gallery from './Gallery'
import DateSelector from './DateSelector'
import GalleryToggleDescription from './GalleryToggleDescription'

export default {
  name: 'Timeline',
  components: {
    DateSelector,
    GalleryToggleDescription,
    Gallery
  },
  data: function() {
    return {
      baseUrl: "http://localhost:8080/",
      galleryMediaFiles: [],
      galleryShowDescription: true
    };
  },  
  methods: {
    fillGalleryFromTimelline: function(yearMonth) {
      this.galleryMediaFiles = [];
      if(yearMonth==null) {
        return;
      }
      var restUrl = this.baseUrl + "rest/mediafiles/" + yearMonth.slice(0,4) + "/"+ yearMonth.slice(5,7) + "/";    
      axios({ method: "GET", "url": restUrl }).then(result => {
          var dates = [];
          for (var key in result.data) {
            dates.push(result.data[key].date);
          }
          dates.sort();
          dates.reverse();
          
          for (var idx in dates) {
            this.galleryMediaFiles.push({ 'title': dates[idx].slice(8,10) + "." + dates[idx].slice(5,7) + "." + dates[idx].slice(0,4), 'mediaFiles': []});
          }

          for (var idx in dates) {
            this.loadMediaFiles(idx, dates[idx]);
          }
      });
    },
    loadMediaFiles: function(idx, date) {
      if(date==null) {
        return;
      }
      var restUrl = this.baseUrl + "rest/mediafiles/" + date.slice(0,4) + "/" + date.slice(5,7) + "/" + date.slice(8,10) + "/";    
      axios({ method: "GET", "url": restUrl }).then(result => {
          for (var key in result.data.mediaFiles) {
              result.data.mediaFiles[key].thumbnailUrl = this.baseUrl + "file/" + result.data.mediaFiles[key].fileName + "/300";
          }
          result.data.mediaFiles.sort(function(a,b) { return a.fileName < b.fileName });
          this.galleryMediaFiles[idx]['mediaFiles'] = result.data.mediaFiles;
      }, error => {
        alert('Error')
      });
    },
    descriptionToggled: function(descriptionOn) {
      this.galleryShowDescription = descriptionOn;
    }
  }  
}
</script>

<style>
</style>
