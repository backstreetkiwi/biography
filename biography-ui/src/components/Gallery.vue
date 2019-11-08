<template>
    <div class="gallery">
        <div class="chapter" v-for="chapter in mediaFiles" v-bind:key="chapter.title">
            <div class="chapter-title">{{chapter.title}}</div>
            <div class="gallery-item" v-for="mediaFile in chapter.mediaFiles" v-bind:key="mediaFile.fileName">
                <div class="gallery-thumbnail">
                    <img v-bind:src="mediaFile.thumbnailUrl"/>
                    <div class="description-overlay" v-bind:class="{'description-overlay-hidden' : !showDescription}">
                        <div class="description">{{mediaFile.description}}</div>
                    </div>
                    <div class="albums-overlay" v-bind:class="{'albums-overlay-hidden' : !showDescription}">
                        <div class="albums" v-for="album in mediaFile.albums" v-bind:key="album">{{album}}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import axios from "axios";

    export default {
        name: 'Gallery',
        props: {
            mediaFiles: {
                type: Array,
                default: () => []
            },
            showDescription: {
                type: Boolean,
                default: () => false
            }
        }
    }
</script>

<style scoped>
div.chapter {
}

div.chapter-title {
    position: relative;
    clear: both;
    font-size: 20px;
    text-align: left;
    padding: 25px 0px 10px 5px;
}

div.gallery-first-item {
    position: relative;
    background-color: black;
    clear: both;
    margin: 5px 5px 0px 0px;
    padding: 0px;
    height: 302px;
}

div.gallery-item {
    position: relative;
    background-color: black;
    float:left;
    margin: 5px 5px 0px 0px;
    padding: 0px;
    height: 302px;
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