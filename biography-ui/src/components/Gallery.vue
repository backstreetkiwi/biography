<template>
    <div id="gallery">
        <div class="large-image" v-bind:class="{'large-image-hidden' : !showImage}" v-on:click="closeImagePopup()">
            <img v-bind:src="this.imageSrc" />
        </div>
        <div class="gallery-toolbar">
            <GalleryToggleDescription :descriptionOn="galleryShowDescription" @toggled="descriptionToggled"/>
            <GalleryToggleAlbums :albumsOn="galleryShowAlbums" @toggled="albumsToggled"/>
        </div>
        <div class="chapter" v-for="chapter in mediaFiles" v-bind:key="chapter.title">
            <div class="chapter-title">{{chapter.title}}</div>
            <div class="gallery-item" v-for="mediaFile in chapter.mediaFiles" v-bind:key="mediaFile.fileName" v-on:click="showImagePopup(mediaFile.fileUrl)">
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
            showImagePopup: function(fileName) {
                this.imageSrc = fileName;
                this.showImage = true;
            },
            closeImagePopup: function() {
                this.showImage = false;
                this.imageSrc = "";
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
    top: 5%;
    bottom: 5%;
    left: 5%;
    right: 5%;
    max-width: 90%;
    max-height: 90%;
    width: auto;
    height: auto;
    margin: auto;
    border-color: white;
    border-width: 5px;
    border-radius: 10px;
    border-style: solid;
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

div.description-overlay-hidden {
    display: none;
}

div.description {
    color: white;
    font-size: 15px;
    padding: 5px;
    text-align: center;
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

div.albums-overlay-hidden {
    display: none;
}

div.albums {
    float: right;
    color: rgb(240,240,240);
    font-size: 15px;
    padding: 5px 10px;
    margin: 5px;
    text-align: center;
    background-color: rgb(43, 84, 197);
    border-radius: 5px;
}

div.footer {
    background-color: blueviolet;
    position: relative;
    float: unset;
}

</style>