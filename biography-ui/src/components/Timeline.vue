<template>
  <div id="timeline">
    <DateSelector @monthChanged="fillGalleryFromTimelline"/>
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
      galleryMediaFiles: [],
      currentYearMonth: null
    };
  },  
  mounted: function() {
    if(!this.galleryMediaFiles || this.galleryMediaFiles.length==0) {
      this.fillGalleryWithMostRecentMonth();
    }
  },
  methods: {
    fillGalleryFromTimelline: function(yearMonth) {
      this.galleryMediaFiles = [];
      if(!yearMonth) {
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
              result.data.mediaFiles[key].thumbnailUrl = this.baseUrl + "file/" + result.data.mediaFiles[key].thumbnailFileName + "/300";
              result.data.mediaFiles[key].fileUrl = this.baseUrl + "file/" + result.data.mediaFiles[key].fileName + "/raw";
          }
          result.data.mediaFiles.sort(function(a,b) { return a.fileName < b.fileName });
          this.galleryMediaFiles[idx]['mediaFiles'] = result.data.mediaFiles;
      }, error => {
        alert('Error')
      });
    },
    fillGalleryWithMostRecentMonth: function() {
      // TODO: Set year/month picker accordingly
      var restUrl = this.baseUrl + "rest/mediafiles/stats/";    
      axios({ method: "GET", "url": restUrl }).then(result => {
          this.fillGalleryFromTimelline(result.data.mostRecentYearMonth);
      }, error => {
        alert('Error')
      });

    }
  }  
}
</script>

<style>
</style>
