<template>
    <div id="gallery">
        <div class="add-album-box" v-bind:class="{'add-album-box-hidden' : !showAddAlbumBox}">
            <input ref="inputNewAlbumName" v-on:keyup.enter="submitAddAlbum()" v-on:keyup.esc="cancelAddAlbum()" v-model="newAlbumName"/>
        </div>
        <div class="edit-description-box" v-bind:class="{'edit-description-box-hidden' : !showEditDescriptionBox}">
            <input ref="inputDescription" v-on:keyup.enter="submitEditDescription()" v-on:keyup.esc="cancelEditDescription()" v-model="newDescription"/>
        </div>
        <div class="large-image" v-bind:class="{'large-image-hidden' : !showImage}" v-on:click.self="closeImagePopup()">
            <a class="shortcut-link" ref="shortcutLink" href="#" v-on:keyup="shortcutEvent"></a>
            <img v-bind:src="this.currentFileUrl" v-on:click.self="closeImagePopup()"/>
            <div v-if="this.mediaFile!=null" class="description-overlay-large-image" v-bind:class="{'description-overlay-hidden' : !galleryShowDescription}">
                <div class="description">
                    {{this.mediaFile!=null ? this.mediaFile.description: ''}}
                    <a href="#" v-on:click="editDescription()">edit</a>
                </div>
            </div>
            <div v-if="this.mediaFile!=null" class="albums-overlay-large-image" v-bind:class="{'albums-overlay-hidden' : !galleryShowAlbums}">
                <div class="albums" v-for="album in this.mediaFile.albums" v-bind:key="album">
                    {{album}}
                    <a href="#" v-on:click="removeAlbum(album)">x</a>
                </div>
                <div class="albums" v-on:click="addAlbum()">+</div>
            </div>
        </div>
        <div class="gallery-toolbar">
            <GalleryToggleDescription :descriptionOn="galleryShowDescription" @toggled="descriptionToggled"/>
            <GalleryToggleAlbums :albumsOn="galleryShowAlbums" @toggled="albumsToggled"/>
        </div>
        <div class="chapter" v-for="(chapter, cIndex) in mediaFiles" v-bind:key="chapter.title">
            <div class="chapter-title">{{chapter.title}}</div>
            <div class="gallery-item" v-for="(mediaFile, mfIndex) in chapter.mediaFiles" v-bind:key="mediaFile.fileName" v-on:click="showImagePopup(mediaFile, mfIndex, chapter, cIndex)">
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
                showImage: false,
                showAddAlbumBox: false,
                newAlbumName: "",
                showEditDescriptionBox: false,
                newDescription: "",
                currentFileUrl: ""
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
            showImagePopup: function(mediaFile, mfIndex, chapter, cIndex) {
                this.mediaFile = mediaFile;
                this.currentFileUrl = mediaFile.fileUrl;
                this.showImage = true;
                this.chapter = chapter;
                this.indexOfCurrentMediaFileInChapter = mfIndex;
                this.indexOfCurrentChapter = cIndex;
                this.setFocusOnShortcutLink();   
            },
            closeImagePopup: function() {
                this.showImage = false;
                this.mediaFile = null;
                this.currentFileUrl = "";
            },
            editDescription: function() {
                this.newDescription = this.mediaFile.description;
                this.showEditDescriptionBox = true;
                this.$nextTick(() => {
                    this.$refs.inputDescription.focus();
                });
            },
            submitEditDescription: function() {
                var currentMediaFile = this.mediaFile;
                var restUrl = this.baseUrl + "rest/file/" + currentMediaFile.fileName + "?description=" + encodeURIComponent(this.newDescription);    
                this.showEditDescriptionBox = false;
                this.setFocusOnShortcutLink();   
                axios({ method: "PUT", "url": restUrl }).then(result => {
                    currentMediaFile.description = this.newDescription;
                }, error => {
                    alert('Error');
                });
            },
            cancelEditDescription: function() {
                this.showEditDescriptionBox = false;
                this.newDescription = "";
                this.setFocusOnShortcutLink();   
            },
            addAlbum: function() {
                this.newAlbumName = "";
                this.showAddAlbumBox = true;
                this.$nextTick(() => {
                    this.$refs.inputNewAlbumName.focus();
                });
            },
            submitAddAlbum: function() {
                var currentMediaFile = this.mediaFile;
                var restUrl = this.baseUrl + "rest/file/" + currentMediaFile.fileName + "/albums/" + encodeURIComponent(this.newAlbumName) + "/";    
                this.showAddAlbumBox = false;
                this.setFocusOnShortcutLink();   
                axios({ method: "POST", "url": restUrl }).then(result => {
                    currentMediaFile.albums.push(this.newAlbumName);
                }, error => {
                    alert('Error');
                });
            },
            cancelAddAlbum: function() {
                this.showAddAlbumBox = false;
                this.newAlbumName = "";
                this.setFocusOnShortcutLink();   
            },
            removeAlbum: function(album) {
                var currentMediaFile = this.mediaFile;
                var restUrl = this.baseUrl + "rest/file/" + currentMediaFile.fileName + "/albums/" + encodeURIComponent(album) + "/"; 
                this.setFocusOnShortcutLink();   
                axios({ method: "DELETE", "url": restUrl }).then(result => {
                    for(var i=currentMediaFile.albums.length; i--;) {
                        if(currentMediaFile.albums[i]==album){
                            currentMediaFile.albums.splice(i,1);
                        }
                    }
                }, error => {
                    alert('Error');
                });
            },
            shortcutEvent(event) {
                switch(event.code) {
                    case "KeyE": {
                        if(this.galleryShowDescription) {
                            this.editDescription();
                        }
                        break;
                    }
                    case "Escape": {
                        this.closeImagePopup();
                        break;
                    }
                    case "BracketRight": {
                        if(this.galleryShowAlbums) {
                            this.addAlbum();
                        }
                        break;
                    }
                    case "ArrowLeft": {
                        if(this.indexOfCurrentMediaFileInChapter > 0) {
                            this.indexOfCurrentMediaFileInChapter--;
                            this.mediaFile = this.chapter.mediaFiles[this.indexOfCurrentMediaFileInChapter];
                            this.currentFileUrl = this.mediaFile.fileUrl;
                        } else {
                            if(this.indexOfCurrentChapter > 0) {
                                this.indexOfCurrentChapter--;
                                this.chapter = this.mediaFiles[this.indexOfCurrentChapter];
                                this.indexOfCurrentMediaFileInChapter = this.chapter.mediaFiles.length-1;
                                this.mediaFile = this.chapter.mediaFiles[this.indexOfCurrentMediaFileInChapter];
                                this.currentFileUrl = this.mediaFile.fileUrl;
                            }
                        }
                        break;
                    }
                    case "ArrowRight": {
                        if(this.indexOfCurrentMediaFileInChapter < this.chapter.mediaFiles.length-1) {
                            this.indexOfCurrentMediaFileInChapter++;
                            this.mediaFile = this.chapter.mediaFiles[this.indexOfCurrentMediaFileInChapter];
                            this.currentFileUrl = this.mediaFile.fileUrl;
                        } else {
                            if(this.indexOfCurrentChapter < this.mediaFiles.length-1) {
                                this.indexOfCurrentChapter++;
                                this.chapter = this.mediaFiles[this.indexOfCurrentChapter];
                                this.indexOfCurrentMediaFileInChapter = 0;
                                this.mediaFile = this.chapter.mediaFiles[this.indexOfCurrentMediaFileInChapter];
                                this.currentFileUrl = this.mediaFile.fileUrl;
                            }
                        }
                        break;
                    }
                }
            },
            // the edit link has all the key shortcuts in large image view mode
            setFocusOnShortcutLink() {
                this.$nextTick(() => {
                    this.$refs.shortcutLink.focus();
                });
            }
        }
    }
</script>

<style scoped>
div.add-album-box {
    position: fixed;
    top: 40%;
    left: 20%;
    right: 20%;
    width: 60%;
    padding: 20px;
    border-radius: 8px;
    background-color:rgb(43, 84, 197);
    z-index: 30;
    text-align: center;
}

div.add-album-box input {
    width: 90%;
    font-size: 25px;
    color: white;
    border: none;
    background-color: rgb(43, 84, 197);
}

div.add-album-box-hidden {
    display: none;
}

div.edit-description-box {
    position: fixed;
    top: 40%;
    left: 20%;
    right: 20%;
    width: 60%;
    padding: 20px;
    border-radius: 8px;
    background-color: black;
    z-index: 30;
    text-align: center;
}

div.edit-description-box input {
    width: 90%;
    font-size: 25px;
    color: white;
    border: none;
    background-color: black;
}

div.edit-description-box-hidden {
    display: none;
}

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

div.large-image a.shortcut-link {
    outline: none;
    font-size: 100px;
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
    color: gray;
    font-size: 20px;
    padding: 10px;
    text-align: center;
    text-decoration: none;
    outline: none;
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
    top: 10px;
    right: 10px;
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
    font-size: 18px;
    padding: 8px 12px;
    margin: 10px;
    text-align: center;
    background-color: rgb(43, 84, 197);
    border-radius: 5px;
}

div.albums-overlay-large-image div.albums a {
    color: gray;
    font-size: 18px;
    padding: 0px 0px 0px 10px;
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