<template>
    <div class="gallery">
        <h3>({{mediaFileCount}} media files found for day {{day}})</h3>
        <div class="media-file-thumb" v-for="mediaFile in mediaFiles">
            <img v-bind:src="mediaFile.urlThumb200"/>
            <br/>
            {{ mediaFile.description }}
        </div>
    </div>
</template>

<script>
    import axios from "axios";

    export default {
        name: 'Gallery',
        props: {
            day: {
                type: String
            }
        },        
        data () {
            return {
                mediaFileCount: "",
                mediaFiles: [],
                restUrl: ""
            }
        },
        mounted() {
            this.updateGallery(this.restUrl);
        },
        methods: {
            updateGallery: function(url) {
                axios({ method: "GET", "url": url }).then(result => {
                    this.mediaFileCount = result.data.count;
                    this.mediaFiles = result.data.mediaFiles;
                    for (var key in this.mediaFiles) {
                        this.mediaFiles[key].urlThumb200 = "http://localhost:8080/file/" + this.mediaFiles[key].fileName + "/200";
                    }
                }, error => {
                    this.mediaFileCount = 0;
                    this.mediaFiles = [];
                });
            }
         },
        watch: {
            day: function() {
                this.restUrl = "http://localhost:8080/rest/mediafiles" + this.day;
                this.updateGallery(this.restUrl);
            }
        }
    }
</script>

<style scoped>
div.media-file-thumb {
    float: left;
    background-color:darkslategrey;
    width: 200px;
    height: 200px;
    overflow: hidden;
    padding: 5px;
    margin: 5px;
    color: white;
}

img {
    width: 150px;
    height: 150px;
}
</style>