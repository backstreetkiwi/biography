<template>
    <div id="gallery">
        <div class="large-image" v-bind:class="{'large-image-hidden' : !showImage}" v-on:click.self="closeImagePopup()">
            <img v-if="this.mediaFile!=null" v-bind:src="this.mediaFile.fileUrl" v-on:click.self="closeImagePopup()"/>
            <div v-if="this.mediaFile!=null" class="description-overlay-large-image" v-bind:class="{'description-overlay-hidden' : !galleryShowDescription}">
                <div class="description">
                    {{this.mediaFile!=null ? this.mediaFile.description: ''}}
                    <a href="#" v-on:click="editDescription()">[edit]</a>
                </div>
            </div>
            <div v-if="this.mediaFile!=null" class="albums-overlay-large-image" v-bind:class="{'albums-overlay-hidden' : !galleryShowAlbums}">
                <div class="albums" v-for="album in this.mediaFile.albums" v-bind:key="album">
                    {{album}}
                    <a href="#" v-on:click="removeAlbum(album)">[X]</a>
                </div>
                <div class="albums" v-on:click="addAlbum()">+</div>
            </div>
        </div>
        <div class="gallery-toolbar">
            <GalleryToggleDescription :descriptionOn="galleryShowDescription" @toggled="descriptionToggled"/>
            <GalleryToggleAlbums :albumsOn="galleryShowAlbums" @toggled="albumsToggled"/>
        </div>
        <div class="chapter" v-for="chapter in mediaFiles" v-bind:key="chapter.title">
            <div class="chapter-title">{{chapter.title}}</div>
            <div class="gallery-item" v-for="mediaFile in chapter.mediaFiles" v-bind:key="mediaFile.fileName" v-on:click="showImagePopup(mediaFile)">
                <div class="gallery-thumbnail">
                    <img v-bind:src="mediaFile.thumbnailUrl"/>
                    <div class="description-overlay" v-bind:class="{'description-overlay-hidden' : !galleryShowDescription}">
                        <div class="description">{{mediaFile.description}}</div>
                    </div>
                    <div class="albums-overlay" v-bind:class="{'albums-overlay-hidden' : !galleryShowAlbums}">
                        <div class="albums" v-for="album in mediaFile.albums" v-bind:key="album">{{album}}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import axios from "axios";
    import GalleryToggleDescription from './GalleryToggleDescription'
    import GalleryToggleAlbums from './GalleryToggleAlbums'

    export default {
        name: 'Gallery',
        components: {
            GalleryToggleDescription,
            GalleryToggleAlbums
        },
        data: function() {
            return {
                baseUrl: "http://localhost:8080/",
                galleryShowDescription: true,
                galleryShowAlbums: false,
                showImage: false
            };
        },  
        props: {
            mediaFiles: {
                type: Array,
                default: () => []
            }
        },
        methods: {
            descriptionToggled: function(descriptionOn) {
                this.galleryShowDescription = descriptionOn;
            },
            albumsToggled: function(albumsOn) {
                this.galleryShowAlbums = albumsOn;
            },
            showImagePopup: function(mediaFile) {
                this.mediaFile = mediaFile;
                this.showImage = true;
            },
            closeImagePopup: function() {
                this.showImage = false;
                this.mediaFile = null;
            },
            editDescription: function() {
                var newDescription = prompt("new description", this.mediaFile.description);
                if(newDescription) {
                    var currentMediaFile = this.mediaFile;
                    var restUrl = this.baseUrl + "rest/file/" + currentMediaFile.fileName + "?description=" + encodeURIComponent(newDescription);    
                    axios({ method: "PUT", "url": restUrl }).then(result => {
                        currentMediaFile.description = newDescription;
                    }, error => {
                        alert('Error')
                    });
                }
            },
            addAlbum: function() {
                var newAlbum = prompt("new album");
                if(newAlbum) {
                    var currentMediaFile = this.mediaFile;
                    var restUrl = this.baseUrl + "rest/file/" + currentMediaFile.fileName + "/albums/" + encodeURIComponent(newAlbum) + "/";    
                    axios({ method: "POST", "url": restUrl }).then(result => {
                        currentMediaFile.albums.push(newAlbum);
                    }, error => {
                        alert('Error')
                    });
                }
            },
            removeAlbum: function(album) {
                var deleteAlbum = confirm("Do you want to remove the album?");
                if(deleteAlbum) {
                    var currentMediaFile = this.mediaFile;
                    var restUrl = this.baseUrl + "rest/file/" + currentMediaFile.fileName + "/albums/" + encodeURIComponent(album) + "/";    
                    axios({ method: "DELETE", "url": restUrl }).then(result => {
                        for(var i=currentMediaFile.albums.length; i--;) {
                            if(currentMediaFile.albums[i]==album){
                                currentMediaFile.albums.splice(i,1);
                            }
                        }
                    }, error => {
                        alert('Error')
                    });
                }
            }
        }
    }
</script>

<style scoped>
div.large-image {
    position: fixed;
    top: 0px;
    width: 100%;
    height: 100%;
    background-color: rgba(0,0,0,0.8);
    z-index: 10;
    text-align: center;
}

div.large-image img {
    position: absolute;
    top: 3%;
    bottom: 3%;
    left: 3%;
    right: 3%;
    max-width: 94%;
    max-height: 94%;
    width: auto;
    height: auto;
    margin: auto;
    border-color: white;
    border-width: 4px;
    border-radius: 8px;
    border-style: solid;
    image-orientation: from-image;
}

div.large-image-hidden {
    display: none;
}

div.gallery-toolbar {
    padding: 10px 0px 10px 5px;
}

div.chapter-title {
    position: relative;
    clear: both;
    font-size: 20px;
    text-align: left;
    padding: 25px 0px 10px 5px;
}

div.gallery-item {
    position: relative;
    background-color: black;
    float:left;
    margin: 5px 5px 0px 0px;
    padding: 0px;
    height: 302px;
    border-radius: 5px;
}

div.gallery-thumbnail {
    position: relative;
    margin: 1px;
    padding: 0px;
    height: 300px;
}

div.description-overlay {
    position: absolute;
    bottom: 0px;
    left: 0px;
    width: 100%;
    height: auto;
    overflow: hidden;
    background-color: rgba(0,0,0,0.5);
    z-index: 2;
    cursor: pointer;    
    padding: 0px;
}

div.description-overlay div.description {
    color: white;
    font-size: 15px;
    padding: 5px;
    text-align: center;
}

div.description-overlay-large-image {
    position: absolute;
    bottom: 0px;
    left: 0px;
    width: 100%;
    height: auto;
    overflow: hidden;
    background-color: rgba(0,0,0,0.75);
    z-index: 20;
    cursor: pointer;    
    padding: 0px;
}

div.description-overlay-large-image div.description {
    color: white;
    font-size: 20px;
    padding: 10px;
    text-align: center;
}

div.description-overlay-large-image div.description a {
    color: lightsalmon;
    font-size: 20px;
    padding: 10px;
    text-align: center;
    text-decoration: none;
}

div.description-overlay-hidden {
    display: none;
}

div.albums-overlay {
    position: absolute;
    top: 0px;
    right: 0px;
    width: auto;
    height: auto;
    overflow: hidden;
    z-index: 2;
    cursor: pointer;    
    padding: 0px;
}

div.albums-overlay div.albums {
    float: right;
    color: rgb(240,240,240);
    font-size: 15px;
    padding: 5px 10px;
    margin: 5px;
    text-align: center;
    background-color: rgb(43, 84, 197);
    border-radius: 5px;
}

div.albums-overlay-large-image {
    position: absolute;
    top: 0px;
    right: 0px;
    width: auto;
    height: auto;
    overflow: hidden;
    z-index: 2;
    cursor: pointer;    
    padding: 0px;
}

div.albums-overlay-large-image div.albums {
    float: right;
    color: rgb(240,240,240);
    font-size: 20px;
    padding: 10px 20px;
    margin: 10px;
    text-align: center;
    background-color: rgb(43, 84, 197);
    border-radius: 5px;
}

div.albums-overlay-large-image div.albums a {
    color: lightsalmon;
    font-size: 20px;
    padding: 10px;
    text-align: center;
    text-decoration: none;
}

div.albums-overlay-hidden {
    display: none;
}

div.footer {
    background-color: blueviolet;
    position: relative;
    float: unset;
}

</style>