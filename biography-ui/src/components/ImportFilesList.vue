<template>
  <div>
    <div>
      <label>File
        <input type="file" id="file" ref="uploadFiles" multiple="multiple" v-on:change="handleFileUpload()"/>
      </label>
        <button v-on:click="submitFileUpload()">Submit</button>
    </div>
    <span>{{jobRunning ? "RUNNING" : ""}}</span>
    <button :disabled="jobRunning" v-on:click="clearImportJob()">Clear Import Job</button>
    <button :disabled="jobRunning" v-on:click="cleanUpImportJob()">Clean Up (processed files)</button>
    <button :disabled="jobRunning" v-on:click="startImport()">START</button>
    <button :disabled="jobRunning" v-on:click="copyFirstAlbumToAll()">copy Album from #1</button>
    <button :disabled="jobRunning" v-on:click="copyFirstDescriptionToAll()">copy Description from #1</button>
    <div class="files">
      <div class="file" v-for="(file, index) in files" v-bind:class="{ fileChanged: file.changed }">
          <div class="thumbnail" v-bind:style="{ background: 'url(' + file.thumbnailUrl + ')', 'background-size': 'contain', 'background-repeat': 'no-repeat', 'background-position': 'center' }"></div>
          <div class="importDetails">
            <table>
              <tr>
                <td class="filename" colspan="2">{{file.name}}</td>
              </tr>
              <tr>
                <td class="label">Description</td>
                <td class="input">
                  <input 
                    v-model="file.description"
                    class="description"
                    :disabled="jobRunning"
                    v-on:keyup.ctrl.up="copyDescriptionFromPredecessor(index)" 
                    v-on:input="markFileAsChanged(index)" 
                    v-on:change="saveChange(index)">
                  </input>
                </td>
              </tr>
              <tr>
                <td class="label">Album</td>
                <td class="input">
                  <input 
                    v-model="file.album"
                    class="album" 
                    :disabled="jobRunning"
                    v-on:keyup.ctrl.up="copyAlbumFromPredecessor(index)" 
                    v-on:input="markFileAsChanged(index)" 
                    v-on:change="saveChange(index)">
                  </input> 
                </td>
              </tr>
              <tr>
                <td class="label">Datetime original</td>
                <td class="input">
                  <input 
                    v-model="file.datetimeOriginal"
                    class="datetimeOriginal"
                    disabled="disabled"></input>
                  <button v-on:click="setDatetimeOriginal(index)">Edit</button>
                </td>
              </tr>
              <tr>
                <td class="label">Import Result</td>
                <td class="input">
                  {{file.importResult}}
                </td>
              </tr>
            </table>
          </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "ImportFilesList",
  data() {
    return {
      files: [],
      jobRunning: false,
      interval: null,
      uploadFiles: []
    };
  },
  mounted() {
    this.updateImportJobState();
    this.updateForm();
  },
  methods: {
    updateForm: function() {
        axios({ method: "GET", "url": "http://localhost:8080/rest/import/files/" }).then(result => {
          for (var key in result.data) {
              result.data[key].thumbnailUrl = "http://localhost:8080/file/import/" + result.data[key].id + "/thumbnail";
              result.data[key].changed = false;
          }
            this.files = result.data;
        }, error => {
            this.files = [];
        });
    },
    updateImportJobState: function() {
        axios({ method: "GET", "url": "http://localhost:8080/rest/import/job/state/" }).then(result => {
          this.jobRunning = (result.data.state=="running");
          if(!this.jobRunning) {
            window.clearInterval(this.interval);
          }
        }, error => {
            alert(error.message);
        });
    },
    clearImportJob: function() {
        axios({ method: "POST", "url": "http://localhost:8080/rest/import/clear/" }).then(result => {
            this.updateForm();
        }, error => {
            alert(error.message);
        });
    },
    cleanUpImportJob: function() {
        axios({ method: "POST", "url": "http://localhost:8080/rest/import/cleanup/" }).then(result => {
            this.updateForm();
        }, error => {
            alert(error.message);
        });
    },
    startImport: function() {
        axios({ method: "POST", "url": "http://localhost:8080/rest/import/start/" }).then(result => {
        }, error => {
            alert(error.message);
        });
        this.interval = setInterval(function () {
          this.updateImportJobState();
          this.updateForm();
        }.bind(this), 1000);
    },
    copyDescriptionFromPredecessor: function(index) {
      if(index!=0) {
        this.files[index].description = this.files[index-1].description 
        this.files[index].changed = true;
        this.saveChange(index);
      }
    },
    copyFirstDescriptionToAll: function() {
      for(var index in this.files) {
        this.files[index].description = this.files[0].description
        this.saveChange(index);
      }
    },
    copyFirstAlbumToAll: function() {
      for(var index in this.files) {
        this.files[index].album = this.files[0].album 
        this.saveChange(index);
      }
    },
    copyAlbumFromPredecessor: function(index) {
      if(index!=0) {
        this.files[index].album = this.files[index-1].album 
        this.files[index].changed = true;
        this.saveChange(index);
      }
    },
    setDatetimeOriginal: function(index) {
      var datetimeOriginal = prompt("Enter new Date/Time original", this.files[index].datetimeOriginal);
      if (datetimeOriginal != null && datetimeOriginal != "") {
        this.files[index].datetimeOriginal = datetimeOriginal;
        this.saveChange(index);
      }
    },
    saveChange: function(index) {
        axios({ 
          method: "PUT", 
          "url": "http://localhost:8080/rest/import/files/" + this.files[index].id, 
          params: {
            "description": this.files[index].description, 
            "album": this.files[index].album,
            "datetimeOriginal": this.files[index].datetimeOriginal
          }
        }).then(result => {
          this.files[index].changed = false;
        }, error => {
            alert(error.message);
        });
    },
    markFileAsChanged: function(index) {
      this.files[index].changed = true;
    },
    handleFileUpload: function() {
      var fileCount = this.$refs.uploadFiles.files.length;
      this.uploadFiles = [];
      for (let index = 0; index < fileCount; index++) {
        this.uploadFiles[index] = this.$refs.uploadFiles.files[index];
      }
    },
    submitFileUpload: function() {
      if(this.uploadFiles.length==0) {
        return;
      }
      let formData = new FormData();
      for (let index = 0; index < this.uploadFiles.length; index++) {
        formData.append('file', this.uploadFiles[index]);
      }
      axios.post( 'http://localhost:8080/rest/import/upload/',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
        ).then((response) => {
          console.log('Upload Successful.');
          this.updateForm();
        }
        ).catch(function(){
          console.log('FAILURE!!');
        });
    }
  }
};
</script>

<style scoped>

div.files {
}

div.file {
  background-color: #DDD;
  margin-top: 10px;
  clear: both;
  height: 220px;
}

div.fileChanged {
  background-color: salmon;
}

div.thumbnail {
  background-color:limegreen;
  margin: 10px;
  float: left;
  width: 200px;
  height: 200px;
  vertical-align: baseline;
}

div.importDetails {
  margin: 10px;
  float: left;
}


td.label {
  width: 200px;
  text-align: left;
}

td.input {
  text-align: left;
}

td.filename {
  text-align: left;
  font-weight: bold;
}

input.description {
  width: 500px;
}

input.album {
  width: 500px;
}

</style>